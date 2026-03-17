USE LibraryAI_DB;

DROP VIEW IF EXISTS V_ArchivosPorRelato;

SET @tabla_vieja_existe := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'ArchivoSubido'
);

SET @tabla_nueva_existe := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'ArchivoUsuario'
);

SET @rename_sql := IF(
    @tabla_vieja_existe > 0 AND @tabla_nueva_existe = 0,
    'RENAME TABLE ArchivoSubido TO ArchivoUsuario',
    'SELECT 1'
);

PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

CREATE VIEW V_ArchivosPorRelato AS
SELECT r.Titulo AS Relato, a.NombreArchivo, a.TipoArchivo, a.TamanoBytes, a.Origen
FROM Relato_ArchivoFuente raf
JOIN Relato r ON raf.FK_RelatoID = r.PK_RelatoID
JOIN ArchivoUsuario a ON raf.FK_ArchivoID = a.PK_ArchivoID;
