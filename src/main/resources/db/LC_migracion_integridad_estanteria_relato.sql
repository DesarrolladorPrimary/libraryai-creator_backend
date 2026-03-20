USE LibraryAI_DB;

-- Normaliza datos viejos antes de imponer restricciones nuevas.
UPDATE Estanteria
SET NombreCategoria = CONCAT('Estantería ', PK_EstanteriaID)
WHERE NombreCategoria IS NULL OR CHAR_LENGTH(TRIM(NombreCategoria)) = 0;

UPDATE Relato
SET Titulo = CONCAT('Relato ', PK_RelatoID)
WHERE Titulo IS NULL OR CHAR_LENGTH(TRIM(Titulo)) = 0;

-- Si un relato quedó apuntando a una estantería ajena, se libera esa relación.
UPDATE Relato r
LEFT JOIN Estanteria e
       ON e.PK_EstanteriaID = r.FK_EstanteriaID
      AND e.FK_UsuarioID = r.FK_UsuarioID
SET r.FK_EstanteriaID = NULL
WHERE r.FK_EstanteriaID IS NOT NULL
  AND e.PK_EstanteriaID IS NULL;

SET @idx_estanteria_usuario_existe := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Estanteria'
      AND INDEX_NAME = 'uk_estanteria_id_usuario'
);

SET @add_idx_estanteria_usuario_sql := IF(
    @idx_estanteria_usuario_existe = 0,
    'ALTER TABLE Estanteria ADD UNIQUE INDEX uk_estanteria_id_usuario (PK_EstanteriaID, FK_UsuarioID)',
    'SELECT 1'
);

PREPARE add_idx_estanteria_usuario_stmt FROM @add_idx_estanteria_usuario_sql;
EXECUTE add_idx_estanteria_usuario_stmt;
DEALLOCATE PREPARE add_idx_estanteria_usuario_stmt;

SET @chk_estanteria_nombre_existe := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Estanteria'
      AND CONSTRAINT_NAME = 'chk_estanteria_nombre_no_vacio'
);

SET @add_chk_estanteria_nombre_sql := IF(
    @chk_estanteria_nombre_existe = 0,
    'ALTER TABLE Estanteria ADD CONSTRAINT chk_estanteria_nombre_no_vacio CHECK (CHAR_LENGTH(TRIM(NombreCategoria)) > 0)',
    'SELECT 1'
);

PREPARE add_chk_estanteria_nombre_stmt FROM @add_chk_estanteria_nombre_sql;
EXECUTE add_chk_estanteria_nombre_stmt;
DEALLOCATE PREPARE add_chk_estanteria_nombre_stmt;

ALTER TABLE Relato
    MODIFY Titulo VARCHAR(255) NOT NULL,
    MODIFY FechaModificacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

SET @chk_relato_titulo_existe := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Relato'
      AND CONSTRAINT_NAME = 'chk_relato_titulo_no_vacio'
);

SET @add_chk_relato_titulo_sql := IF(
    @chk_relato_titulo_existe = 0,
    'ALTER TABLE Relato ADD CONSTRAINT chk_relato_titulo_no_vacio CHECK (CHAR_LENGTH(TRIM(Titulo)) > 0)',
    'SELECT 1'
);

PREPARE add_chk_relato_titulo_stmt FROM @add_chk_relato_titulo_sql;
EXECUTE add_chk_relato_titulo_stmt;
DEALLOCATE PREPARE add_chk_relato_titulo_stmt;

SET @fk_relato_estanteria := (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Relato'
      AND COLUMN_NAME = 'FK_EstanteriaID'
      AND REFERENCED_TABLE_NAME = 'Estanteria'
    LIMIT 1
);

SET @drop_fk_sql := IF(
    @fk_relato_estanteria IS NOT NULL,
    CONCAT('ALTER TABLE Relato DROP FOREIGN KEY `', @fk_relato_estanteria, '`'),
    'SELECT 1'
);

PREPARE drop_fk_stmt FROM @drop_fk_sql;
EXECUTE drop_fk_stmt;
DEALLOCATE PREPARE drop_fk_stmt;

SET @fk_relato_estanteria_usuario_existe := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Relato'
      AND CONSTRAINT_NAME = 'fk_relato_estanteria_usuario'
);

SET @add_fk_relato_estanteria_usuario_sql := IF(
    @fk_relato_estanteria_usuario_existe = 0,
    'ALTER TABLE Relato ADD CONSTRAINT fk_relato_estanteria_usuario FOREIGN KEY (FK_EstanteriaID, FK_UsuarioID) REFERENCES Estanteria(PK_EstanteriaID, FK_UsuarioID) ON DELETE RESTRICT',
    'SELECT 1'
);

PREPARE add_fk_relato_estanteria_usuario_stmt FROM @add_fk_relato_estanteria_usuario_sql;
EXECUTE add_fk_relato_estanteria_usuario_stmt;
DEALLOCATE PREPARE add_fk_relato_estanteria_usuario_stmt;
