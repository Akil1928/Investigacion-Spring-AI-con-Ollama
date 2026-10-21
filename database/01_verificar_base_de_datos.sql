-- Ejecutar en SSMS 21 conectado como administrador (sa o Windows)
-- Crea la base Spring_AI_DataBase y el usuario Lenguajes para la aplicación.

IF DB_ID(N'Spring_AI_DataBase') IS NULL
    CREATE DATABASE Spring_AI_DataBase;
GO

-- Login a nivel de servidor
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'Lenguajes')
    CREATE LOGIN Lenguajes WITH PASSWORD = 'MI_CLAVE', CHECK_POLICY = OFF;
GO

USE Spring_AI_DataBase;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'Lenguajes')
    CREATE USER Lenguajes FOR LOGIN Lenguajes;
GO

ALTER ROLE db_owner ADD MEMBER Lenguajes;
GO