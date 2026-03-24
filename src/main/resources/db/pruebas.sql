-- Archivo de consultas de apoyo para pruebas y validaciones manuales.
-- Úsalo por bloques según lo que necesites revisar en la demo o en QA.

USE LibraryAI_DB;

-- =========================================================
-- 1. USUARIOS
-- =========================================================

-- Lista básica de usuarios.
SELECT
    PK_UsuarioID,
    Nombre,
    Correo,
    Activo,
    CorreoVerificado,
    FechaRegistro
FROM Usuario
ORDER BY PK_UsuarioID;

-- Usuarios con su rol actual.
SELECT
    u.PK_UsuarioID,
    u.Nombre,
    u.Correo,
    r.NombreRol
FROM Usuario u
LEFT JOIN UsuarioRol ur ON ur.FK_UsuarioID = u.PK_UsuarioID
LEFT JOIN Rol r ON r.PK_RolID = ur.FK_RolID
ORDER BY u.PK_UsuarioID, r.NombreRol;

-- Usuarios sin rol asignado.
SELECT
    u.PK_UsuarioID,
    u.Nombre,
    u.Correo
FROM Usuario u
LEFT JOIN UsuarioRol ur ON ur.FK_UsuarioID = u.PK_UsuarioID
WHERE ur.FK_UsuarioID IS NULL
ORDER BY u.PK_UsuarioID;

-- Correos transaccionales registrados.
SELECT
    c.PK_CorreoID,
    u.Correo AS Usuario,
    c.TipoCorreo,
    c.Destinatario,
    c.Asunto,
    c.Estado,
    c.ErrorDetalle,
    c.FechaEnvio
FROM Correo c
JOIN Usuario u ON u.PK_UsuarioID = c.FK_UsuarioID
ORDER BY c.PK_CorreoID;

-- =========================================================
-- 2. PLANES Y SUSCRIPCIONES
-- =========================================================

-- Catálogo de planes.
SELECT
    PK_PlanID,
    CodigoPlan,
    NombrePlan,
    AlmacenamientoMaxMB,
    Precio,
    Activo
FROM PlanSuscripcion
ORDER BY PK_PlanID;

-- Suscripción actual de cada usuario.
SELECT
    u.PK_UsuarioID,
    u.Correo,
    p.CodigoPlan,
    p.NombrePlan,
    s.Estado,
    s.FechaInicio,
    s.FechaFin,
    s.RenovacionAutomatica
FROM Usuario u
LEFT JOIN Suscripcion s
       ON s.FK_UsuarioID = u.PK_UsuarioID
      AND s.Estado = 'Activa'
LEFT JOIN PlanSuscripcion p ON p.PK_PlanID = s.FK_PlanID
ORDER BY u.PK_UsuarioID;

-- Historial de suscripciones.
SELECT
    s.PK_SuscripcionID,
    u.Correo,
    p.NombrePlan,
    s.Estado,
    s.FechaInicio,
    s.FechaFin
FROM Suscripcion s
JOIN Usuario u ON u.PK_UsuarioID = s.FK_UsuarioID
JOIN PlanSuscripcion p ON p.PK_PlanID = s.FK_PlanID
ORDER BY s.PK_SuscripcionID;

-- Pagos registrados.
SELECT
    pg.PK_PagoID,
    u.Correo,
    p.NombrePlan,
    pg.Monto,
    pg.EstadoPago,
    pg.ReferenciaExterna,
    pg.FechaPago
FROM Pago pg
JOIN Suscripcion s ON s.PK_SuscripcionID = pg.FK_SuscripcionID
JOIN Usuario u ON u.PK_UsuarioID = s.FK_UsuarioID
JOIN PlanSuscripcion p ON p.PK_PlanID = s.FK_PlanID
ORDER BY pg.PK_PagoID;

-- Usuarios con más de una suscripción activa (debería devolver 0 filas).
SELECT
    FK_UsuarioID,
    COUNT(*) AS SuscripcionesActivas
FROM Suscripcion
WHERE Estado = 'Activa'
GROUP BY FK_UsuarioID
HAVING COUNT(*) > 1;

-- =========================================================
-- 3. ESTANTERÍAS Y RELATOS
-- =========================================================

