-- Ejecutar en SQL Server Management Studio 21 conectado como administrador (sa o Windows)
-- Base de datos: Spring_AI_DataBase  |  Usuario de la aplicación: Lenguajes

IF DB_ID(N'Spring_AI_DataBase') IS NULL
    CREATE DATABASE Spring_AI_DataBase;
GO

USE Spring_AI_DataBase;
GO

-- Si el usuario "Lenguajes" no tiene permisos sobre la base, descomentar:
-- CREATE USER Lenguajes FOR LOGIN Lenguajes;
-- ALTER ROLE db_owner ADD MEMBER Lenguajes;
-- GO
