-- Seed demo determinista para LibraryAI_DB
-- Uso recomendado:
-- 1. Ejecuta primero seed/LC_reset_datos_demo.sql si quieres reiniciar IDs desde 1.
-- 2. Luego ejecuta este seed para cargar datos base de prueba.
--
-- Qué deja cargado:
-- - roles y planes demo
-- - relación plan/rol para los planes de usuario
-- - 2 modelos IA (gratuito y premium)
-- - blacklist base de moderación
-- - 1 admin demo y 4 usuarios demo
-- - correos transaccionales demo
-- - roles, suscripciones y pagos simulados para usuarios finales
-- - estanterías globales
-- - relatos, versiones, configuración IA y chats
-- - un log de moderación y una auditoría de rol para probar vistas admin
--
-- Credenciales demo:
-- - demo.admin@example.com / Demo123!
-- - demo.ana@example.com / Demo123!
-- - demo.bruno@example.com / Demo123!
-- - demo.carla@example.com / Demo123!
-- - demo.diego@example.com / Demo123!

USE LibraryAI_DB;

START TRANSACTION;

SET @demo_hash = '$2a$10$Ka5IDG6s7g2jGVsnMkXzJ.Mf57hx5CjabKB7uXNaj9LnF7/QyXR2W';

INSERT IGNORE INTO Rol (NombreRol)
VALUES ('Admin'), ('Gratuito'), ('Premium');

INSERT INTO Plan (CodigoPlan, NombrePlan, AlmacenamientoMaxMB, Precio, ColorHex, Activo)
SELECT 'GRATUITO', 'Plan Gratuito', 500, 0.00, '#4ECDC4', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM Plan WHERE CodigoPlan = 'GRATUITO'
);

INSERT INTO Plan (CodigoPlan, NombrePlan, AlmacenamientoMaxMB, Precio, ColorHex, Activo)
SELECT 'PREMIUM', 'Plan Premium', 2048, 9.99, '#FFD700', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM Plan WHERE CodigoPlan = 'PREMIUM'
);

INSERT INTO ModeloIA (
    NombreModelo, Version, Descripcion, NotasVersion, FechaLanzamiento, EsGratuito, Estado
)
SELECT 'gemini-2.5-flash', '2.5-flash',
       'Modelo base disponible para todos los usuarios.',
       'Modelo recomendado para el plan Gratuito.',
       CURRENT_TIMESTAMP, TRUE, 'Activo'
WHERE NOT EXISTS (
    SELECT 1 FROM ModeloIA WHERE LOWER(NombreModelo) = LOWER('gemini-2.5-flash')
);

INSERT INTO ModeloIA (
    NombreModelo, Version, Descripcion, NotasVersion, FechaLanzamiento, EsGratuito, Estado
)
SELECT 'gemini-2.5-pro', '2.5-pro',
       'Modelo avanzado habilitado para usuarios Premium.',
       'Modelo preferente para el plan Premium.',
       CURRENT_TIMESTAMP, FALSE, 'Activo'
WHERE NOT EXISTS (
    SELECT 1 FROM ModeloIA WHERE LOWER(NombreModelo) = LOWER('gemini-2.5-pro')
);

INSERT IGNORE INTO PalabraProhibida (Palabra)
VALUES
    ('pornografia'),
    ('porno'),
    ('contenido adulto'),
    ('xxx'),
    ('sexo'),
    ('sexo explicito'),
    ('erotico'),
    ('erotica'),
    ('violacion'),
    ('incesto'),
    ('zoofilia'),
    ('pedofilia');

INSERT INTO Usuario (
    Nombre,
    Correo,
    PasswordHash,
    InstruccionPermanenteIA,
    Activo,
    CorreoVerificado,
    FechaVerificacion,
    FechaRegistro
)
SELECT
    seed.nombre,
    seed.correo,
    @demo_hash,
    seed.instruccion,
    TRUE,
    TRUE,
    NOW(),
    seed.fecha_registro