-- Estanterías creadas por usuario.
SELECT
    e.PK_EstanteriaID,
    u.Correo,
    e.NombreCategoria
FROM Estanteria e
JOIN Usuario u ON u.PK_UsuarioID = e.FK_UsuarioID
ORDER BY e.PK_EstanteriaID;

-- Relatos con autor, estantería y modelo usado.
SELECT
    r.PK_RelatoID,
    r.Titulo,
    r.ModoOrigen,
    u.Correo AS Autor,
    e.NombreCategoria AS Estanteria,
    m.NombreModelo AS Modelo,
    r.FechaCreacion,
    r.FechaModificacion
FROM Relato r
JOIN Usuario u ON u.PK_UsuarioID = r.FK_UsuarioID
LEFT JOIN Estanteria e
       ON e.PK_EstanteriaID = r.FK_EstanteriaID
      AND e.FK_UsuarioID = r.FK_UsuarioID
LEFT JOIN ModeloIA m ON m.PK_ModeloID = r.FK_ModeloUsadoID
ORDER BY r.PK_RelatoID;

-- Relatos sin estantería asignada.
SELECT
    r.PK_RelatoID,
    r.Titulo,
    u.Correo AS Autor
FROM Relato r
JOIN Usuario u ON u.PK_UsuarioID = r.FK_UsuarioID
WHERE r.FK_EstanteriaID IS NULL
ORDER BY r.PK_RelatoID;

-- Validación de integridad: relatos vinculados a estantería de otro usuario.
-- Debería devolver 0 filas.
SELECT
    r.PK_RelatoID,
    r.Titulo,
    r.FK_UsuarioID AS UsuarioRelato,
    e.FK_UsuarioID AS UsuarioEstanteria
FROM Relato r
JOIN Estanteria e ON e.PK_EstanteriaID = r.FK_EstanteriaID
WHERE r.FK_EstanteriaID IS NOT NULL
  AND r.FK_UsuarioID <> e.FK_UsuarioID;

-- =========================================================
-- 4. VERSIONES DE RELATO
-- =========================================================

-- Historial de versiones por relato.
SELECT
    rv.PK_VersionID,
    rv.FK_RelatoID,
    r.Titulo,
    rv.NumeroVersion,
    rv.EsPublicada,
    rv.FechaVersion,
    rv.Notas
FROM RelatoVersion rv
JOIN Relato r ON r.PK_RelatoID = rv.FK_RelatoID
ORDER BY rv.FK_RelatoID, rv.NumeroVersion;

-- Última versión registrada por relato.
SELECT
    rv.FK_RelatoID,
    r.Titulo,
    MAX(rv.NumeroVersion) AS UltimaVersion
FROM RelatoVersion rv
JOIN Relato r ON r.PK_RelatoID = rv.FK_RelatoID
GROUP BY rv.FK_RelatoID, r.Titulo
ORDER BY rv.FK_RelatoID;

-- =========================================================
-- 5. CHAT Y CONFIGURACIÓN IA
-- =========================================================

-- Configuración IA por relato.
SELECT
    c.PK_ConfigID,
    r.Titulo,
    c.EstiloEscritura,
    c.NivelCreatividad,
    c.LongitudRespuesta,
    c.TonoEmocional
FROM ConfiguracionIA c
JOIN Relato r ON r.PK_RelatoID = c.FK_RelatoID
ORDER BY c.PK_ConfigID;

-- Conversación completa ordenada por relato.
SELECT
    mc.PK_MensajeID,
    mc.FK_RelatoID,
    r.Titulo,
    mc.Orden,
    mc.Emisor,
    mc.FechaEnvio,
    mc.ContenidoMensaje
FROM MensajeChat mc
JOIN Relato r ON r.PK_RelatoID = mc.FK_RelatoID
ORDER BY mc.FK_RelatoID, mc.Orden;

-- Validación de integridad: órdenes repetidos por relato.
-- Debería devolver 0 filas.
SELECT
    FK_RelatoID,
    Orden,
    COUNT(*) AS Repetidos
FROM MensajeChat
GROUP BY FK_RelatoID, Orden
HAVING COUNT(*) > 1;

-- =========================================================
-- 6. ARCHIVOS DEL USUARIO
-- =========================================================

