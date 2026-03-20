-- Active: 1.769801523439e+12@@127.0.0.1@3306@mysql
CREATE DATABASE IF NOT EXISTS LibraryAI_DB;
USE LibraryAI_DB;

CREATE TABLE ModeloIA (
    PK_ModeloID INT AUTO_INCREMENT PRIMARY KEY,
    NombreModelo VARCHAR(100) NOT NULL,
    Version VARCHAR(50) NOT NULL,
    Descripcion TEXT,
    NotasVersion TEXT,
    FechaLanzamiento DATETIME,
    EsGratuito BOOLEAN DEFAULT FALSE,
    Estado ENUM('Activo', 'Inactivo', 'Obsoleto') DEFAULT 'Activo'
);

CREATE TABLE Usuario (
    PK_UsuarioID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Correo VARCHAR(255) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FotoPerfil VARCHAR(500),
    InstruccionPermanenteIA TEXT,
    Activo BOOLEAN DEFAULT TRUE,
    CorreoVerificado BOOLEAN DEFAULT FALSE,
    FechaVerificacion DATETIME,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Correo (
    PK_CorreoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    Asunto VARCHAR(255),
    Cuerpo TEXT,
    FechaEnvio DATETIME DEFAULT CURRENT_TIMESTAMP,
    Estado ENUM('Pendiente', 'Enviado', 'Fallido') DEFAULT 'Pendiente',
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT
);

CREATE TABLE Rol (
    PK_RolID INT AUTO_INCREMENT PRIMARY KEY,
    NombreRol VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Permiso (
    PK_PermisoID INT AUTO_INCREMENT PRIMARY KEY,
    ClavePermiso VARCHAR(100) NOT NULL UNIQUE,
    Descripcion VARCHAR(255)
);

CREATE TABLE PlanSuscripcion (
    PK_PlanID INT AUTO_INCREMENT PRIMARY KEY,
    CodigoPlan VARCHAR(30) NOT NULL UNIQUE,
    NombrePlan VARCHAR(100) NOT NULL UNIQUE,
    AlmacenamientoMaxMB BIGINT,
    Precio DECIMAL(10, 2) CHECK (Precio >= 0),
    Activo BOOLEAN DEFAULT TRUE,
    CHECK (CHAR_LENGTH(TRIM(CodigoPlan)) > 0),
    CHECK (CHAR_LENGTH(TRIM(NombrePlan)) > 0)
);

CREATE TABLE TokenAcceso (
    PK_TokenID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    TipoToken ENUM('Verificacion_Registro', 'Recuperacion_Password') NOT NULL,
    Token VARCHAR(255) NOT NULL UNIQUE,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FechaExpiracion DATETIME NOT NULL,
    Usado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT
);

CREATE TABLE Estanteria (
    PK_EstanteriaID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreCategoria VARCHAR(150) NOT NULL,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    UNIQUE (FK_UsuarioID, NombreCategoria),
    UNIQUE (PK_EstanteriaID, FK_UsuarioID),
    CHECK (CHAR_LENGTH(TRIM(NombreCategoria)) > 0)
);

CREATE TABLE Suscripcion (
    PK_SuscripcionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_PlanID INT NOT NULL,
    FechaInicio DATETIME NOT NULL,
    FechaFin DATETIME,
    Estado ENUM('Activa', 'Cancelada', 'Vencida') DEFAULT 'Activa',
    UsuarioActivoUnico INT GENERATED ALWAYS AS (
        CASE
            WHEN Estado = 'Activa' THEN FK_UsuarioID
            ELSE NULL
        END
    ) STORED,
    RenovacionAutomatica BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID),
    CHECK (FechaFin IS NULL OR FechaFin >= FechaInicio),
    UNIQUE (UsuarioActivoUnico)
);

CREATE TABLE ArchivoUsuario (
    PK_ArchivoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreArchivo VARCHAR(255) NOT NULL,
    TipoArchivo ENUM('PDF', 'DOC', 'DOCX') NOT NULL,
    Origen ENUM('Subido', 'Exportado') NOT NULL DEFAULT 'Subido',
    RutaAlmacenamiento TEXT NOT NULL,
    TamanoBytes BIGINT NOT NULL,
    FechaSubida DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CHECK (TamanoBytes > 0)
);

CREATE TABLE UsuarioRol (
    FK_UsuarioID INT NOT NULL,
    FK_RolID INT NOT NULL,
    PRIMARY KEY (FK_UsuarioID, FK_RolID),
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

CREATE TABLE RolPermiso (
    FK_RolID INT NOT NULL,
    FK_PermisoID INT NOT NULL,
    PRIMARY KEY (FK_RolID, FK_PermisoID),
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_PermisoID) REFERENCES Permiso(PK_PermisoID) ON DELETE RESTRICT
);

CREATE TABLE PlanRol (
    FK_PlanID INT NOT NULL,
    FK_RolID INT NOT NULL,
    PRIMARY KEY (FK_PlanID, FK_RolID),
    FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

CREATE TABLE Pago (
    PK_PagoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_SuscripcionID INT NOT NULL,
    Pasarela VARCHAR(100),
    EstadoPago ENUM('Pendiente', 'Completado', 'Fallido', 'Reembolsado') DEFAULT 'Pendiente',
    ReferenciaExterna VARCHAR(255),
    Monto DECIMAL(10, 2) CHECK (Monto >= 0),
    FechaPago DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_SuscripcionID) REFERENCES Suscripcion(PK_SuscripcionID) ON DELETE RESTRICT
);

CREATE TABLE Relato (
    PK_RelatoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_EstanteriaID INT,
    FK_ModeloUsadoID INT,
    Titulo VARCHAR(255) NOT NULL,
    ModoOrigen ENUM('Seccion_Artificial', 'Seccion_Creativa') NOT NULL,
    Descripcion TEXT,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_EstanteriaID, FK_UsuarioID) REFERENCES Estanteria(PK_EstanteriaID, FK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_ModeloUsadoID) REFERENCES ModeloIA(PK_ModeloID) ON DELETE SET NULL,
    CHECK (CHAR_LENGTH(TRIM(Titulo)) > 0)
);

