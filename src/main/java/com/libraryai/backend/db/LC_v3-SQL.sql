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
    Estado VARCHAR(50) DEFAULT 'Activo'
);

CREATE TABLE Usuario (
    PK_UsuarioID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Correo VARCHAR(255) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
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
    Estado VARCHAR(50) DEFAULT 'Enviado',
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE
);

CREATE TABLE Rol (
    PK_RolID INT AUTO_INCREMENT PRIMARY KEY,
    NombreRol VARCHAR(100) NOT NULL
);

CREATE TABLE Permiso (
    PK_PermisoID INT AUTO_INCREMENT PRIMARY KEY,
    ClavePermiso VARCHAR(100) NOT NULL UNIQUE,
    Descripcion VARCHAR(255)
);

CREATE TABLE PlanSuscripcion (
    PK_PlanID INT AUTO_INCREMENT PRIMARY KEY,
    NombrePlan VARCHAR(100) NOT NULL,
    AlmacenamientoMaxMB INT,
    Precio DECIMAL(10, 2),
    Activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE TokenAcceso (
    PK_TokenID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    TipoToken VARCHAR(50),
    Token VARCHAR(500) NOT NULL,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FechaExpiracion DATETIME,
    Usado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE
);

CREATE TABLE Estanteria (
    PK_EstanteriaID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreCategoria VARCHAR(150),
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE
);

CREATE TABLE Suscripcion (
    PK_SuscripcionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_PlanID INT NOT NULL,
    FechaInicio DATETIME NOT NULL,
    FechaFin DATETIME,
    Estado VARCHAR(50),
    RenovacionAutomatica BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE,
    FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID)
);

CREATE TABLE ArchivoSubido (
    PK_ArchivoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    NombreArchivo VARCHAR(255),
    TipoArchivo VARCHAR(50),
    RutaAlmacenamiento VARCHAR(500),
    TamanoBytes INT,
    FechaSubida DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE
);


CREATE TABLE UsuarioRol (
    FK_UsuarioID INT,
    FK_RolID INT,
    PRIMARY KEY (FK_UsuarioID, FK_RolID),
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE,
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE CASCADE
);

CREATE TABLE RolPermiso (
    FK_RolID INT,
    FK_PermisoID INT,
    PRIMARY KEY (FK_RolID, FK_PermisoID),
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE CASCADE,
    FOREIGN KEY (FK_PermisoID) REFERENCES Permiso(PK_PermisoID) ON DELETE CASCADE
);

CREATE TABLE PlanRol (
    FK_PlanID INT,
    FK_RolID INT,
    PRIMARY KEY (FK_PlanID, FK_RolID),
    FOREIGN KEY (FK_PlanID) REFERENCES PlanSuscripcion(PK_PlanID) ON DELETE CASCADE,
    FOREIGN KEY (FK_RolID) REFERENCES Rol(PK_RolID) ON DELETE CASCADE
);

CREATE TABLE Pago (
    PK_PagoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_SuscripcionID INT NOT NULL,
    Pasarela VARCHAR(100),
    EstadoPago VARCHAR(50),
    ReferenciaExterna VARCHAR(255),
    Monto DECIMAL(10, 2),
    FechaPago DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_SuscripcionID) REFERENCES Suscripcion(PK_SuscripcionID) ON DELETE CASCADE
);

CREATE TABLE Relato (
    PK_RelatoID INT AUTO_INCREMENT PRIMARY KEY,
    FK_UsuarioID INT NOT NULL,
    FK_EstanteriaID INT,
    FK_ModeloUsadoID INT,
    Titulo VARCHAR(255),
    ModoOrigen VARCHAR(100),
    Descripcion TEXT,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion DATETIME,
    FOREIGN KEY (FK_UsuarioID) REFERENCES Usuario(PK_UsuarioID) ON DELETE CASCADE,
    FOREIGN KEY (FK_EstanteriaID) REFERENCES Estanteria(PK_EstanteriaID) ON DELETE SET NULL,
    FOREIGN KEY (FK_ModeloUsadoID) REFERENCES ModeloIA(PK_ModeloID) ON DELETE SET NULL
);