-- Archivos registrados por usuario.
SELECT
    a.PK_ArchivoID,
    u.Correo,
    a.NombreArchivo,
    a.TipoArchivo,
    a.Origen,
    a.TamanoBytes,
    a.FechaSubida
FROM ArchivoUsuario a
JOIN Usuario u ON u.PK_UsuarioID = a.FK_UsuarioID
ORDER BY a.PK_ArchivoID;

-- Archivos vinculados a relatos.
SELECT
    raf.FK_RelatoID,
    r.Titulo,
    a.PK_ArchivoID,
    a.NombreArchivo,
    a.Origen
FROM Relato_ArchivoFuente raf
JOIN Relato r ON r.PK_RelatoID = raf.FK_RelatoID
JOIN ArchivoUsuario a ON a.PK_ArchivoID = raf.FK_ArchivoID
ORDER BY raf.FK_RelatoID, a.PK_ArchivoID;

-- Consumo de espacio por usuario.
SELECT
    u.PK_UsuarioID,
    u.Correo,
    COALESCE(SUM(a.TamanoBytes), 0) AS BytesUsados
FROM Usuario u
LEFT JOIN ArchivoUsuario a ON a.FK_UsuarioID = u.PK_UsuarioID
GROUP BY u.PK_UsuarioID, u.Correo
ORDER BY u.PK_UsuarioID;

-- =========================================================
-- 7. MODERACIÓN Y AUDITORÍA
-- =========================================================

-- Palabras prohibidas configuradas.
SELECT
    PK_PalabraID,
    Palabra
FROM PalabraProhibida
ORDER BY PK_PalabraID;

-- Logs de moderación.
SELECT
    lm.PK_LogID,
    u.Correo,
    pp.Palabra,
    lm.Motivo,
    lm.ContenidoBloqueadoHash,
    lm.Fecha
FROM LogModeracion lm
JOIN Usuario u ON u.PK_UsuarioID = lm.FK_UsuarioID
LEFT JOIN PalabraProhibida pp ON pp.PK_PalabraID = lm.FK_PalabraID
ORDER BY lm.PK_LogID DESC;

-- Auditoría de cambios de rol.
SELECT
    a.PK_AuditoriaID,
    ua.Correo AS UsuarioAfectado,
    ad.Correo AS AdminQueCambio,
    a.RolAnterior,
    a.RolNuevo,
    a.FechaCambio
FROM AuditoriaRolUsuario a
JOIN Usuario ua ON ua.PK_UsuarioID = a.FK_UsuarioAfectadoID
JOIN Usuario ad ON ad.PK_UsuarioID = a.FK_AdminID
ORDER BY a.PK_AuditoriaID DESC;

-- =========================================================
-- 8. VISTAS DEL SISTEMA
-- =========================================================

SELECT * FROM V_UsuarioSuscripcion;
SELECT * FROM V_RelatosEnEstanteria;
SELECT * FROM V_RolesDeUsuario;
SELECT * FROM V_DetallePagos;
SELECT * FROM V_ArchivosPorRelato;
SELECT * FROM V_EstadisticasSistema;
SELECT * FROM V_DetalleRelatos;

-- =========================================================
-- 9. RESUMEN RÁPIDO
-- =========================================================

SELECT 'Usuarios' AS Tabla, COUNT(*) AS Total FROM Usuario
UNION ALL
SELECT 'Estanterias', COUNT(*) FROM Estanteria
UNION ALL
SELECT 'Relatos', COUNT(*) FROM Relato
UNION ALL
SELECT 'RelatoVersion', COUNT(*) FROM RelatoVersion
UNION ALL
SELECT 'MensajeChat', COUNT(*) FROM MensajeChat
UNION ALL
SELECT 'ArchivoUsuario', COUNT(*) FROM ArchivoUsuario
UNION ALL
SELECT 'Suscripcion', COUNT(*) FROM Suscripcion
UNION ALL
SELECT 'Pago', COUNT(*) FROM Pago
UNION ALL
SELECT 'LogModeracion', COUNT(*) FROM LogModeracion
UNION ALL
SELECT 'AuditoriaRolUsuario', COUNT(*) FROM AuditoriaRolUsuario;