FROM (
    SELECT 'Admin Demo' AS nombre, 'demo.admin@example.com' AS correo,
           NULL AS instruccion,
           NOW() - INTERVAL 30 DAY AS fecha_registro
    UNION ALL
    SELECT 'Ana Demo', 'demo.ana@example.com',
           'Prefiero fantasía suave, personajes humanos y atmósfera.',
           NOW() - INTERVAL 20 DAY
    UNION ALL
    SELECT 'Bruno Demo', 'demo.bruno@example.com',
           'Quiero textos directos con aventura juvenil y buen ritmo.',
           NOW() - INTERVAL 18 DAY
    UNION ALL
    SELECT 'Carla Demo', 'demo.carla@example.com',
           'Mantén una prosa elegante con descripciones un poco más amplias.',
           NOW() - INTERVAL 12 DAY
    UNION ALL
    SELECT 'Diego Demo', 'demo.diego@example.com',
           'Quiero ciencia ficción con misterio y tensión progresiva.',
           NOW() - INTERVAL 10 DAY
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM Usuario u WHERE u.Correo = seed.correo
);

SET @free_plan_id = (
    SELECT PK_PlanID FROM Plan WHERE CodigoPlan = 'GRATUITO' LIMIT 1
);
SET @premium_plan_id = (
    SELECT PK_PlanID FROM Plan WHERE CodigoPlan = 'PREMIUM' LIMIT 1
);
SET @flash_model_id = (
    SELECT PK_ModeloID FROM ModeloIA
    WHERE LOWER(NombreModelo) = LOWER('gemini-2.5-flash')
    LIMIT 1
);
SET @pro_model_id = (
    SELECT PK_ModeloID FROM ModeloIA
    WHERE LOWER(NombreModelo) = LOWER('gemini-2.5-pro')
    LIMIT 1
);

UPDATE Plan
SET ColorHex = '#4ECDC4',
    FK_ModeloPreferidoID = @flash_model_id
WHERE CodigoPlan = 'GRATUITO';

UPDATE Plan
SET ColorHex = '#FFD700',
    FK_ModeloPreferidoID = @pro_model_id
WHERE CodigoPlan = 'PREMIUM';

SET @demo_admin_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.admin@example.com' LIMIT 1);
SET @demo_ana_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.ana@example.com' LIMIT 1);
SET @demo_bruno_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.bruno@example.com' LIMIT 1);
SET @demo_carla_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.carla@example.com' LIMIT 1);
SET @demo_diego_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.diego@example.com' LIMIT 1);

INSERT INTO Correo (
    FK_UsuarioID, TipoCorreo, Destinatario, Asunto, Cuerpo, Estado, ErrorDetalle, FechaEnvio
)
SELECT
    data.user_id,
    data.tipo_correo,
    data.destinatario,
    data.asunto,
    data.cuerpo,
    data.estado,
    data.error_detalle,
    data.fecha_envio
FROM (
    SELECT
        @demo_ana_id AS user_id,
        'Verificacion' AS tipo_correo,
        'demo.ana@example.com' AS destinatario,
        'Verifica tu cuenta en LibraryAI' AS asunto,
        'Tu cuenta fue verificada correctamente en el entorno demo.' AS cuerpo,
        'Enviado' AS estado,
        NULL AS error_detalle,
        NOW() - INTERVAL 20 DAY AS fecha_envio
    UNION ALL
    SELECT
        @demo_bruno_id,
        'Recuperacion',
        'demo.bruno@example.com',
        'Recupera tu contraseña',
        'Se generó una solicitud de recuperación en el entorno demo.',
        'Fallido',
        'Entrega simulada fallida por sandbox de correo.',
        NOW() - INTERVAL 14 DAY
    UNION ALL
    SELECT
        @demo_carla_id,
        'Notificacion',
        'demo.carla@example.com',
        'Tu plan Premium fue activado',
        'La suscripción Premium quedó activa para la cuenta demo.',
        'Enviado',
        NULL,
        NOW() - INTERVAL 11 DAY
    UNION ALL
    SELECT
        @demo_diego_id,
        'Notificacion',
        'demo.diego@example.com',
        'Tu biblioteca recibió una exportación',
        'Se registró una exportación demo en la biblioteca del usuario.',
        'Enviado',
        NULL,
        NOW() - INTERVAL 7 DAY
) data
WHERE data.user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM Correo c
      WHERE c.FK_UsuarioID = data.user_id
        AND c.TipoCorreo = data.tipo_correo
        AND c.Asunto = data.asunto
  );

