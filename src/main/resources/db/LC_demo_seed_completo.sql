-- Seed demo completo para LibraryAI_DB
-- Objetivo:
-- dejar la base con usuarios, roles, planes, historial y relatos
-- como si la aplicación ya hubiera sido usada manualmente.
--
-- Qué crea:
-- - roles y planes si no existen
-- - 5 usuarios demo
-- - suscripciones y pagos simulados
-- - estanterías
-- - relatos artificiales y creativos
-- - versiones de relatos
-- - configuraciones IA
-- - historiales de chat con Poly
-- - actividad adicional para usuarios actuales si ya existen
--
-- Credenciales demo:
-- - demo.admin@example.com / Demo123!
-- - demo.ana@example.com / Demo123!
-- - demo.bruno@example.com / Demo123!
-- - demo.carla@example.com / Demo123!
-- - demo.diego@example.com / Demo123!
--
-- Nota:
-- este script no crea archivos físicos en uploads ni inserta registros
-- en ArchivoUsuario/Relato_ArchivoFuente.

USE LibraryAI_DB;

START TRANSACTION;

SET @demo_hash = '$2a$10$Ka5IDG6s7g2jGVsnMkXzJ.Mf57hx5CjabKB7uXNaj9LnF7/QyXR2W';

INSERT IGNORE INTO Rol (NombreRol)
VALUES ('Admin'), ('Gratuito'), ('Premium');

INSERT INTO PlanSuscripcion (NombrePlan, AlmacenamientoMaxMB, Precio, Activo)
SELECT 'Plan Gratuito', 500, 0.00, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM PlanSuscripcion WHERE NombrePlan = 'Plan Gratuito'
);

INSERT INTO PlanSuscripcion (NombrePlan, AlmacenamientoMaxMB, Precio, Activo)
SELECT 'Plan Premium', 2048, 9.99, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM PlanSuscripcion WHERE NombrePlan = 'Plan Premium'
);

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
           'Responde como administrador demo y resume con claridad.' AS instruccion,
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
    SELECT PK_PlanID FROM PlanSuscripcion WHERE NombrePlan = 'Plan Gratuito' LIMIT 1
);
SET @premium_plan_id = (
    SELECT PK_PlanID FROM PlanSuscripcion WHERE NombrePlan = 'Plan Premium' LIMIT 1
);
SET @flash_model_id = (
    SELECT PK_ModeloID FROM ModeloIA
    WHERE Estado = 'Activo' AND EsGratuito = TRUE
    ORDER BY PK_ModeloID
    LIMIT 1
);
SET @pro_model_id = (
    SELECT PK_ModeloID FROM ModeloIA
    WHERE Estado = 'Activo' AND EsGratuito = FALSE
    ORDER BY PK_ModeloID
    LIMIT 1
);

SET @demo_admin_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.admin@example.com' LIMIT 1);
SET @demo_ana_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.ana@example.com' LIMIT 1);
SET @demo_bruno_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.bruno@example.com' LIMIT 1);
SET @demo_carla_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.carla@example.com' LIMIT 1);
SET @demo_diego_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo.diego@example.com' LIMIT 1);
SET @current_admin_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'keinerf588@gmail.com' LIMIT 1);
SET @current_demo_id = (SELECT PK_UsuarioID FROM Usuario WHERE Correo = 'demo2@example.com' LIMIT 1);

