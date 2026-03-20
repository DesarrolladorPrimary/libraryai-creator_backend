USE LibraryAI_DB;

CREATE TEMPORARY TABLE tmp_suscripciones_activas_extra AS
SELECT PK_SuscripcionID
FROM (
    SELECT
        PK_SuscripcionID,
        ROW_NUMBER() OVER (
            PARTITION BY FK_UsuarioID
            ORDER BY FechaInicio DESC, PK_SuscripcionID DESC
        ) AS rn
    FROM Suscripcion
    WHERE Estado = 'Activa'
) duplicadas
WHERE rn > 1;

UPDATE Suscripcion s
JOIN tmp_suscripciones_activas_extra dup ON dup.PK_SuscripcionID = s.PK_SuscripcionID
SET s.Estado = 'Cancelada',
    s.FechaFin = COALESCE(s.FechaFin, CURRENT_TIMESTAMP);

DROP TEMPORARY TABLE tmp_suscripciones_activas_extra;

SET @col_usuario_activo_unico_existe := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Suscripcion'
      AND COLUMN_NAME = 'UsuarioActivoUnico'
);

SET @add_col_usuario_activo_unico_sql := IF(
    @col_usuario_activo_unico_existe = 0,
    'ALTER TABLE Suscripcion ADD COLUMN UsuarioActivoUnico INT GENERATED ALWAYS AS (CASE WHEN Estado = ''Activa'' THEN FK_UsuarioID ELSE NULL END) STORED AFTER Estado',
    'SELECT 1'
);

PREPARE add_col_usuario_activo_unico_stmt FROM @add_col_usuario_activo_unico_sql;
EXECUTE add_col_usuario_activo_unico_stmt;
DEALLOCATE PREPARE add_col_usuario_activo_unico_stmt;

SET @uk_suscripcion_activa_unica_existe := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Suscripcion'
      AND INDEX_NAME = 'uk_suscripcion_activa_unica'
);

SET @add_uk_suscripcion_activa_unica_sql := IF(
    @uk_suscripcion_activa_unica_existe = 0,
    'ALTER TABLE Suscripcion ADD UNIQUE INDEX uk_suscripcion_activa_unica (UsuarioActivoUnico)',
    'SELECT 1'
);

PREPARE add_uk_suscripcion_activa_unica_stmt FROM @add_uk_suscripcion_activa_unica_sql;
EXECUTE add_uk_suscripcion_activa_unica_stmt;
DEALLOCATE PREPARE add_uk_suscripcion_activa_unica_stmt;