INSERT INTO PlanRol (FK_PlanID, FK_RolID)
SELECT p.PK_PlanID, r.PK_RolID
FROM Plan p
JOIN Rol r ON r.NombreRol = 'Gratuito'
WHERE p.CodigoPlan = 'GRATUITO'
AND NOT EXISTS (
    SELECT 1
    FROM PlanRol pr
    WHERE pr.FK_PlanID = p.PK_PlanID
      AND pr.FK_RolID = r.PK_RolID
);

INSERT INTO PlanRol (FK_PlanID, FK_RolID)
SELECT p.PK_PlanID, r.PK_RolID
FROM Plan p
JOIN Rol r ON r.NombreRol = 'Premium'
WHERE p.CodigoPlan = 'PREMIUM'
AND NOT EXISTS (
    SELECT 1
    FROM PlanRol pr
    WHERE pr.FK_PlanID = p.PK_PlanID
      AND pr.FK_RolID = r.PK_RolID
);

INSERT INTO UsuarioRol (FK_UsuarioID, FK_RolID)
SELECT u.PK_UsuarioID, r.PK_RolID
FROM Usuario u
JOIN Rol r ON r.NombreRol = 'Admin'
WHERE u.Correo = 'demo.admin@example.com'
AND NOT EXISTS (
    SELECT 1 FROM UsuarioRol ur
    WHERE ur.FK_UsuarioID = u.PK_UsuarioID
      AND ur.FK_RolID = r.PK_RolID
);

INSERT INTO UsuarioRol (FK_UsuarioID, FK_RolID)
SELECT u.PK_UsuarioID, r.PK_RolID
FROM Usuario u
JOIN Rol r ON r.NombreRol = 'Gratuito'
WHERE u.Correo IN ('demo.ana@example.com', 'demo.bruno@example.com')
AND NOT EXISTS (
    SELECT 1 FROM UsuarioRol ur
    WHERE ur.FK_UsuarioID = u.PK_UsuarioID
      AND ur.FK_RolID = r.PK_RolID
);

INSERT INTO UsuarioRol (FK_UsuarioID, FK_RolID)
SELECT u.PK_UsuarioID, r.PK_RolID
FROM Usuario u
JOIN Rol r ON r.NombreRol = 'Premium'
WHERE u.Correo IN ('demo.carla@example.com', 'demo.diego@example.com')
AND NOT EXISTS (
    SELECT 1 FROM UsuarioRol ur
    WHERE ur.FK_UsuarioID = u.PK_UsuarioID
      AND ur.FK_RolID = r.PK_RolID
);

INSERT INTO Suscripcion (
    FK_UsuarioID, FK_PlanID, FechaInicio, FechaFin, Estado, RenovacionAutomatica
)
SELECT user_id, plan_id, fecha_inicio, NULL, 'Activa', renovacion
FROM (
    SELECT @demo_ana_id AS user_id, @free_plan_id AS plan_id, NOW() - INTERVAL 20 DAY AS fecha_inicio, FALSE AS renovacion
    UNION ALL
    SELECT @demo_bruno_id, @free_plan_id, NOW() - INTERVAL 18 DAY, FALSE
    UNION ALL
    SELECT @demo_carla_id, @premium_plan_id, NOW() - INTERVAL 12 DAY, TRUE
    UNION ALL
    SELECT @demo_diego_id, @premium_plan_id, NOW() - INTERVAL 10 DAY, TRUE
) seed
WHERE user_id IS NOT NULL
  AND plan_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM Suscripcion s
      WHERE s.FK_UsuarioID = seed.user_id
        AND s.Estado = 'Activa'
  );