INSERT INTO UsuarioRol (FK_UsuarioID, FK_RolID)
SELECT u.PK_UsuarioID, r.PK_RolID
FROM Usuario u
JOIN Rol r ON r.NombreRol = 'Admin'
WHERE u.Correo IN ('demo.admin@example.com')
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
    SELECT @demo_admin_id AS user_id, @premium_plan_id AS plan_id, NOW() - INTERVAL 30 DAY AS fecha_inicio, TRUE AS renovacion
    UNION ALL
    SELECT @demo_ana_id, @free_plan_id, NOW() - INTERVAL 20 DAY, FALSE
    UNION ALL
    SELECT @demo_bruno_id, @free_plan_id, NOW() - INTERVAL 18 DAY, FALSE
    UNION ALL
    SELECT @demo_carla_id, @premium_plan_id, NOW() - INTERVAL 12 DAY, TRUE
    UNION ALL
    SELECT @demo_diego_id, @premium_plan_id, NOW() - INTERVAL 10 DAY, TRUE
    UNION ALL
    SELECT @current_admin_id, @premium_plan_id, NOW() - INTERVAL 45 DAY, TRUE
    UNION ALL
    SELECT @current_demo_id, @free_plan_id, NOW() - INTERVAL 20 DAY, FALSE
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
SELECT s.PK_SuscripcionID, 'Simulada', 'Completado', ref.referencia, 9.99, ref.fecha_pago
FROM (
    SELECT 'demo.admin@example.com' AS correo, 'DEMO-ADMIN-001' AS referencia, NOW() - INTERVAL 29 DAY AS fecha_pago
    UNION ALL
    SELECT 'demo.carla@example.com', 'DEMO-CARLA-001', NOW() - INTERVAL 11 DAY
    UNION ALL
    SELECT 'demo.diego@example.com', 'DEMO-DIEGO-001', NOW() - INTERVAL 9 DAY
    UNION ALL
    SELECT 'keinerf588@gmail.com', 'HIST-KEYNER-001', NOW() - INTERVAL 44 DAY
) ref
JOIN Usuario u ON u.Correo = ref.correo
JOIN Suscripcion s ON s.FK_UsuarioID = u.PK_UsuarioID AND s.Estado = 'Activa'
WHERE NOT EXISTS (
    SELECT 1 FROM Pago p WHERE p.ReferenciaExterna = ref.referencia
);

INSERT INTO Estanteria (FK_UsuarioID, NombreCategoria)
SELECT seed.user_id, seed.nombre_categoria
FROM (
    SELECT @demo_ana_id AS user_id, 'Fantasía' AS nombre_categoria
    UNION ALL
    SELECT @demo_ana_id, 'Borradores'
    UNION ALL
    SELECT @demo_bruno_id, 'Aventura'
    UNION ALL
    SELECT @demo_carla_id, 'Premium Drafts'
    UNION ALL
    SELECT @demo_carla_id, 'Publicados'
    UNION ALL
    SELECT @demo_diego_id, 'Sci-Fi'
    UNION ALL
    SELECT @current_demo_id, 'Borradores personales'
    UNION ALL
    SELECT @current_demo_id, 'Ciencia ficción'
    UNION ALL
    SELECT @current_admin_id, 'Beta interna'
    UNION ALL
    SELECT @current_admin_id, 'Historias revisadas'
) seed
WHERE seed.user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM Estanteria e
      WHERE e.FK_UsuarioID = seed.user_id
        AND e.NombreCategoria = seed.nombre_categoria
  );

INSERT INTO Relato (
    FK_UsuarioID, FK_EstanteriaID, FK_ModeloUsadoID,
    Titulo, ModoOrigen, Descripcion, FechaCreacion, FechaModificacion
)
SELECT
    data.user_id,
    (
        SELECT e.PK_EstanteriaID
        FROM Estanteria e
        WHERE e.FK_UsuarioID = data.user_id
          AND e.NombreCategoria = data.estanteria
        LIMIT 1
    ),
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
    SELECT @demo_diego_id, 'Sci-Fi', @pro_model_id,
           'Niebla sobre Europa IX', 'Seccion_Artificial',
           'Una estación minera detecta señales imposibles bajo el hielo de Europa IX.',
           NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 7 DAY
    UNION ALL
    SELECT @current_demo_id, 'Borradores personales', NULL,
           'La casa que encendía la lluvia', 'Seccion_Creativa',
           'Una restauradora hereda una casa donde cada tormenta activa recuerdos ajenos atrapados en las paredes.',
           NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 18 DAY
    UNION ALL
    SELECT @current_demo_id, 'Ciencia ficción', @flash_model_id,
           'La órbita de cristal', 'Seccion_Artificial',
           'Una nave de prospección detecta una estructura transparente orbitando un planeta sin nombre.',
           NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 10 DAY
    UNION ALL
    SELECT @current_admin_id, 'Historias revisadas', @pro_model_id,
           'Archivo de mareas sumergidas', 'Seccion_Artificial',
           'Un archivista encuentra mapas oceánicos que señalan ciudades borradas de la historia oficial.',
           NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY
    UNION ALL
    SELECT @current_admin_id, 'Beta interna', NULL,
           'Notas del quinto ascensor', 'Seccion_Creativa',
           'Borrador de thriller urbano sobre un ascensor que abre a pisos inexistentes cuando alguien miente dentro de él.',
           NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY
) data
WHERE data.user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM Relato r
      WHERE r.FK_UsuarioID = data.user_id
        AND r.Titulo = data.titulo
  );

