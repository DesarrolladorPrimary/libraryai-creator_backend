DROP DATABASE IF EXISTS LibraryAI_DB;
CREATE DATABASE LibraryAI_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE LibraryAI_DB;

CREATE TABLE ModeloIA (
    PK_ModeloID INT AUTO_INCREMENT PRIMARY KEY,
    NombreModelo VARCHAR(100) NOT NULL,
    Version VARCHAR(50) NOT NULL,
    Descripcion TEXT,
    NotasVersion TEXT,
    FechaLanzamiento DATETIME,
    EsGratuito BOOLEAN NOT NULL DEFAULT FALSE,
    Estado ENUM('Activo', 'Inactivo', 'Obsoleto') NOT NULL DEFAULT 'Activo',
    CONSTRAINT UQ_ModeloIA_Nombre_Version UNIQUE (NombreModelo, Version)
);

CREATE TABLE Usuario (
    PK_UsuarioID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Correo VARCHAR(255) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FotoPerfil VARCHAR(500),
    InstruccionPermanenteIA TEXT,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,
    CorreoVerificado BOOLEAN NOT NULL DEFAULT FALSE,
    FechaVerificacion DATETIME,
    FechaRegistro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Correo (
    PK_CorreoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    TipoCorreo ENUM('Verificacion', 'Recuperacion', 'Notificacion') NOT NULL DEFAULT 'Notificacion',
    Destinatario VARCHAR(255) NOT NULL,
    Asunto VARCHAR(255) NOT NULL,
    Cuerpo TEXT NOT NULL,
    FechaEnvio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Estado ENUM('Pendiente', 'Enviado', 'Fallido') NOT NULL DEFAULT 'Pendiente',
    ErrorDetalle VARCHAR(500),
    CONSTRAINT FK_Correo_Usuario
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
    Precio DECIMAL(10, 2) NOT NULL DEFAULT 0,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT CHK_PlanSuscripcion_Precio CHECK (Precio >= 0),
    CONSTRAINT CHK_PlanSuscripcion_Almacenamiento CHECK (AlmacenamientoMaxMB IS NULL OR AlmacenamientoMaxMB >= 0)
);

CREATE TABLE TokenAcceso (
    PK_TokenID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    TipoToken ENUM('Verificacion_Registro', 'Recuperacion_Password') NOT NULL,
    Token VARCHAR(255) NOT NULL UNIQUE,
    FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FechaExpiracion DATETIME NOT NULL,
    Usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT FK_TokenAcceso_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT CHK_TokenAcceso_Fechas CHECK (FechaExpiracion >= FechaCreacion)
);

CREATE TABLE Estanteria (
    PK_EstanteriaID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreCategoria VARCHAR(150) NOT NULL,
    CONSTRAINT FK_Estanteria_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT UQ_Estanteria_Usuario_Categoria UNIQUE (FK_UsuarioID, NombreCategoria),
    CONSTRAINT UQ_Estanteria_ID_Usuario UNIQUE (PK_EstanteriaID, FK_UsuarioID)
);

CREATE TABLE Suscripcion (
    PK_SuscripcionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_PlanID INT NOT NULL,
    FechaInicio DATETIME NOT NULL,
    FechaFin DATETIME,
    Estado ENUM('Activa', 'Cancelada', 'Vencida') NOT NULL DEFAULT 'Activa',
    RenovacionAutomatica BOOLEAN NOT NULL DEFAULT FALSE,
    UsuarioActivoUnico INT GENERATED ALWAYS AS (
        CASE WHEN Estado = 'Activa' THEN FK_UsuarioID ELSE NULL END
    ) STORED,
    CONSTRAINT FK_Suscripcion_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_Suscripcion_Plan
        FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID) ON DELETE RESTRICT,
    CONSTRAINT CHK_Suscripcion_Fechas CHECK (FechaFin IS NULL OR FechaFin >= FechaInicio),
    CONSTRAINT UQ_Suscripcion_UsuarioActivo UNIQUE (UsuarioActivoUnico)
);