INSERT INTO Pago (
    FK_SuscripcionID, Pasarela, EstadoPago, ReferenciaExterna, Monto, FechaPago
)
SELECT s.PK_SuscripcionID, 'Otra', 'Completado', ref.referencia, 9.99, ref.fecha_pago
FROM (
    SELECT 'demo.carla@example.com' AS correo, 'DEMO-CARLA-001' AS referencia, NOW() - INTERVAL 11 DAY AS fecha_pago
    UNION ALL
    SELECT 'demo.diego@example.com', 'DEMO-DIEGO-001', NOW() - INTERVAL 9 DAY
) ref
JOIN Usuario u ON u.Correo = ref.correo
JOIN Suscripcion s ON s.FK_UsuarioID = u.PK_UsuarioID AND s.Estado = 'Activa'
WHERE NOT EXISTS (
    SELECT 1 FROM Pago p WHERE p.ReferenciaExterna = ref.referencia
);

INSERT INTO Estanteria (NombreCategoria)
SELECT seed.nombre_categoria
FROM (
    SELECT 'Fantasía' AS nombre_categoria
    UNION ALL
    SELECT 'Borradores'
    UNION ALL
    SELECT 'Aventura'
    UNION ALL
    SELECT 'Premium Drafts'
    UNION ALL
    SELECT 'Publicados'
    UNION ALL
    SELECT 'Sci-Fi'
) seed
WHERE NOT EXISTS (
      SELECT 1 FROM Estanteria e
      WHERE e.NombreCategoria = seed.nombre_categoria
  );

INSERT INTO Relato (
    FK_UsuarioID, FK_ModeloUsadoID,
    Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
)
SELECT
    data.user_id,
    data.model_id,
    data.titulo,
    data.modo_origen,
    data.descripcion,
    data.fecha_creacion,
    data.fecha_modificacion
FROM (
    SELECT @demo_ana_id AS user_id, 'Fantasía' AS estanteria, @flash_model_id AS model_id,
           'El faro de sal' AS titulo, 'Seccion_Artificial' AS modo_origen,
           'Una cartógrafa encuentra un faro antiguo cuyo resplandor revela rutas que no existen en ningún mapa.' AS descripcion,
           NOW() - INTERVAL 18 DAY AS fecha_creacion, NOW() - INTERVAL 18 DAY AS fecha_modificacion
    UNION ALL
    SELECT @demo_ana_id, 'Borradores', NULL,
           'Cuaderno del bosque gris', 'Seccion_Creativa',
           'Notas de una aprendiz de herborista que descubre voces antiguas entre los árboles.',
           NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 14 DAY
    UNION ALL
    SELECT @demo_bruno_id, 'Aventura', NULL,
           'La brújula sin norte', 'Seccion_Creativa',
           'Un joven explorador hereda una brújula que señala secretos familiares en lugar del norte.',
           NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 12 DAY
    UNION ALL
    SELECT @demo_carla_id, 'Premium Drafts', @pro_model_id,
           'Atlas de ciudades sumergidas', 'Seccion_Artificial',
           'Una archivista investiga ciudades hundidas que reaparecen durante unas pocas horas al año.',
           NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY
    UNION ALL
    SELECT @demo_carla_id, 'Publicados', @pro_model_id,
           'Bitácora del tren nocturno', 'Seccion_Artificial',
           'Un convoy cruza un país dormido mientras su tripulación descubre estaciones borradas de todos los mapas.',
           NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY
    UNION ALL
    SELECT @demo_diego_id, 'Sci-Fi', @pro_model_id,
           'Niebla sobre Europa IX', 'Seccion_Artificial',
           'Una estación minera detecta señales imposibles bajo el hielo de Europa IX.',
           NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 7 DAY
) data
WHERE data.user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM Relato r
      WHERE r.FK_UsuarioID = data.user_id
        AND r.Titulo = data.titulo
  );

INSERT INTO Relato_Estanteria (
    FK_RelatoID, FK_EstanteriaID
)
SELECT
    r.PK_RelatoID,
    e.PK_EstanteriaID
FROM (
    SELECT @demo_ana_id AS user_id, 'El faro de sal' AS titulo, 'Fantasía' AS estanteria
    UNION ALL
    SELECT @demo_ana_id, 'Cuaderno del bosque gris', 'Borradores'
    UNION ALL
    SELECT @demo_bruno_id, 'La brújula sin norte', 'Aventura'
    UNION ALL
    SELECT @demo_carla_id, 'Atlas de ciudades sumergidas', 'Premium Drafts'
    UNION ALL
    SELECT @demo_carla_id, 'Atlas de ciudades sumergidas', 'Publicados'
    UNION ALL
    SELECT @demo_carla_id, 'Bitácora del tren nocturno', 'Publicados'
    UNION ALL
    SELECT @demo_diego_id, 'Niebla sobre Europa IX', 'Sci-Fi'
    UNION ALL
    SELECT @demo_diego_id, 'Niebla sobre Europa IX', 'Premium Drafts'
) data
JOIN Relato r
  ON r.FK_UsuarioID = data.user_id
 AND r.Titulo = data.titulo
