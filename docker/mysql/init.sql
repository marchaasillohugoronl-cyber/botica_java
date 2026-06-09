-- Script de inicializacion de base de datos para produccion
CREATE DATABASE IF NOT EXISTS boticadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON boticadb.* TO 'botica_user'@'%';
FLUSH PRIVILEGES;