CREATE TABLE ArchivoUsuario (
    PK_ArchivoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreArchivo VARCHAR(255) NOT NULL,
    TipoArchivo ENUM('PDF', 'DOC', 'DOCX', 'TXT', 'RTF', 'EPUB', 'JPG', 'JPEG', 'PNG') NOT NULL,
    Origen ENUM('Subido', 'Exportado') NOT NULL DEFAULT 'Subido',
    RutaAlmacenamiento VARCHAR(500) NOT NULL,
    TamanoBytes BIGINT NOT NULL,
    FechaSubida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_ArchivoUsuario_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT CHK_ArchivoUsuario_Tamano CHECK (TamanoBytes > 0)
);

CREATE TABLE UsuarioRol (
    FK_UsuarioID INT NOT NULL,
    FK_RolID INT NOT NULL,
    PRIMARY KEY (FK_UsuarioID, FK_RolID),
    CONSTRAINT FK_UsuarioRol_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_UsuarioRol_Rol
        FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

CREATE TABLE RolPermiso (
    FK_RolID INT NOT NULL,
    FK_PermisoID INT NOT NULL,
    PRIMARY KEY (FK_RolID, FK_PermisoID),
    CONSTRAINT FK_RolPermiso_Rol
        FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT,
    CONSTRAINT FK_RolPermiso_Permiso
        FOREIGN KEY (FK_PermisoID) REFERENCES Permiso(PK_PermisoID) ON DELETE RESTRICT
);

CREATE TABLE PlanRol (
    FK_PlanID INT NOT NULL,
    FK_RolID INT NOT NULL,
    PRIMARY KEY (FK_PlanID, FK_RolID),
    CONSTRAINT FK_PlanRol_Plan
        FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID) ON DELETE RESTRICT,
    CONSTRAINT FK_PlanRol_Rol
        FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

CREATE TABLE Pago (
    PK_PagoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_SuscripcionID INT NOT NULL,
    Pasarela ENUM('Stripe', 'PayPal', 'MercadoPago', 'Otra') NOT NULL DEFAULT 'Otra',
    EstadoPago ENUM('Pendiente', 'Completado', 'Fallido', 'Reembolsado') NOT NULL DEFAULT 'Pendiente',
    ReferenciaExterna VARCHAR(255) UNIQUE,
    Monto DECIMAL(10, 2) NOT NULL,
    FechaPago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Pago_Suscripcion
        FOREIGN KEY (FK_SuscripcionID) REFERENCES Suscripcion(PK_SuscripcionID) ON DELETE RESTRICT,
    CONSTRAINT CHK_Pago_Monto CHECK (Monto >= 0)
);

CREATE TABLE Relato (
    PK_RelatoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_EstanteriaID INT NULL,
    FK_ModeloUsadoID INT NULL,
    Titulo VARCHAR(255) NOT NULL,
    ModoOrigen ENUM('Seccion_Artificial', 'Seccion_Creativa') NOT NULL,
    Descripcion TEXT,
    FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion DATETIME,
    CONSTRAINT FK_Relato_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_Relato_Estanteria_Usuario
        FOREIGN KEY (FK_EstanteriaID, FK_UsuarioID)
        REFERENCES Estanteria(PK_EstanteriaID, FK_UsuarioID),
    CONSTRAINT FK_Relato_ModeloIA
        FOREIGN KEY (FK_ModeloUsadoID) REFERENCES ModeloIA(PK_ModeloID) ON DELETE SET NULL,
    CONSTRAINT CHK_Relato_Fechas CHECK (FechaModificacion IS NULL OR FechaModificacion >= FechaCreacion)
);

CREATE TABLE RelatoVersion (
    PK_VersionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    NumeroVersion INT NOT NULL,
    Contenido TEXT NOT NULL,
    Notas TEXT,
    EsPublicada BOOLEAN NOT NULL DEFAULT FALSE,
    FechaVersion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_RelatoVersion_Relato
        FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    CONSTRAINT UQ_RelatoVersion_Relato_Numero UNIQUE (FK_RelatoID, NumeroVersion),
    CONSTRAINT CHK_RelatoVersion_Numero CHECK (NumeroVersion > 0)
);

CREATE TABLE ConfiguracionIA (
    PK_ConfigID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL UNIQUE,
    EstiloEscritura VARCHAR(100),
    NivelCreatividad ENUM('Bajo', 'Medio', 'Alto', 'Extremo') NOT NULL DEFAULT 'Medio',
    LongitudRespuesta ENUM('Corta', 'Media', 'Larga') NOT NULL DEFAULT 'Media',
    TonoEmocional ENUM('Neutral', 'Alegre', 'Triste', 'Dramatico', 'Misterioso', 'Inspirador', 'Tenso') DEFAULT 'Neutral',
    CONSTRAINT FK_ConfiguracionIA_Relato
        FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT
);

CREATE TABLE MensajeChat (
    PK_MensajeID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    Emisor ENUM('Usuario', 'Poly', 'Sistema') NOT NULL,
    ContenidoMensaje TEXT NOT NULL,
    FechaEnvio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Orden INT NOT NULL,
    CONSTRAINT FK_MensajeChat_Relato
        FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    CONSTRAINT UQ_MensajeChat_Relato_Orden UNIQUE (FK_RelatoID, Orden),
    CONSTRAINT CHK_MensajeChat_Orden CHECK (Orden > 0)
);

CREATE TABLE Relato_ArchivoFuente (
    FK_RelatoID INT NOT NULL,
    FK_ArchivoID INT NOT NULL,
    PRIMARY KEY (FK_RelatoID, FK_ArchivoID),
    CONSTRAINT FK_RelatoArchivoFuente_Relato
        FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE RESTRICT,
    CONSTRAINT FK_RelatoArchivoFuente_Archivo
        FOREIGN KEY (FK_ArchivoID) REFERENCES ArchivoUsuario(PK_ArchivoID) ON DELETE RESTRICT
);

CREATE TABLE AuditoriaRolUsuario (
    PK_AuditoriaID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioAfectadoID INT NOT NULL,
    FK_AdminID INT NOT NULL,
    FK_RolAnteriorID INT NULL,
    FK_RolNuevoID INT NOT NULL,
    FechaCambio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_AuditoriaRolUsuario_UsuarioAfectado
        FOREIGN KEY (FK_UsuarioAfectadoID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_AuditoriaRolUsuario_Admin
        FOREIGN KEY (FK_AdminID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_AuditoriaRolUsuario_RolAnterior
        FOREIGN KEY (FK_RolAnteriorID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT,
    CONSTRAINT FK_AuditoriaRolUsuario_RolNuevo
        FOREIGN KEY (FK_RolNuevoID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT
);

CREATE TABLE PalabraProhibida (
    PK_PalabraID INT AUTO_INCREMENT PRIMARY KEY,
    Palabra VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE LogModeracion (
    PK_LogID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_PalabraID INT NULL,
    Motivo VARCHAR(255) NOT NULL,
    ContenidoBloqueadoHash VARCHAR(255),
    Fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LogModeracion_Usuario
        FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE RESTRICT,
    CONSTRAINT FK_LogModeracion_Palabra
        FOREIGN KEY (FK_PalabraID) REFERENCES PalabraProhibida(PK_PalabraID) ON DELETE SET NULL
);

CREATE INDEX idx_suscripcion_estado ON Suscripcion(Estado);
CREATE INDEX idx_relato_titulo ON Relato(Titulo);
CREATE INDEX idx_pago_referencia ON Pago(ReferenciaExterna);
CREATE INDEX idx_archivo_nombre ON ArchivoUsuario(NombreArchivo);
CREATE INDEX idx_modelo_estado ON ModeloIA(Estado);
CREATE INDEX idx_fk_suscripcion_usuario ON Suscripcion(FK_UsuarioID);
CREATE INDEX idx_fk_relato_usuario ON Relato(FK_UsuarioID);
CREATE INDEX idx_fk_relato_estanteria ON Relato(FK_EstanteriaID);
CREATE INDEX idx_fk_relato_modelo ON Relato(FK_ModeloUsadoID);
CREATE INDEX idx_fk_mensaje_relato ON MensajeChat(FK_RelatoID);
CREATE INDEX idx_fk_version_relato ON RelatoVersion(FK_RelatoID);
CREATE INDEX idx_mensaje_fecha ON MensajeChat(FechaEnvio);
CREATE INDEX idx_fk_token_usuario ON TokenAcceso(FK_UsuarioID);
CREATE INDEX idx_fk_correo_usuario ON Correo(FK_UsuarioID);
CREATE INDEX idx_correo_usuario_fecha ON Correo(FK_UsuarioID, FechaEnvio);
CREATE INDEX idx_correo_tipo_estado ON Correo(TipoCorreo, Estado);
CREATE INDEX idx_fk_archivo_usuario ON ArchivoUsuario(FK_UsuarioID);
CREATE INDEX idx_auditoria_usuario ON AuditoriaRolUsuario(FK_UsuarioAfectadoID);
CREATE INDEX idx_logmoderacion_usuario ON LogModeracion(FK_UsuarioID);

CREATE VIEW V_UsuarioSuscripcion AS
SELECT u.Nombre, u.Correo, s.FechaInicio, s.FechaFin, s.Estado, p.NombrePlan
FROM Suscripcion s
JOIN Usuario u ON s.FK_UsuarioID = u.PK_UsuarioID
JOIN PlanSuscripcion p ON s.FK_PlanID = p.PK_PlanID;

CREATE VIEW V_RelatosEnEstanteria AS
SELECT r.Titulo, r.ModoOrigen, e.NombreCategoria, u.Nombre AS Autor
FROM Relato r
JOIN Estanteria e
  ON r.FK_EstanteriaID = e.PK_EstanteriaID
 AND r.FK_UsuarioID = e.FK_UsuarioID
JOIN Usuario u ON r.FK_UsuarioID = u.PK_UsuarioID;

CREATE VIEW V_RolesDeUsuario AS
SELECT u.PK_UsuarioID, u.Nombre, u.Correo, u.FotoPerfil, u.FechaRegistro, r.NombreRol
FROM UsuarioRol ur
JOIN Usuario u ON ur.FK_UsuarioID = u.PK_UsuarioID
JOIN Rol r ON ur.FK_RolID = r.PK_RolID;

CREATE VIEW V_DetallePagos AS
SELECT pg.PK_PagoID, u.Nombre AS Cliente, pg.Monto, pg.FechaPago, pg.EstadoPago, pg.ReferenciaExterna
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
    (SELECT COUNT(*) FROM Suscripcion WHERE Estado = 'Activa') AS SuscripcionesActivas,
    (SELECT COUNT(*) FROM Relato) AS TotalRelatosCreados,
    (SELECT COUNT(*) 
       FROM MensajeChat 
      WHERE Emisor = 'Usuario' 
        AND MONTH(FechaEnvio) = MONTH(CURRENT_DATE()) 
        AND YEAR(FechaEnvio) = YEAR(CURRENT_DATE())) AS SolicitudesIAMesActual;

CREATE VIEW V_DetalleRelatos AS
SELECT 
    r.PK_RelatoID,
    r.Titulo, 
    r.ModoOrigen, 
    u.Nombre AS Autor, 
    e.NombreCategoria,
    m.NombreModelo AS ModeloUtilizado,
    m.Version AS VersionModelo,
    r.FechaCreacion,
    r.FechaModificacion
FROM Relato r
JOIN Usuario u ON r.FK_UsuarioID = u.PK_UsuarioID
LEFT JOIN Estanteria e 
  ON r.FK_EstanteriaID = e.PK_EstanteriaID
 AND r.FK_UsuarioID = e.FK_UsuarioID
LEFT JOIN ModeloIA m ON r.FK_ModeloUsadoID = m.PK_ModeloID;