CREATE TABLE RelatoVersion (
    PK_VersionID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    NumeroVersion FLOAT,
    Contenido TEXT,
    Notas VARCHAR(500),
    EsPublicada BOOLEAN DEFAULT FALSE,
    FechaVersion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE CASCADE
);

CREATE TABLE ConfiguracionIA (
    PK_ConfigID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL UNIQUE,
    EstiloEscritura VARCHAR(100),
    NivelCreatividad VARCHAR(50),
    LongitudRespuesta VARCHAR(50),
    TonoEmocional VARCHAR(50),
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE CASCADE
);

CREATE TABLE MensajeChat (
    PK_MensajeID INT AUTO_INCREMENT PRIMARY KEY,
    FK_RelatoID INT NOT NULL,
    Emisor VARCHAR(50),
    ContenidoMensaje TEXT,
    FechaEnvio DATETIME DEFAULT CURRENT_TIMESTAMP,
    Orden INT,
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE CASCADE
);

CREATE TABLE Relato_ArchivoFuente (
    FK_RelatoID INT,
    FK_ArchivoID INT,
    PRIMARY KEY (FK_RelatoID, FK_ArchivoID),
    FOREIGN KEY (FK_RelatoID) REFERENCES Relato(PK_RelatoID) ON DELETE CASCADE,
    FOREIGN KEY (FK_ArchivoID) REFERENCES ArchivoSubido(PK_ArchivoID) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_correo ON Usuario(Correo);
CREATE INDEX idx_suscripcion_estado ON Suscripcion(Estado);
CREATE INDEX idx_relato_titulo ON Relato(Titulo);
CREATE INDEX idx_pago_referencia ON Pago(ReferenciaExterna);
CREATE INDEX idx_archivo_nombre ON ArchivoSubido(NombreArchivo);
CREATE INDEX idx_modelo_estado ON ModeloIA(Estado);
CREATE INDEX idx_fk_suscripcion_usuario ON Suscripcion(FK_UsuarioID);
CREATE INDEX idx_fk_relato_usuario ON Relato(FK_UsuarioID);
CREATE INDEX idx_fk_mensaje_relato ON MensajeChat(FK_RelatoID);
CREATE INDEX idx_fk_version_relato ON RelatoVersion(FK_RelatoID);

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
SELECT u.PK_UsuarioID, u.Nombre, u.Correo, r.NombreRol
FROM UsuarioRol ur
JOIN Usuario u ON ur.FK_UsuarioID = u.PK_UsuarioID
JOIN Rol r ON ur.FK_RolID = r.PK_RolID;

CREATE VIEW V_DetallePagos AS
SELECT pg.PK_PagoID, u.Nombre AS Cliente, pg.Monto, pg.FechaPago, pg.EstadoPago
FROM Pago pg
JOIN Suscripcion s ON pg.FK_SuscripcionID = s.PK_SuscripcionID
JOIN Usuario u ON s.FK_UsuarioID = u.PK_UsuarioID;

CREATE VIEW V_ArchivosPorRelato AS
SELECT r.Titulo AS Relato, a.NombreArchivo, a.TipoArchivo, a.TamanoBytes
FROM Relato_ArchivoFuente raf
JOIN Relato r ON raf.FK_RelatoID = r.PK_RelatoID
JOIN ArchivoSubido a ON raf.FK_ArchivoID = a.PK_ArchivoID;

CREATE VIEW V_EstadisticasSistema AS
SELECT 
    (SELECT COUNT(*) FROM Usuario WHERE Activo = TRUE) AS UsuariosActivos,
    (SELECT COUNT(*) FROM Suscripcion WHERE Estado = 'Activa' AND FK_PlanID = (SELECT PK_PlanID FROM PlanSuscripcion WHERE NombrePlan = 'Plan Premium')) AS UsuariosPremium,
    (SELECT COUNT(*) FROM Relato) AS TotalRelatosCreados;

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