JOIN Estanteria e
  ON e.NombreCategoria = data.estanteria
WHERE NOT EXISTS (
    SELECT 1
    FROM Relato_Estanteria re
    WHERE re.FK_RelatoID = r.PK_RelatoID
      AND re.FK_EstanteriaID = e.PK_EstanteriaID
);

INSERT INTO RelatoVersion (FK_RelatoID, NumeroVersion, Contenido, Notas, EsPublicada, FechaVersion)
SELECT r.PK_RelatoID, 1, r.Descripcion, 'Versión inicial de demo', FALSE, r.FechaCreacion
FROM Relato r
WHERE r.Titulo IN (
    'El faro de sal',
    'Cuaderno del bosque gris',
    'La brújula sin norte',
    'Atlas de ciudades sumergidas',
    'Bitácora del tren nocturno',
    'Niebla sobre Europa IX'
)
AND NOT EXISTS (
    SELECT 1 FROM RelatoVersion rv
    WHERE rv.FK_RelatoID = r.PK_RelatoID
      AND rv.NumeroVersion = 1
);

INSERT INTO RelatoVersion (FK_RelatoID, NumeroVersion, Contenido, Notas, EsPublicada, FechaVersion)
SELECT
    r.PK_RelatoID,
    2,
    CONCAT(r.Descripcion, '\n\nLa protagonista descubre una pista adicional que complica la investigación.'),
    'Segunda versión de demo',
    FALSE,
    NOW() - INTERVAL 7 DAY
FROM Relato r
WHERE r.Titulo IN ('El faro de sal', 'Atlas de ciudades sumergidas')
AND NOT EXISTS (
    SELECT 1 FROM RelatoVersion rv
    WHERE rv.FK_RelatoID = r.PK_RelatoID
      AND rv.NumeroVersion = 2
);

INSERT INTO ConfiguracionIA (
    FK_RelatoID, EstiloEscritura, NivelCreatividad, LongitudRespuesta, TonoEmocional
)
SELECT r.PK_RelatoID, 'Narrativo', 'Medio', 'Media', 'Misterioso'
FROM Relato r
WHERE r.Titulo IN ('El faro de sal', 'Bitácora del tren nocturno')
AND NOT EXISTS (
    SELECT 1 FROM ConfiguracionIA c WHERE c.FK_RelatoID = r.PK_RelatoID
);

INSERT INTO ConfiguracionIA (
    FK_RelatoID, EstiloEscritura, NivelCreatividad, LongitudRespuesta, TonoEmocional
)
SELECT r.PK_RelatoID, 'Descriptivo', 'Alto', 'Larga', 'Tenso'
FROM Relato r
WHERE r.Titulo IN ('Niebla sobre Europa IX', 'Atlas de ciudades sumergidas')
AND NOT EXISTS (
    SELECT 1 FROM ConfiguracionIA c WHERE c.FK_RelatoID = r.PK_RelatoID
);

