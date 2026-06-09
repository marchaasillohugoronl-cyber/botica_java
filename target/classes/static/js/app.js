// Resaltar enlace activo en navbar
document.addEventListener('DOMContentLoaded', function () {
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && path.startsWith(href) && href !== '/') {
            link.classList.add('active');
        }
    });

    // Auto-cerrar alertas después de 4 segundos
    document.querySelectorAll('.alert.alert-success, .alert.alert-info').forEach(el => {
        setTimeout(() => {
            el.classList.add('fade');
            setTimeout(() => el.remove(), 300);
        }, 4000);
    });
});
