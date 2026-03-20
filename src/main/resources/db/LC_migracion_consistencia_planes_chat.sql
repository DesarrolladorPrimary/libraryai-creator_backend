USE LibraryAI_DB;

SET @codigo_plan_existe := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'PlanSuscripcion'
      AND COLUMN_NAME = 'CodigoPlan'
);

SET @add_codigo_plan_sql := IF(
    @codigo_plan_existe = 0,
    'ALTER TABLE PlanSuscripcion ADD COLUMN CodigoPlan VARCHAR(30) NULL AFTER PK_PlanID',
    'SELECT 1'
);

PREPARE add_codigo_plan_stmt FROM @add_codigo_plan_sql;
EXECUTE add_codigo_plan_stmt;
DEALLOCATE PREPARE add_codigo_plan_stmt;

UPDATE PlanSuscripcion
SET CodigoPlan = CASE
    WHEN LOWER(NombrePlan) LIKE '%premium%' THEN 'PREMIUM'
    WHEN LOWER(NombrePlan) LIKE '%gratuito%' OR LOWER(NombrePlan) LIKE '%free%'
         OR LOWER(NombrePlan) LIKE '%basic%' OR LOWER(NombrePlan) LIKE '%basico%'
         OR LOWER(NombrePlan) LIKE '%básico%' THEN 'GRATUITO'
    ELSE UPPER(REPLACE(REPLACE(TRIM(NombrePlan), ' ', '_'), '-', '_'))
END
WHERE CodigoPlan IS NULL OR CHAR_LENGTH(TRIM(CodigoPlan)) = 0;

SET @uk_codigo_plan_existe := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'PlanSuscripcion'
      AND INDEX_NAME = 'uk_plan_codigo'
);

SET @add_uk_codigo_plan_sql := IF(
    @uk_codigo_plan_existe = 0,
    'ALTER TABLE PlanSuscripcion ADD UNIQUE INDEX uk_plan_codigo (CodigoPlan)',
    'SELECT 1'
);

PREPARE add_uk_codigo_plan_stmt FROM @add_uk_codigo_plan_sql;
EXECUTE add_uk_codigo_plan_stmt;
DEALLOCATE PREPARE add_uk_codigo_plan_stmt;

ALTER TABLE PlanSuscripcion
    MODIFY CodigoPlan VARCHAR(30) NOT NULL;

ALTER TABLE UsuarioRol
    MODIFY FK_UsuarioID INT NOT NULL,
    MODIFY FK_RolID INT NOT NULL;

ALTER TABLE RolPermiso
    MODIFY FK_RolID INT NOT NULL,
    MODIFY FK_PermisoID INT NOT NULL;

ALTER TABLE PlanRol
    MODIFY FK_PlanID INT NOT NULL,
    MODIFY FK_RolID INT NOT NULL;

ALTER TABLE Relato_ArchivoFuente
    MODIFY FK_RelatoID INT NOT NULL,
    MODIFY FK_ArchivoID INT NOT NULL;

CREATE TEMPORARY TABLE tmp_mensaje_orden AS
SELECT
    PK_MensajeID,
    ROW_NUMBER() OVER (
        PARTITION BY FK_RelatoID
        ORDER BY COALESCE(Orden, 2147483647), FechaEnvio, PK_MensajeID
    ) AS NuevoOrden
FROM MensajeChat;

UPDATE MensajeChat mc
JOIN tmp_mensaje_orden tmp ON tmp.PK_MensajeID = mc.PK_MensajeID
SET mc.Orden = tmp.NuevoOrden;

DROP TEMPORARY TABLE tmp_mensaje_orden;

ALTER TABLE MensajeChat
    MODIFY Orden INT NOT NULL;

SET @uk_mensaje_orden_existe := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'MensajeChat'
      AND INDEX_NAME = 'uk_mensaje_relato_orden'
);

SET @add_uk_mensaje_orden_sql := IF(
    @uk_mensaje_orden_existe = 0,
    'ALTER TABLE MensajeChat ADD UNIQUE INDEX uk_mensaje_relato_orden (FK_RelatoID, Orden)',
    'SELECT 1'
);

PREPARE add_uk_mensaje_orden_stmt FROM @add_uk_mensaje_orden_sql;
EXECUTE add_uk_mensaje_orden_stmt;
DEALLOCATE PREPARE add_uk_mensaje_orden_stmt;

DROP VIEW IF EXISTS V_EstadisticasSistema;

CREATE VIEW V_EstadisticasSistema AS
SELECT 
    (SELECT COUNT(*) FROM Usuario WHERE Activo = TRUE) AS UsuariosActivos,
    (SELECT COUNT(*)
     FROM Suscripcion s
     JOIN PlanSuscripcion p ON p.PK_PlanID = s.FK_PlanID
     WHERE s.Estado = 'Activa' AND p.CodigoPlan = 'PREMIUM') AS UsuariosPremium,
    (SELECT COUNT(*) FROM Relato) AS TotalRelatosCreados,
    (SELECT COUNT(*)
     FROM MensajeChat
     WHERE Emisor = 'Usuario'
       AND MONTH(FechaEnvio) = MONTH(CURRENT_DATE())
       AND YEAR(FechaEnvio) = YEAR(CURRENT_DATE())) AS SolicitudesIAMesActual;