INSERT INTO MensajeChat (FK_RelatoID, Emisor, ContenidoMensaje, Orden, FechaEnvio)
SELECT r.PK_RelatoID, seed.emisor, seed.contenido, seed.orden, seed.fecha_envio
FROM (
    SELECT 'El faro de sal' AS titulo, 'Usuario' AS emisor,
           'Quiero una apertura atmosférica con mar, viento y una pista antigua.' AS contenido,
           1 AS orden, NOW() - INTERVAL 18 DAY AS fecha_envio
    UNION ALL
    SELECT 'El faro de sal', 'Poly',
           'La niebla cubría el acantilado cuando Mara vio que el faro latía como si respirara.',
           2, NOW() - INTERVAL 18 DAY + INTERVAL 5 MINUTE
    UNION ALL
    SELECT 'El faro de sal', 'Usuario',
           'Hazlo más amplio y que la protagonista sospeche que alguien dejó un mapa.',
           3, NOW() - INTERVAL 18 DAY + INTERVAL 10 MINUTE
    UNION ALL
    SELECT 'El faro de sal', 'Poly',
           'Entre la sal y la madera mojada, Mara halló un mapa doblado dentro de una lámpara apagada.',
           4, NOW() - INTERVAL 18 DAY + INTERVAL 13 MINUTE
    UNION ALL
    SELECT 'Atlas de ciudades sumergidas', 'Usuario',
           'Dame un arranque elegante y descriptivo, como si fuera un atlas narrado.',
           1, NOW() - INTERVAL 10 DAY
    UNION ALL
    SELECT 'Atlas de ciudades sumergidas', 'Poly',
           'Cada equinoccio, las ciudades hundidas ascendían como recuerdos mojados a la superficie del mundo.',
           2, NOW() - INTERVAL 10 DAY + INTERVAL 4 MINUTE
    UNION ALL
    SELECT 'Niebla sobre Europa IX', 'Usuario',
           'Quiero un inicio de ciencia ficción con tensión y una anomalía bajo el hielo.',
           1, NOW() - INTERVAL 8 DAY
    UNION ALL
    SELECT 'Niebla sobre Europa IX', 'Poly',
           'Los sensores marcaron un latido térmico imposible a tres kilómetros bajo la capa azul del glaciar.',
           2, NOW() - INTERVAL 8 DAY + INTERVAL 6 MINUTE
) seed
JOIN Relato r ON r.Titulo = seed.titulo
WHERE NOT EXISTS (
    SELECT 1 FROM MensajeChat m
    WHERE m.FK_RelatoID = r.PK_RelatoID
      AND m.Orden = seed.orden
);

INSERT INTO LogModeracion (FK_UsuarioID, FK_PalabraID, Motivo, ContenidoBloqueadoHash, Fecha)
SELECT @demo_bruno_id,
       (SELECT PK_PalabraID FROM PalabraProhibida WHERE Palabra = 'contenido adulto' LIMIT 1),
       'Mensaje bloqueado por contenido no permitido',
       SHA2('contenido adulto de prueba', 256),
       NOW() - INTERVAL 6 DAY
WHERE @demo_bruno_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM LogModeracion lm
      WHERE lm.FK_UsuarioID = @demo_bruno_id
        AND lm.Motivo = 'Mensaje bloqueado por contenido no permitido'
  );

UPDATE LogModeracion lm
JOIN Usuario u ON u.PK_UsuarioID = lm.FK_UsuarioID
LEFT JOIN PalabraProhibida pp ON pp.PK_PalabraID = lm.FK_PalabraID
SET lm.FK_PalabraID = (
        SELECT p.PK_PalabraID
        FROM PalabraProhibida p
        WHERE p.Palabra = 'contenido adulto'
        LIMIT 1
    )
WHERE u.Correo = 'demo.bruno@example.com'
  AND lm.Motivo = 'Mensaje bloqueado por contenido no permitido'
  AND pp.PK_PalabraID IS NULL;

INSERT INTO AuditoriaRolUsuario (
    FK_UsuarioAfectadoID,
    FK_AdminID,
    FK_RolAnteriorID,
    FK_RolNuevoID,
    FechaCambio
)
SELECT
    @demo_carla_id,
    @demo_admin_id,
    (SELECT PK_RolID FROM Rol WHERE NombreRol = 'Gratuito' LIMIT 1),
    (SELECT PK_RolID FROM Rol WHERE NombreRol = 'Premium' LIMIT 1),
    NOW() - INTERVAL 11 DAY
WHERE @demo_carla_id IS NOT NULL
  AND @demo_admin_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM AuditoriaRolUsuario aru
      WHERE aru.FK_UsuarioAfectadoID = @demo_carla_id
        AND aru.FK_AdminID = @demo_admin_id
        AND aru.FK_RolNuevoID = (SELECT PK_RolID FROM Rol WHERE NombreRol = 'Premium' LIMIT 1)
  );

COMMIT;
