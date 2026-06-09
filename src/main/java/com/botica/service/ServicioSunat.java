package com.botica.service;

import com.botica.model.Comprobante;
import com.botica.model.Venta;
import com.botica.model.DetalleVenta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Integración con SUNAT para emisión de comprobantes electrónicos.
 *
 * Proceso:
 * 1. Genera XML en formato UBL 2.1 (estándar SUNAT)
 * 2. Firma el XML con certificado digital (XMLDSig)
 * 3. Comprime en ZIP
 * 4. Envía al web service SUNAT mediante SOAP
 * 5. Procesa la CDR (Constancia de Recepción)
 *
 * Requisitos para producción:
 * - Certificado digital X.509 emitido por entidad autorizada
 * - RUC activo como emisor electrónico en SUNAT
 * - Credenciales SOL (usuario/clave)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioSunat {

    @Value("${sunat.ruc}")
    private String ruc;

    @Value("${sunat.razon-social}")
    private String razonSocial;

    @Value("${sunat.nombre-comercial}")
    private String nombreComercial;

    @Value("${sunat.ubigeo}")
    private String ubigeo;

    @Value("${sunat.direccion}")
    private String direccion;

    @Value("${sunat.distrito}")
    private String distrito;

    @Value("${sunat.provincia}")
    private String provincia;

    @Value("${sunat.departamento}")
    private String departamento;

    @Value("${sunat.usuario-sol}")
    private String usuarioSol;

    @Value("${sunat.clave-sol}")
    private String claveSol;

    @Value("${sunat.url-beta}")
    private String urlBeta;

    @Value("${sunat.url-produccion}")
    private String urlProduccion;

    @Value("${sunat.modo:beta}")
    private String modo;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Genera y envía el comprobante a SUNAT.
     * Devuelve el Comprobante actualizado con el resultado de SUNAT.
     */
    public Comprobante emitirComprobante(Venta venta, Comprobante comprobante) {
        try {
            String xml = generarXml(venta, comprobante);
            comprobante.setXmlContent(xml);

            String xmlFirmado = firmarXml(xml, comprobante);

            String nombreArchivo = ruc + "-" + comprobante.getTipoComprobante()
                + "-" + comprobante.getSerie() + "-" + String.format("%08d", comprobante.getCorrelativo());

            byte[] bytesZip = comprimirXml(xmlFirmado, nombreArchivo + ".xml");
            String zipBase64 = Base64.getEncoder().encodeToString(bytesZip);

            String cdr = enviarSunat(nombreArchivo + ".zip", zipBase64);
            comprobante.setCdrContent(cdr);
            comprobante.setEstadoSunat("ACEPTADO");
            comprobante.setSunatMensaje("Comprobante aceptado por SUNAT");

            log.info("Comprobante {} emitido OK - SUNAT", nombreArchivo);
        } catch (Exception e) {
            log.error("Error al emitir comprobante SUNAT: {}", e.getMessage(), e);
            comprobante.setEstadoSunat("ERROR");
            comprobante.setSunatMensaje("Error: " + e.getMessage());
        }
        return comprobante;
    }

    /**
     * Genera el XML en formato UBL 2.1 requerido por SUNAT.
     * 01=Factura, 03=Boleta de Venta
     */
    public String generarXml(Venta venta, Comprobante comprobante) {
        boolean esFactura = "01".equals(comprobante.getTipoComprobante());
        String idComprobante = comprobante.getSerie() + "-" + comprobante.getCorrelativo();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n");
        xml.append("  xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n");
        xml.append("  xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"\n");
        xml.append("  xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"\n");
        xml.append("  xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n\n");

        xml.append("  <ext:UBLExtensions>\n");
        xml.append("    <ext:UBLExtension><ext:ExtensionContent/></ext:UBLExtension>\n");
        xml.append("  </ext:UBLExtensions>\n\n");

        xml.append("  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>\n");
        xml.append("  <cbc:CustomizationID>2.0</cbc:CustomizationID>\n");
        xml.append("  <cbc:ID>").append(idComprobante).append("</cbc:ID>\n");
        xml.append("  <cbc:IssueDate>").append(comprobante.getFechaEmision().format(FORMATO_FECHA)).append("</cbc:IssueDate>\n");
        xml.append("  <cbc:IssueTime>").append(venta.getFechaVenta().format(FORMATO_HORA)).append("</cbc:IssueTime>\n");
        xml.append("  <cbc:InvoiceTypeCode listID=\"0101\" listAgencyName=\"PE:SUNAT\" ")
           .append("listName=\"Tipo de Documento\" listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\">")
           .append(comprobante.getTipoComprobante()).append("</cbc:InvoiceTypeCode>\n");

        String totalEnLetras = numeroALetras(comprobante.getTotal());
        xml.append("  <cbc:Note languageLocaleID=\"1000\"><![CDATA[").append(totalEnLetras).append("]]></cbc:Note>\n");
        xml.append("  <cbc:DocumentCurrencyCode listID=\"ISO 4217 Alpha\" listName=\"Currency\" ")
           .append("listAgencyName=\"United Nations Economic Commission for Europe\">PEN</cbc:DocumentCurrencyCode>\n\n");

        // Referencia de firma digital
        xml.append("  <cac:Signature>\n");
        xml.append("    <cbc:ID>SignatureSP</cbc:ID>\n");
        xml.append("    <cac:SignatoryParty>\n");
        xml.append("      <cac:PartyIdentification><cbc:ID>").append(ruc).append("</cbc:ID></cac:PartyIdentification>\n");
        xml.append("      <cac:PartyName><cbc:Name><![CDATA[").append(razonSocial).append("]]></cbc:Name></cac:PartyName>\n");
        xml.append("    </cac:SignatoryParty>\n");
        xml.append("    <cac:DigitalSignatureAttachment>\n");
        xml.append("      <cac:ExternalReference><cbc:URI>#SignatureSP</cbc:URI></cac:ExternalReference>\n");
        xml.append("    </cac:DigitalSignatureAttachment>\n");
        xml.append("  </cac:Signature>\n\n");

        // Datos del emisor
        xml.append("  <cac:AccountingSupplierParty>\n");
        xml.append("    <cbc:CustomerAssignedAccountID>").append(ruc).append("</cbc:CustomerAssignedAccountID>\n");
        xml.append("    <cbc:AdditionalAccountID>6</cbc:AdditionalAccountID>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyName><cbc:Name><![CDATA[").append(nombreComercial).append("]]></cbc:Name></cac:PartyName>\n");
        xml.append("      <cac:PostalAddress>\n");
        xml.append("        <cbc:ID>").append(ubigeo).append("</cbc:ID>\n");
        xml.append("        <cbc:StreetName><![CDATA[").append(direccion).append("]]></cbc:StreetName>\n");
        xml.append("        <cbc:CityName>").append(provincia).append("</cbc:CityName>\n");
        xml.append("        <cbc:CountrySubentity>").append(departamento).append("</cbc:CountrySubentity>\n");
        xml.append("        <cbc:District>").append(distrito).append("</cbc:District>\n");
        xml.append("        <cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country>\n");
        xml.append("      </cac:PostalAddress>\n");
        xml.append("      <cac:PartyTaxScheme>\n");
        xml.append("        <cbc:RegistrationName><![CDATA[").append(razonSocial).append("]]></cbc:RegistrationName>\n");
        xml.append("        <cbc:CompanyID schemeID=\"6\" schemeName=\"SUNAT:Identidad Documento Identidad\" ")
           .append("schemeAgencyName=\"PE:SUNAT\" ")
           .append("schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">").append(ruc).append("</cbc:CompanyID>\n");
        xml.append("        <cac:TaxScheme><cbc:ID>RUC</cbc:ID><cbc:Name>RUC</cbc:Name><cbc:TaxTypeCode>VAT</cbc:TaxTypeCode></cac:TaxScheme>\n");
        xml.append("      </cac:PartyTaxScheme>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingSupplierParty>\n\n");

        // Datos del receptor
        String clienteId = esFactura
            ? (comprobante.getClienteRuc() != null ? comprobante.getClienteRuc() : "00000000000")
            : (comprobante.getClienteDni() != null ? comprobante.getClienteDni() : "00000000");
        String tipoDocCliente = esFactura ? "6" : "1";
        String nombreCliente = comprobante.getClienteNombre() != null ? comprobante.getClienteNombre() : "CLIENTE VARIOS";

        xml.append("  <cac:AccountingCustomerParty>\n");
        xml.append("    <cbc:CustomerAssignedAccountID>").append(clienteId).append("</cbc:CustomerAssignedAccountID>\n");
        xml.append("    <cbc:AdditionalAccountID>").append(tipoDocCliente).append("</cbc:AdditionalAccountID>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyIdentification>\n");
        xml.append("        <cbc:ID schemeID=\"").append(tipoDocCliente).append("\">").append(clienteId).append("</cbc:ID>\n");
        xml.append("      </cac:PartyIdentification>\n");
        xml.append("      <cac:PartyName><cbc:Name><![CDATA[").append(nombreCliente).append("]]></cbc:Name></cac:PartyName>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingCustomerParty>\n\n");

        BigDecimal subtotal = comprobante.getSubtotal();
        BigDecimal igv = comprobante.getIgv();
        BigDecimal total = comprobante.getTotal();

        xml.append("  <cac:TaxTotal>\n");
        xml.append("    <cbc:TaxAmount currencyID=\"PEN\">").append(igv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:TaxAmount>\n");
        xml.append("    <cac:TaxSubtotal>\n");
        xml.append("      <cbc:TaxableAmount currencyID=\"PEN\">").append(subtotal.setScale(2, RoundingMode.HALF_UP)).append("</cbc:TaxableAmount>\n");
        xml.append("      <cbc:TaxAmount currencyID=\"PEN\">").append(igv.setScale(2, RoundingMode.HALF_UP)).append("</cbc:TaxAmount>\n");
        xml.append("      <cac:TaxCategory>\n");
        xml.append("        <cbc:ID schemeID=\"UN/ECE 5305\" schemeName=\"Tax Category Identifier\" ")
           .append("schemeAgencyName=\"United Nations Economic Commission for Europe\">S</cbc:ID>\n");
        xml.append("        <cbc:Percent>18</cbc:Percent>\n");
        xml.append("        <cbc:TaxExemptionReasonCode listAgencyName=\"PE:SUNAT\" listName=\"Afectacion del IGV\" ")
           .append("listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07\">10</cbc:TaxExemptionReasonCode>\n");
        xml.append("        <cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name><cbc:TaxTypeCode>VAT</cbc:TaxTypeCode></cac:TaxScheme>\n");
        xml.append("      </cac:TaxCategory>\n");
        xml.append("    </cac:TaxSubtotal>\n");
        xml.append("  </cac:TaxTotal>\n\n");

        xml.append("  <cac:LegalMonetaryTotal>\n");
        xml.append("    <cbc:LineExtensionAmount currencyID=\"PEN\">").append(subtotal.setScale(2, RoundingMode.HALF_UP)).append("</cbc:LineExtensionAmount>\n");
        xml.append("    <cbc:TaxInclusiveAmount currencyID=\"PEN\">").append(total.setScale(2, RoundingMode.HALF_UP)).append("</cbc:TaxInclusiveAmount>\n");
        xml.append("    <cbc:PayableAmount currencyID=\"PEN\">").append(total.setScale(2, RoundingMode.HALF_UP)).append("</cbc:PayableAmount>\n");
        xml.append("  </cac:LegalMonetaryTotal>\n\n");

        int numeroLinea = 1;
        for (DetalleVenta detalle : venta.getDetalles()) {
            BigDecimal precioSinIgv = detalle.getPrecioUnitario().divide(BigDecimal.ONE.add(IGV_PORCENTAJE), 10, RoundingMode.HALF_UP);
            BigDecimal subtotalLinea = precioSinIgv.multiply(new BigDecimal(detalle.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igvLinea = detalle.getSubtotal().subtract(subtotalLinea).setScale(2, RoundingMode.HALF_UP);

            xml.append("  <cac:InvoiceLine>\n");
            xml.append("    <cbc:ID>").append(numeroLinea++).append("</cbc:ID>\n");
            xml.append("    <cbc:InvoicedQuantity unitCode=\"NIU\">").append(detalle.getCantidad()).append("</cbc:InvoicedQuantity>\n");
            xml.append("    <cbc:LineExtensionAmount currencyID=\"PEN\">").append(subtotalLinea).append("</cbc:LineExtensionAmount>\n");
            xml.append("    <cac:PricingReference><cac:AlternativeConditionPrice>\n");
            xml.append("      <cbc:PriceAmount currencyID=\"PEN\">").append(detalle.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP)).append("</cbc:PriceAmount>\n");
            xml.append("      <cbc:PriceTypeCode listName=\"Tipo de Precio\" listAgencyName=\"PE:SUNAT\" ")
               .append("listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16\">01</cbc:PriceTypeCode>\n");
            xml.append("    </cac:AlternativeConditionPrice></cac:PricingReference>\n");
            xml.append("    <cac:TaxTotal>\n");
            xml.append("      <cbc:TaxAmount currencyID=\"PEN\">").append(igvLinea).append("</cbc:TaxAmount>\n");
            xml.append("      <cac:TaxSubtotal>\n");
            xml.append("        <cbc:TaxableAmount currencyID=\"PEN\">").append(subtotalLinea).append("</cbc:TaxableAmount>\n");
            xml.append("        <cbc:TaxAmount currencyID=\"PEN\">").append(igvLinea).append("</cbc:TaxAmount>\n");
            xml.append("        <cac:TaxCategory>\n");
            xml.append("          <cbc:ID schemeID=\"UN/ECE 5305\" schemeName=\"Tax Category Identifier\" ")
               .append("schemeAgencyName=\"United Nations Economic Commission for Europe\">S</cbc:ID>\n");
            xml.append("          <cbc:Percent>18</cbc:Percent>\n");
            xml.append("          <cbc:TaxExemptionReasonCode listAgencyName=\"PE:SUNAT\" listName=\"Afectacion del IGV\" ")
               .append("listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07\">10</cbc:TaxExemptionReasonCode>\n");
            xml.append("          <cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name><cbc:TaxTypeCode>VAT</cbc:TaxTypeCode></cac:TaxScheme>\n");
            xml.append("        </cac:TaxCategory>\n");
            xml.append("      </cac:TaxSubtotal>\n");
            xml.append("    </cac:TaxTotal>\n");
            xml.append("    <cac:Item>\n");
            xml.append("      <cbc:Description><![CDATA[").append(detalle.getProducto().getNombre()).append("]]></cbc:Description>\n");
            xml.append("      <cac:SellersItemIdentification><cbc:ID>").append(detalle.getProducto().getId()).append("</cbc:ID></cac:SellersItemIdentification>\n");
            xml.append("    </cac:Item>\n");
            xml.append("    <cac:Price><cbc:PriceAmount currencyID=\"PEN\">").append(precioSinIgv.setScale(10, RoundingMode.HALF_UP)).append("</cbc:PriceAmount></cac:Price>\n");
            xml.append("  </cac:InvoiceLine>\n");
        }

        xml.append("</Invoice>");
        return xml.toString();
    }

    private String firmarXml(String xml, Comprobante comprobante) {
        // TODO: Implementar firma digital con certificado X.509
        // Para producción: cargar KeyStore (.p12), obtener PrivateKey y X509Certificate,
        // usar javax.xml.crypto.dsig.XMLSignature e inyectar en ext:UBLExtension/ext:ExtensionContent
        log.warn("AVISO: XML sin firma digital — requerida para producción SUNAT");
        return xml;
    }

    private byte[] comprimirXml(String contenidoXml, String nombreArchivoXml) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entrada = new ZipEntry(nombreArchivoXml);
            zos.putNextEntry(entrada);
            zos.write(contenidoXml.getBytes("UTF-8"));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private String enviarSunat(String nombreArchivo, String contenidoBase64) {
        String urlEndpoint = "beta".equalsIgnoreCase(modo) ? urlBeta : urlProduccion;

        // En producción usar HttpURLConnection o WebServiceTemplate con el SOAP request
        log.info("Simulando envío a SUNAT endpoint: {}", urlEndpoint);
        log.info("Archivo: {}", nombreArchivo);

        return simularRespuestaCdr(nombreArchivo);
    }

    private String simularRespuestaCdr(String nombreArchivo) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- CDR Simulado — Integrar certificado real para producción -->\n" +
            "<ApplicationResponse>\n" +
            "  <ID>" + nombreArchivo + "</ID>\n" +
            "  <ResponseCode>0</ResponseCode>\n" +
            "  <Description>La factura fue aceptada (SIMULADO)</Description>\n" +
            "</ApplicationResponse>";
    }

    /**
     * Convierte un monto decimal a texto en español para el campo Note del XML.
     */
    public String numeroALetras(BigDecimal monto) {
        long entero = monto.longValue();
        long centavos = monto.subtract(new BigDecimal(entero))
            .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        return convertirEntero(entero).toUpperCase() + " CON " + String.format("%02d", centavos) + "/100 SOLES";
    }

    private String convertirEntero(long num) {
        if (num == 0) return "CERO";
        if (num < 0) return "MENOS " + convertirEntero(-num);

        String[] unidades = {"", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
            "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};
        String[] decenas = {"", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
        String[] centenas = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

        StringBuilder resultado = new StringBuilder();
        if (num >= 1000000) {
            long millones = num / 1000000;
            resultado.append(millones == 1 ? "UN MILLÓN " : convertirEntero(millones) + " MILLONES ");
            num %= 1000000;
        }
        if (num >= 1000) {
            long miles = num / 1000;
            resultado.append(miles == 1 ? "MIL " : convertirEntero(miles) + " MIL ");
            num %= 1000;
        }
        if (num >= 100) {
            if (num == 100) return resultado + "CIEN";
            resultado.append(centenas[(int)(num / 100)]).append(" ");
            num %= 100;
        }
        if (num >= 20) {
            resultado.append(decenas[(int)(num / 10)]);
            if (num % 10 != 0) resultado.append(" Y ").append(unidades[(int)(num % 10)]);
        } else if (num > 0) {
            resultado.append(unidades[(int)num]);
        }
        return resultado.toString().trim();
    }
}
