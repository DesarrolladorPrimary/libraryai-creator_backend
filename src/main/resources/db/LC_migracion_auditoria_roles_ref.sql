USE LibraryAI_DB;

SET @col_rol_anterior_id_existe := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'AuditoriaRolUsuario'
      AND COLUMN_NAME = 'FK_RolAnteriorID'
);

SET @add_col_rol_anterior_id_sql := IF(
    @col_rol_anterior_id_existe = 0,
    'ALTER TABLE AuditoriaRolUsuario ADD COLUMN FK_RolAnteriorID INT NULL AFTER FK_AdminID',
    'SELECT 1'
);

PREPARE add_col_rol_anterior_id_stmt FROM @add_col_rol_anterior_id_sql;
EXECUTE add_col_rol_anterior_id_stmt;
DEALLOCATE PREPARE add_col_rol_anterior_id_stmt;

SET @col_rol_nuevo_id_existe := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'AuditoriaRolUsuario'
      AND COLUMN_NAME = 'FK_RolNuevoID'
);

SET @add_col_rol_nuevo_id_sql := IF(
    @col_rol_nuevo_id_existe = 0,
    'ALTER TABLE AuditoriaRolUsuario ADD COLUMN FK_RolNuevoID INT NULL AFTER FK_RolAnteriorID',
    'SELECT 1'
);

PREPARE add_col_rol_nuevo_id_stmt FROM @add_col_rol_nuevo_id_sql;
EXECUTE add_col_rol_nuevo_id_stmt;
DEALLOCATE PREPARE add_col_rol_nuevo_id_stmt;

UPDATE AuditoriaRolUsuario aru
LEFT JOIN Rol rol_anterior ON rol_anterior.NombreRol = aru.RolAnterior
LEFT JOIN Rol rol_nuevo ON rol_nuevo.NombreRol = aru.RolNuevo
SET aru.FK_RolAnteriorID = rol_anterior.PK_RolID,
    aru.FK_RolNuevoID = rol_nuevo.PK_RolID
WHERE aru.FK_RolAnteriorID IS NULL OR aru.FK_RolNuevoID IS NULL;

ALTER TABLE AuditoriaRolUsuario
    MODIFY FK_RolNuevoID INT NOT NULL;

SET @fk_auditoria_rol_anterior_existe := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'AuditoriaRolUsuario'
      AND CONSTRAINT_NAME = 'fk_auditoria_rol_anterior'
);

SET @add_fk_auditoria_rol_anterior_sql := IF(
    @fk_auditoria_rol_anterior_existe = 0,
    'ALTER TABLE AuditoriaRolUsuario ADD CONSTRAINT fk_auditoria_rol_anterior FOREIGN KEY (FK_RolAnteriorID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT',
    'SELECT 1'
);

PREPARE add_fk_auditoria_rol_anterior_stmt FROM @add_fk_auditoria_rol_anterior_sql;
EXECUTE add_fk_auditoria_rol_anterior_stmt;
DEALLOCATE PREPARE add_fk_auditoria_rol_anterior_stmt;

SET @fk_auditoria_rol_nuevo_existe := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'AuditoriaRolUsuario'
      AND CONSTRAINT_NAME = 'fk_auditoria_rol_nuevo'
);

SET @add_fk_auditoria_rol_nuevo_sql := IF(
    @fk_auditoria_rol_nuevo_existe = 0,
    'ALTER TABLE AuditoriaRolUsuario ADD CONSTRAINT fk_auditoria_rol_nuevo FOREIGN KEY (FK_RolNuevoID) REFERENCES Rol(PK_RolID) ON DELETE RESTRICT',
    'SELECT 1'
);

PREPARE add_fk_auditoria_rol_nuevo_stmt FROM @add_fk_auditoria_rol_nuevo_sql;
EXECUTE add_fk_auditoria_rol_nuevo_stmt;
DEALLOCATE PREPARE add_fk_auditoria_rol_nuevo_stmt;