CREATE TABLE RelatoVersion (
    PK_VersionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    NumeroVersion INT NOT NULL,
    Contenido TEXT,
    Notas TEXT,
    EsPublicada BOOLEAN DEFAULT FALSE,
    FechaVersion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    UNIQUE (FK_RelatoID, NumeroVersion)
);

CREATE TABLE ConfiguracionIA (
    PK_ConfigID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL UNIQUE,
    EstiloEscritura VARCHAR(100),
    NivelCreatividad ENUM('Bajo', 'Medio', 'Alto', 'Extremo') DEFAULT 'Medio',
    LongitudRespuesta ENUM('Corta', 'Media', 'Larga') DEFAULT 'Media',
    TonoEmocional VARCHAR(50),
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT
);

CREATE TABLE MensajeChat (
    PK_MensajeID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    Emisor ENUM('Usuario', 'Poly', 'Sistema') NOT NULL,
    ContenidoMensaje TEXT NOT NULL,
    FechaEnvio DATETIME DEFAULT CURRENT_TIMESTAMP,
    Orden INT NOT NULL,
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    UNIQUE (FK_RelatoID, Orden)
);

CREATE TABLE Relato_ArchivoFuente (
    FK_RelatoID INT NOT NULL,
    FK_ArchivoID INT NOT NULL,
    PRIMARY KEY (FK_RelatoID, FK_ArchivoID),
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_ArchivoID) REFERENCES ArchivoUsuario(PK_ArchivoID) ON DELETE RESTRICT
);

