USE LibraryAI_DB;

SET @db_name = DATABASE();

SET @has_tipo_correo = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'Correo'
      AND COLUMN_NAME = 'TipoCorreo'
);

SET @add_tipo_correo_sql = IF(
    @has_tipo_correo = 0,
    "ALTER TABLE Correo ADD COLUMN TipoCorreo ENUM('Verificacion', 'Recuperacion', 'Notificacion') NOT NULL DEFAULT 'Notificacion' AFTER FK_UsuarioID",
    "SELECT 'TipoCorreo ya existe' AS info"
);
PREPARE stmt FROM @add_tipo_correo_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_destinatario = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'Correo'
      AND COLUMN_NAME = 'Destinatario'
);

SET @add_destinatario_sql = IF(
    @has_destinatario = 0,
    "ALTER TABLE Correo ADD COLUMN Destinatario VARCHAR(255) NULL AFTER TipoCorreo",
    "SELECT 'Destinatario ya existe' AS info"
);
PREPARE stmt FROM @add_destinatario_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE Correo c
INNER JOIN Usuario u ON u.PK_UsuarioID = c.FK_UsuarioID
SET c.Destinatario = u.Correo
WHERE c.Destinatario IS NULL OR TRIM(c.Destinatario) = '';

UPDATE Correo
SET Asunto = 'Correo del sistema'
WHERE Asunto IS NULL OR TRIM(Asunto) = '';

UPDATE Correo
SET Cuerpo = 'Registro historico de correo migrado.'
WHERE Cuerpo IS NULL OR TRIM(Cuerpo) = '';

ALTER TABLE Correo
    MODIFY COLUMN Destinatario VARCHAR(255) NOT NULL,
    MODIFY COLUMN Asunto VARCHAR(255) NOT NULL,
    MODIFY COLUMN Cuerpo TEXT NOT NULL;

SET @has_error_detalle = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'Correo'
      AND COLUMN_NAME = 'ErrorDetalle'
);

SET @add_error_detalle_sql = IF(
    @has_error_detalle = 0,
    "ALTER TABLE Correo ADD COLUMN ErrorDetalle VARCHAR(500) NULL AFTER Estado",
    "SELECT 'ErrorDetalle ya existe' AS info"
);
PREPARE stmt FROM @add_error_detalle_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_correo_usuario_fecha = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'Correo'
      AND INDEX_NAME = 'idx_correo_usuario_fecha'
);

SET @create_idx_correo_usuario_fecha_sql = IF(
    @has_idx_correo_usuario_fecha = 0,
    "CREATE INDEX idx_correo_usuario_fecha ON Correo(FK_UsuarioID, FechaEnvio)",
    "SELECT 'idx_correo_usuario_fecha ya existe' AS info"
);
PREPARE stmt FROM @create_idx_correo_usuario_fecha_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_correo_tipo_estado = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'Correo'
      AND INDEX_NAME = 'idx_correo_tipo_estado'
);

SET @create_idx_correo_tipo_estado_sql = IF(
    @has_idx_correo_tipo_estado = 0,
    "CREATE INDEX idx_correo_tipo_estado ON Correo(TipoCorreo, Estado)",
    "SELECT 'idx_correo_tipo_estado ya existe' AS info"
);
PREPARE stmt FROM @create_idx_correo_tipo_estado_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