INSERT INTO RelatoVersion (FK_RelatoID, NumeroVersion, Contenido, Notas, EsPublicada, FechaVersion)
SELECT r.PK_RelatoID, 1, r.Descripcion, 'Versión inicial de demo', FALSE, r.FechaCreacion
FROM Relato r
WHERE r.Titulo IN (
    'El faro de sal',
    'Cuaderno del bosque gris',
    'La brújula sin norte',
    'Atlas de ciudades sumergidas',
    'Niebla sobre Europa IX',
    'La casa que encendía la lluvia',
    'La órbita de cristal',
    'Archivo de mareas sumergidas',
    'Notas del quinto ascensor'
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
    CONCAT(r.Descripcion, '\n\nLa protagonista sospecha que la casa no guarda fantasmas, sino decisiones que nunca tomó.'),
    'Ajuste manual del segundo día',
    FALSE,
    NOW() - INTERVAL 17 DAY
FROM Relato r
WHERE r.Titulo = 'La casa que encendía la lluvia'
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
WHERE r.Titulo IN ('La órbita de cristal', 'Atlas de ciudades sumergidas')
AND NOT EXISTS (
    SELECT 1 FROM ConfiguracionIA c WHERE c.FK_RelatoID = r.PK_RelatoID
);

INSERT INTO ConfiguracionIA (
    FK_RelatoID, EstiloEscritura, NivelCreatividad, LongitudRespuesta, TonoEmocional
)
SELECT r.PK_RelatoID, 'Narrativo', 'Alto', 'Larga', 'Melancólico'
FROM Relato r
WHERE r.Titulo = 'Archivo de mareas sumergidas'
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
    UNION ALL
    SELECT 'La órbita de cristal', 'Usuario',
           'Necesito una apertura con mucha imagen visual y algo imposible en el radar.',
           1, NOW() - INTERVAL 11 DAY
    UNION ALL
    SELECT 'La órbita de cristal', 'Poly',
           'La esfera reflejaba las estrellas con un retraso de varios segundos, como si las recordara en lugar de mirarlas.',
           2, NOW() - INTERVAL 11 DAY + INTERVAL 5 MINUTE
    UNION ALL
    SELECT 'Archivo de mareas sumergidas', 'Usuario',
           'Quiero un tono elegante, casi de archivo histórico, pero con un misterio oceánico.',
           1, NOW() - INTERVAL 9 DAY
    UNION ALL
    SELECT 'Archivo de mareas sumergidas', 'Poly',
           'Los mapas no describían costas: describían ausencias, ciudades retiradas con violencia del recuerdo humano.',
           2, NOW() - INTERVAL 9 DAY + INTERVAL 6 MINUTE
) seed
JOIN Relato r ON r.Titulo = seed.titulo
WHERE NOT EXISTS (
    SELECT 1 FROM MensajeChat m
    WHERE m.FK_RelatoID = r.PK_RelatoID
      AND m.Orden = seed.orden
);

COMMIT;