-- Tabla nueva para auditoria basica de cambio de roles
CREATE TABLE AuditoriaRolUsuario (
    PK_AuditoriaID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioAfectadoID INT NOT NULL,
    FK_AdminID INT NOT NULL,
    FK_RolAnteriorID INT,
    FK_RolNuevoID INT NOT NULL,
    RolAnterior VARCHAR(50),
    RolNuevo VARCHAR(50) NOT NULL,
    FechaCambio DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_UsuarioAfectadoID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_AdminID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_RolAnteriorID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT,
    FOREIGN KEY (FK_RolNuevoID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

-- Tablas nuevas para cumplir el RF_05 (Filtro NSFW)
CREATE TABLE PalabraProhibida (
    PK_PalabraID INT AUTO_INCREMENT PRIMARY KEY,
    Palabra VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE LogModeracion (
    PK_LogID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    Motivo VARCHAR(255) NOT NULL,
    ContenidoBloqueadoHash VARCHAR(255),
    Fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT
);

-- Indices para rendimiento
CREATE INDEX idx_usuario_correo ON Usuario(Correo);
CREATE INDEX idx_suscripcion_estado ON Suscripcion(Estado);
CREATE INDEX idx_relato_titulo ON Relato(Titulo);
CREATE INDEX idx_pago_referencia ON Pago(ReferenciaExterna);
CREATE INDEX idx_archivo_nombre ON ArchivoUsuario(NombreArchivo);
CREATE INDEX idx_modelo_estado ON ModeloIA(Estado);
CREATE INDEX idx_fk_suscripcion_usuario ON Suscripcion(FK_UsuarioID);
CREATE INDEX idx_fk_relato_usuario ON Relato(FK_UsuarioID);
CREATE INDEX idx_fk_mensaje_relato ON MensajeChat(FK_RelatoID);
CREATE INDEX idx_fk_version_relato ON RelatoVersion(FK_RelatoID);
CREATE INDEX idx_mensaje_fecha ON MensajeChat(FechaEnvio);
CREATE INDEX idx_token_unico ON TokenAcceso(Token);
CREATE INDEX idx_auditoria_usuario ON AuditoriaRolUsuario(FK_UsuarioAfectadoID);

-- Vistas
CREATE VIEW V_UsuarioSuscripcion AS
SELECT u.Nombre, u.Correo, s.FechaInicio, s.FechaFin, p.NombrePlan
FROM Suscripcion s
JOIN Usuario u ON s.FK_UsuarioID = u.PK_UsuarioID
JOIN PlanSuscripcion p ON s.FK_PlanID = p.PK_PlanID;

CREATE VIEW V_RelatosEnEstanteria AS
SELECT r.Titulo, r.ModoOrigen, e.NombreCategoria, u.Nombre AS Autor
FROM Relato r
JOIN Estanteria e ON r.FK_EstanteriaID = e.PK_EstanteriaID
JOIN Usuario u ON r.FK_UsuarioID = u.PK_UsuarioID;

CREATE VIEW V_RolesDeUsuario AS
SELECT u.PK_UsuarioID, u.Nombre, u.Correo, u.FotoPerfil, u.FechaRegistro, r.NombreRol
FROM UsuarioRol ur
JOIN Usuario u ON ur.FK_UsuarioID = u.PK_UsuarioID
JOIN Rol r ON ur.FK_RolID = r.PK_RolID;

CREATE VIEW V_DetallePagos AS
SELECT pg.PK_PagoID, u.Nombre AS Cliente, pg.Monto, pg.FechaPago, pg.EstadoPago
FROM Pago pg
JOIN Suscripcion s ON pg.FK_SuscripcionID = s.PK_SuscripcionID
JOIN Usuario u ON s.FK_UsuarioID = u.PK_UsuarioID;

CREATE VIEW V_ArchivosPorRelato AS
SELECT r.Titulo AS Relato, a.NombreArchivo, a.TipoArchivo, a.TamanoBytes, a.Origen
FROM Relato_ArchivoFuente raf
JOIN Relato r ON raf.FK_RelatoID = r.PK_RelatoID
JOIN ArchivoUsuario a ON raf.FK_ArchivoID = a.PK_ArchivoID;

CREATE VIEW V_EstadisticasSistema AS
SELECT 
    (SELECT COUNT(*) FROM Usuario WHERE Activo = TRUE) AS UsuariosActivos,
    (SELECT COUNT(*) FROM Suscripcion WHERE Estado = 'Activa' AND FK_PlanID = (SELECT PK_PlanID FROM PlanSuscripcion WHERE CodigoPlan = 'PREMIUM')) AS UsuariosPremium,
    (SELECT COUNT(*) FROM Relato) AS TotalRelatosCreados,
    (SELECT COUNT(*) FROM MensajeChat WHERE Emisor = 'Usuario' AND MONTH(FechaEnvio) = MONTH(CURRENT_DATE()) AND YEAR(FechaEnvio) = YEAR(CURRENT_DATE())) AS SolicitudesIAMesActual;

CREATE VIEW V_DetalleRelatos AS
SELECT 
    r.Titulo, 
    r.ModoOrigen, 
    u.Nombre AS Autor, 
    e.NombreCategoria,
    m.NombreModelo AS ModeloUtilizado,
    m.Version AS VersionModelo
FROM Relato r
JOIN Usuario u ON r.FK_UsuarioID = u.PK_UsuarioID
LEFT JOIN Estanteria e ON r.FK_EstanteriaID = e.PK_EstanteriaID
LEFT JOIN ModeloIA m ON r.FK_ModeloUsadoID = m.PK_ModeloID;
