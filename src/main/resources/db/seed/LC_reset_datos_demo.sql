-- Reinicia los datos de LibraryAI_DB para pruebas
-- Objetivo:
-- - vaciar las tablas transaccionales y catálogos usados por la app
-- - reiniciar AUTO_INCREMENT para que los IDs vuelvan a comenzar desde 1
-- - dejar la base lista para ejecutar después seed/LC_demo_seed_completo.sql

USE LibraryAI_DB;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE Correo;
TRUNCATE TABLE TokenAcceso;
TRUNCATE TABLE Pago;
TRUNCATE TABLE Suscripcion;
TRUNCATE TABLE UsuarioRol;
TRUNCATE TABLE RolPermiso;
TRUNCATE TABLE PlanRol;
TRUNCATE TABLE AuditoriaRolUsuario;
TRUNCATE TABLE LogModeracion;
TRUNCATE TABLE MensajeChat;
TRUNCATE TABLE ConfiguracionIA;
TRUNCATE TABLE RelatoVersion;
TRUNCATE TABLE Relato_ArchivoFuente;
TRUNCATE TABLE Relato_Estanteria;
TRUNCATE TABLE ArchivoUsuario;
TRUNCATE TABLE Relato;
TRUNCATE TABLE Estanteria;
TRUNCATE TABLE PalabraProhibida;
TRUNCATE TABLE ModeloIA;
TRUNCATE TABLE Plan;
TRUNCATE TABLE Permiso;
TRUNCATE TABLE Rol;
TRUNCATE TABLE Usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- Después de ejecutar este archivo, corre:
-- seed/LC_demo_seed_completo.sql
