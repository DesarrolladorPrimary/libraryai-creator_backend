2-3-2025

Library
Creator
Proyecto Final

Keyner Arismendy
ADSO 2994281

Tabla Contenido
Tabla Contenido ........................................................................................................... 1
Introducción .................................................................................................................. 2
Objetivo(s) General(s) y Especifico(s) ........................................................................... 2
DESCRIPCIÓN ............................................................................................................. 3
Requisitos Funcionales: ................................................................................................ 4
Registrar y verificar datos del usuario ........................................................................ 4
Autenticar Usuario ..................................................................................................... 5
Brindar una experiencia de interfaz estructurada intuitiva .......................................... 6
Insertar documentos.................................................................................................. 8
Implementar el uso de Inteligencia Artificial (Poly)..................................................... 9
Parametrizar instrucciones ...................................................................................... 10
Creador manual ...................................................................................................... 12
Proporcionar herramientas de redacción ................................................................. 13
Convertir Chat a formato exportable ........................................................................ 14
Crear chats.............................................................................................................. 16
Gestionar secciones ................................................................................................ 17
Eliminar chats.......................................................................................................... 18
Requisitos no funcionales (RNF)................................................................................. 34
Conclusión .................................................................................................................. 35
Bibliografía .................................................................................................................. 36

Descripción Proyecto:
Library Creator es una plataforma integral de creación literaria diseñada para
potenciar la imaginación del autor mediante un entorno híbrido que fusiona la
escritura tradicional con la asistencia de Inteligencia Artificial avanzada. A
través de su asistente virtual "Poly", el sistema ofrece dos modalidades de
trabajo: una Sección Artificial, donde los usuarios interactúan mediante un
1

chat inteligente parametrizable para generar y reinventar narrativas guiadas, y
una Sección Creativa, que proporciona un espacio de redacción manual con
herramientas de edición especializadas para quienes buscan total autonomía.
Más allá de la escritura, el software actúa como un gestor editorial completo
que permite catalogar las obras en estanterías virtuales, gestionar el
versionamiento de los textos y convertir los proyectos finales en libros
exportables (PDF/Word) , todo ello sustentado bajo un modelo escalable de
suscripciones que adapta el almacenamiento y la potencia de la IA a las
necesidades del escritor

Introducción
Con la innovación y mejora de la Inteligencia Artificial (IA) el mundo ha
mejorado y automatizado todas esas tareas que se han llevado durante años
por la sociedad, las más conocidas lectura y escritura, una actividad que se ha
realizado por años y se podría decir las más antigua de todas. Este proyecto no
busca reemplazar a la más querida escritura, pero si conseguir la maravilla de
la lectura con la creación de una plataforma capaz de inventar y crear
fascinantes historias ficticias o incluso recrear aquellas existentes, pero con un
toque creativo que el mundo podría decidir.
Este gran entorno dará la posibilidad de reinventar y crear fascinante mundo
que se exploraran atreves de la lectura y creatividad, dando a acceder a varias
herramientas con IA que facilitaran el trabajo e incluso también apoyarse del
mismo pensamiento crítico.

Objetivo(s) General(s) y Especifico(s)
Generales:
- Desarrollar un sistema con la capacidad de crear y reinventar literatura e
historias ficticias imbuidas con IA para la facilidad de la creación y
fomentación de la escritura.

2

Específicos:
- Desarrollar un conjunto de herramientas interactivas que faciliten la
creación literaria y fomenten la escritura creativa.
- Implementar una IA avanzada que genere relatos ficticios
personalizados según las preferencias y gustos de los usuarios.
- Proporcionar opciones de almacenamiento y exportación para que
los usuarios puedan guardar, compartir y conservar sus creaciones de
manera segura y accesible.
- Facilitar un chat interactivo que permita la comodidad de uso para la
intuición en el momento de realizar la creación de relatos.

DESCRIPCIÓN
Este software está diseñado para ofrecer una plataforma intuitiva que facilita la
creación y redacción de cuentos infantiles o novelas, brindando una
experiencia enriquecedora para escritores de todos los niveles. Tendrá como
primero una interfaz de inicio de sesión que permitirá al usuario registrarse. El
trabajo se llevará a cabo mediante una sección impulsada por inteligencia
artificial (IA), en la que se habilitará un chat interactivo. Este chat permitirá al
usuario ingresar sus necesidades de manera intuitiva, ya que la IA le guiará a
través de un proceso paso a paso, preguntando por sus preferencias. Cuando
se establezcan, la IA comenzará a generar el contenido basado en dichas
preferencias.
Adicionalmente, el software incluye una sección donde el usuario puede
redactar su contenido sin la ayuda de la IA, permitiendo la total autonomía
sobre el desarrollo del proyecto. Todo el contenido creado se podrá guardar y
almacenar de forma eficiente y automatizada para evitar sobrecargas o
inestabilidad. En esta sección dedicada, el usuario podrá visualizar, modificar,
cambiar, borrar y exportar el contenido según sea necesario. Además, el
almacenamiento se manejará por suscripción y en base a la suscripción del
usuario tendrá una cantidad de almacenamiento disponible para su uso. El
software se lanzará en sistemas móviles, pero estará soportado para otras
plataformas para ampliar su accesibilidad y funcionalidad.

3

Requisitos Funcionales:
Library Creator Requisitos Funcionales

ID_Requisito

RF_01

Nombre de
requisito

Registrar y verificar datos del usuario

Componente

Campos de entrada (nombre, correo,
contraseña) y verificación correo.

Característica
asociada

Gestión de usuarios y seguridad

Descripción del
requisito

El sistema debe permitir el registro y
verificación de datos personales de
los usuarios una sola vez,
garantizando la integridad y
confidencialidad de la información.

Características

Validación de campos obligatorios,
Encriptación de datos sensibles.

Prioridad

Alta

4

Library Creator Requisitos Funcionales

Restricciones

Los datos deben cumplir con la
normativa de protección de datos
vigente. Además, deben poseer una
longitud de (8 caracteres) agregando
un carácter numérico o símbolos.

Interacción
humano
tecnología

Si

Interacción
tecnología
tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

RF_02

Nombre de
requisito

Autenticar Usuario

Componente

Verificar credenciales del usuario

Característica
asociada

Autenticación del usuario

Descripción del
requisito

El sistema debe permitir que los usuarios ingresen su
nombre de usuario y contraseña para acceder a la
plataforma. Los datos de inicio de sesión deben ser
validados contra la base de datos para asegurar que las
credenciales sean correctas y que el usuario tenga
permiso para acceder.

5

Library Creator Requisitos Funcionales

características

El sistema debe proporcionar un formulario de inicio de
sesión. El nombre de usuario y la contraseña deben ser
verificados contra los registros de la base de datos. El
sistema debe mostrar mensajes de error apropiados si
las credenciales no son válidas.

Prioridad

Alta

Restricciones

Los datos ingresados deben coincidir con los de registro
que fueron llevados a la base de datos.

Interacción
humano
tecnología

Si

Interacción
tecnología
tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de
requisito

RF_03

Brindar una experiencia de interfaz
estructurada intuitiva

Componente

Interfaz de Usuario (UI)

Característica
asociada

Usabilidad y Experiencia del Usuario (UX)

6

Library Creator Requisitos Funcionales

Descripción del
requisito

El sistema debe permitir al usuario acceder a las
funciones principales de manera corta y sencilla, siendo
desde la pantalla principal a una función (Crear chat, la
biblioteca o configuraciones) en menos de 3 clics u
oportunidades.

características

Todos los elementos visuales y funcionales deben
seguir un patrón uniforme. Los elementos más
importantes deben destacarse mediante tamaño, color
o posición. Las rutas de navegación deben ser
predecibles y fáciles de seguir.

Prioridad

Alta

Restricciones

No debe sobrecargar el sistema con animaciones o
efectos innecesarios que afecten el rendimiento. Debe
ser funcional en dispositivos móviles, tabletas y
computadoras de escritorio.

Interacción
humano
tecnología

Si

Interacción
tecnología
tecnología

No

7

Library Creator Requisitos Funcionales

ID_Requisito

RF_04

Nombre de
requisito

Insertar documentos

Componente

Función de inserción de contenido.

Característica
asociada

Capacidad de subir contenido (Documentos).

Descripción del
requisito

El sistema debe permitir al usuario por medio de una
opción en la ventana de chat, subir localmente un
documento Word o PDF.

Características

Cuando el usuario desee que la IA analice un texto o
relato que este en un documento, podrá insertarlo por
medio de una opción en el menú de la ventana de
chat, subiendo un archivo que posea localmente.
La IA tendrá la capacidad de leer los documentos y en
base a la petición del usuario, podrá hacerle cualquier
solicitud en referencia al documento para crear o
reinventar el relato deseado.

Prioridad

Media

Restricciones

Al usuario el sistema le permitirá insertar documentos
de textos, ya sea Word o PDF, pero no podrá subir otro
tipo de archivo que no sea el especificado por el
sistema.

Interacción
Si
humano tecnología

8

Library Creator Requisitos Funcionales

Interacción
tecnología
tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de
requisito

RF_05

Implementar el uso de Inteligencia
Artificial (Poly)

Componente

Ventana de chat, sistema.

Característica
asociada

Generador artificial de texto.

Descripción del
requisito

El Software debe usar la IA por medio de un chat de
creación que permitirá al usuario crear contenido en
base a preferencias.

características

El usuario al ingresar a la plataforma tendrá acceso a la
sección de chat con IA, la ventana de chat le permitirá
al usuario comunicarse y brindar sus ideas a la Poly, el
chatbot encargado de generar las creativas ideas del
usuario.
El usuario hablara con poly como en una conversación
por chat, principalmente se le pedirá con detalle que
desea que se genere, como sus gustos e ideas.

9

Library Creator Requisitos Funcionales

Prioridad

Alta

Restricciones

El usuario no deberá ingresar ningún tipo de
información que sea considerada (NSFW).
Si el usuario ingresa contenido +18 se le dará una
advertencia que se retracte de seguir y que cambie el
contenido.
El sistema deberá identificar malas palabras que estén
previamente en una lista negra identificada como un
filtro de palabras no aptas y bloquee inmediatamente él
envió del usuario, además de lanzar una advertencia
para corregir el contenido.

Interacción
humano
tecnología
Interacción
tecnología
tecnología

Si

Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_06

Parametrizar instrucciones

Componente

Ventana de chat, IA (Poly)

Característica asociada

Parámetros de instrucción

10

Library Creator Requisitos Funcionales

Descripción del requisito

El sistema debe permitir al usuario
ingresar de forma permanente las
instrucciones que desea que Poly
recuerde siempre para mejorar o
controlar la manera en que crea los
relatos.

características

El usuario tendrá acceso a un apartado
de personalización, el cual le dará la
posibilidad de que por medio de un
promt le pueda dar una instrucción
permanente a la IA que recordara y
ajustara según el promt inscrito por el
usuario.
La instrucción le dará a la IA una idea
de cómo deberá relatar la información
proporcionada por el usuario. Ajustando
su forma vocabulario y forma de
expresarse en el chat.

Prioridad

Media

Restricciones

EL sistema recibirá las instrucciones
que el usuario le proporcione, pero si
las instrucciones son +18 el sistema le
dará un aviso de que ese contenido no
es admitido y que reconsidere la lo que
desea instruir.

Interacción humano tecnología
Si

Interacción tecnología tecnología
No

11

Library Creator Requisitos Funcionales

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_07

Creador manual

Componente

Ventana de chat

Característica asociada

Relatador manual

Descripción del requisito

El sistema le permitirá al usuario en una
sección de chat, la opción de redactar sus
propios relatos ficticios sin el uso de la IA.

características

El usuario dentro del aplicativo tendrá acceso a
una sección de chat donde no estará disponible
Poly, la ventana de chat estará constituida para
que el usuario redacte sus relatos a su propio
gusto.

Prioridad

Media

12

Library Creator Requisitos Funcionales

Restricciones

El usuario ingresará el texto que desea para su
relato, aunque la ventana de chat no tenga
disponible la IA, el texto que se ingrese si será
leído por la IA para verificar su contenido y si
detecta contenido no acto le dará un aviso al
usuario de cambiarlo.

Interacción humano tecnología

Si

Interacción tecnología tecnología No

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_08

Proporcionar herramientas de
redacción

Componente

Editor de Contenidos, ventana de chat

Característica asociada

Creación y edición de textos

Descripción del requisito

El sistema debe proporcionar herramientas de
redacción que permitan a los usuarios crear,
editar y formatear textos de manera intuitiva.
Estas herramientas incluirán corrector
ortográfico, sugerencias de estilo y opciones de
formato.

13

Library Creator Requisitos Funcionales

características

El usuario tendrá disponible una opción que le
permitirá acceder a las herramientas de
redacción que le brindará una mejor forma de
escritura y mejora de sus relatos.

Prioridad

Alta

Restricciones

El usuario deberá estar registrado para acceder
a las herramientas.

Interacción humano tecnología

Si

Interacción tecnología tecnología Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_ 09

Convertir Chat a formato exportable

Componente

Contenido creado, texto

Característica asociada

Conversor a formatos

14

Library Creator Requisitos Funcionales

Descripción del requisito

El sistema debe convertir el contenido del
chat a Word o PDF para su descarga o
guardado.

características

El usuario cuando haya efectuado su proceso
de creación tendrá acceso a la opción de
conversión de formatos que le permitirá pasar
sus relatos a un documento (Word o PDF) y
almacenarlos.

Prioridad

Alta

Restricciones

El usuario deberá estar previamente registrado
en la plataforma.
El usuario no tendrá acceso a convertir si se
detectan palabras indebidas o no actas.

Interacción humano tecnología

Si

Interacción tecnología tecnología Si

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 10

15

Library Creator Requisitos Funcionales

Nombre de requisito

Crear chats

Componente

Ventanas de chat, sección

Característica asociada

Gestión de chats, historial de chats

Descripción del requisito

El sistema debe permitir al usuario crear y
gestionar los chats de creación, accediendo a
crear cuantos desee dependiendo de la sección
elegida previamente.

características

El usuario al haber ingresado y registrado
previamente en la plataforma tendrá acceso a
las secciones, dependiendo de cual elija el
sistema le permitirá gestionar un chat de
creación, con el que podrá empezar su relato.

Los chats de creación tienes diferentes
disposiciones y herramientas dependiendo de
la sección escogida previamente.
Prioridad

Alta

Restricciones

El usuario que no esté registrado en la
plataforma tendrá un acceso límites a las
funciones y cantidad de chats de creación.

Interacción humano tecnología

Si

Interacción tecnología tecnología No

16

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_ 11

Gestionar secciones

Componente

Secciones de chats

Característica asociada

Chats, funciones específicas

Descripción del requisito

El sistema permitirá al usuario seleccionar la
sección que desee usar, según sus
preferencias.

características

El usuario tendrá disponible dos secciones
llamadas:
-

-

Sección Artificial: Esta sección permitirá
el acceso a un chat de creación que
estará impulsado con Inteligencia
Artificial, que se enfocará en brindar
ayuda al usuario a en su trabajo de
redacción de sus relatos.
Sección Creativa: Esta sección permitirá
el acceso a un chat de creación donde el
usuario tendrá libertad de redactar por si
mismos sin ayuda, tendrá acceso a las
herramientas de redacción para brindar
apoyo en ortografía y demás.

17

Library Creator Requisitos Funcionales

Prioridad

Alta

Restricciones

Los chats de creación de cada sección estarán
monitoreados por IA para la restricción y
precaución de palabras indebidas o +18.
El usuario deberá estar registrado para poder
acceder a todo el contenido implementado en la
plataforma.

Interacción humano tecnología

Si

Interacción tecnología tecnología Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_ 12

Eliminar chats

Componente

Ventanas de chat, Secciones

Característica asociada

Eliminación de chats de creación

18

Library Creator Requisitos Funcionales

Descripción del requisito

El sistema debe permitir eliminar los chats de
creación que el usuario haya creado
previamente.

características

El usuario tendrá acceso a la opción de eliminar
los chats de creación que haya creado
previamente en su respectiva sección

Prioridad

Alta

Restricciones

El usuario debe estar registrado previamente.

Interacción humano tecnología

Si

Interacción tecnología tecnología No

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_ 13

Descargar el archivo ya convertido

19

Library Creator Requisitos Funcionales

Componente

Documentos de contenido, biblioteca de
guardado

Característica asociada

Descargar contenido.

Descripción del requisito

El sistema debe permitir al usuario descargar
localmente los documentos que ya fueron
convertidos.

características

El usuario podrá acceder a la función dentro de
la biblioteca de descargar los documentos que
fueron convertidos y guardados para usarlos
externamente en su dispositivo.

Prioridad

Media

Restricciones

El usuario podrá descargar los documentos
siempre y cuando estos existan y hayan sido
convertidos a formato (Word o PDF).
El usuario deberá estar registrado para acceder
a la función de descargar.

Interacción humano tecnología

Si

Interacción tecnología tecnología Si

20

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 14

Nombre de requisito

Almacenar documentos en la biblioteca

Componente

"Documentos guardados, Base de datos,
Módulo de Suscripción"

Característica asociada

Control de almacenamiento según plan
(Gratuito/Premium)

Descripción del requisito

El sistema debe guardar automáticamente los
documentos convertidos en la biblioteca del
usuario según su plan de suscripción.

características

1. El sistema verificará el peso del archivo
antes de guardar.
2. Si el usuario es "Gratuito", el sistema validará
que no exceda el límite de 500 MB.
3. Si el usuario es "Premium", el sistema
permitirá almacenamiento ilimitado.

Prioridad

Alta

Restricciones

El sistema debe bloquear la acción de guardar
y mostrar una alerta si se supera la cuota
permitida. Los documentos deben haber sido
convertidos previamente.

Interacción humano tecnología

Si

21

Library Creator Requisitos Funcionales

Interacción tecnología tecnología Si

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 15

Nombre de requisito

Eliminar libros

Componente

Registro de documentos, biblioteca

Característica asociada

Eliminar libros que hayan sido registrados

Descripción del requisito

El sistema debe permitir al usuario la acción de
eliminar los documentos que fueron guardados
en la biblioteca.

características

El usuario tendrá acceso a eliminar los libros
(Documentos) que han sido guardados por él
en la biblioteca.

Prioridad

Media

22

Library Creator Requisitos Funcionales

Restricciones

El usuario deberá estar registrado previamente
en la plataforma.
Los documentos deben estar guardados
previamente.

Interacción humano tecnología

Si

Interacción tecnología tecnología No

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 16

Nombre de requisito

Catalogar libros (documentos)

Componente

Documentos, Biblioteca

Característica asociada

Categorizar libros almacenados

Descripción del requisito

El sistema debe permitir crear categorías donde
se catalogarán y organizarán los libros que se
hayan almacenados.

23

Library Creator Requisitos Funcionales

características

El usuario antes de almacenar sus documentos
se le dará la opción de crear estanterías, que se
encargaran de organizar los documentos con el
nombre que el usuario disponga.

Prioridad

Media

Restricciones

Los documentos deberán estar convertidos
previamente en su respectivo formato.
El usuario deberá estar registrado en la
plataforma previamente.

Interacción humano tecnología

Si

Interacción tecnología tecnología

No

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 17

Nombre de requisito

Gestionar Roles de Usuario

Componente

Sistema de autenticación, Módulo de permisos
24

Library Creator Requisitos Funcionales

Característica asociada

Control de acceso basado en roles

Descripción del requisito

El sistema debe asignar un rol a cada usuario
registrado para controlar el acceso a las
funciones y los límites de la plataforma.

características

Rol "Gratuito": Es el rol por defecto al
registrarse. Tiene acceso a funciones limitadas.
Rol "Premium": Rol de pago. Tiene límites
ampliados o ilimitados.
Rol "Administrador": Tiene acceso a funciones
de gestión del sistema, incluyendo la capacidad
de modificar los roles de otros usuarios.

Prioridad

Alta

Restricciones

El rol "Administrador" no puede ser obtenido por
un usuario regular; debe ser asignado
manualmente (ej. directamente en la base de
datos).

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

25

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 18

Nombre de requisito

Visualizar Planes de Suscripción

Componente

Interfaz de Usuario, Sección de Perfil

Característica asociada

Presentación de planes (Página de precios)

Descripción del requisito

El sistema debe mostrar al usuario los diferentes
planes (Gratuito y Premium) y una comparativa
de sus características y límites.

características

El usuario podrá acceder a una sección (ej.
"Mi Suscripción") donde se detallan los
beneficios de ascender al rol "Premium".

Prioridad

Media

Restricciones

La información de precios debe ser clara y estar
actualizada.

Interacción humano tecnología

Si

Interacción tecnología tecnología

No

26

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 19

Nombre de requisito

Procesar Pago y Suscripción Automática

Componente

"Pasarela de Pagos (API externa), Módulo de
Suscripción"

Característica asociada

Integración de pagos y activación automática de
servicios

Descripción del requisito

El sistema debe integrar un proveedor de pagos
seguro para permitir al usuario adquirir el plan
"Premium" y activar los beneficios de forma
inmediata tras la confirmación exitosa de la
transacción.

características

1. Al seleccionar "Mejorar a Premium", el sistema
redirigirá a la interfaz segura del proveedor de
pagos.

2. El sistema debe ser capaz de recibir la
confirmación de pago (Webhook/API).

3. Al recibir la confirmación de "pago exitoso", el
sistema cambiará automáticamente el rol del
usuario de "Gratuito" a "Premium" en la base de
datos.

27

Library Creator Requisitos Funcionales

Prioridad

Alta

Restricciones

El sistema debe manejar errores de transacción
(fondos insuficientes, tarjeta rechazada) e
informar al usuario. La conexión debe ser segura
(HTTPS).

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 20

Nombre de requisito

Gestionar Usuarios y Transacciones (Admin)

Componente

Panel de Administración, Historial de Pagos

Característica asociada

Auditoría y soporte de usuarios

Descripción del requisito

El sistema debe proveer una interfaz para que el
"Administrador" gestione los usuarios y visualice
el estado de las suscripciones, conservando la
capacidad de modificar roles en casos
excepcionales (soporte técnico).

28

Library Creator Requisitos Funcionales

características

1. El "Administrador" podrá visualizar el historial
de pagos y el estado actual de la suscripción de
cualquier usuario.

2. El "Administrador" conserva el permiso de
revocar o asignar roles manualmente para
resolver disputas o errores, aunque el proceso
principal sea automático.

Prioridad

Alta

Restricciones

Esta interfaz es exclusiva para el rol
"Administrador". El administrador no debe tener
acceso a datos sensibles de pago (como
números completos de tarjetas de crédito), solo a
los metadatos de la transacción (ID, fecha,
estado).

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

29

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 21

Nombre de requisito

Recuperación de Credenciales

Componente

"Sistema de Autenticación, Servicio de Correo
(SMTP)"

Característica asociada

Seguridad y recuperación de cuentas

Descripción del requisito

El sistema debe permitir a los usuarios
restablecer su contraseña en caso de olvido,
mediante un proceso de verificación por correo
electrónico.

características

1. El usuario podrá solicitar un enlace de
recuperación ingresando su correo registrado.

2. El sistema enviará un correo con un token o
enlace único.

3. El usuario podrá definir una nueva contraseña
que cumpla con las políticas de seguridad
(mayúsculas, números, mín. 8 caracteres).

Prioridad

Alta

30

Library Creator Requisitos Funcionales

Restricciones

El enlace de recuperación debe tener un tiempo
de expiración limitado por seguridad. El correo
ingresado debe existir en la base de datos.

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

RF_ 22

Nombre de requisito

Visualizar Estadísticas del Sistema

Componente

"Panel de Administración, Dashboard"

Característica asociada

Inteligencia de Negocios y Monitoreo

Descripción del requisito

El sistema debe generar un tablero de control
(Dashboard) visual para el Administrador con
métricas clave del estado de la plataforma en
tiempo real.

características

1. Visualización de contadores de usuarios
totales, activos y suspendidos.

31

Library Creator Requisitos Funcionales

2. Gráficos o métricas sobre la distribución de
planes (Cantidad de usuarios Gratuitos vs
Premium).

3. Reporte de solicitudes procesadas en el mes
actual.

Prioridad

Media

Restricciones

Acceso exclusivo para el rol "Administrador". Los
datos deben actualizarse en tiempo real o bajo
demanda.

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

Library Creator Requisitos Funcionales

ID_Requisito

Nombre de requisito

RF_ 23

Gestión y Visualización de Modelos IA

32

Library Creator Requisitos Funcionales

Componente

"Motor de IA (Poly), Configuración"

Característica asociada

Versionamiento y Mantenimiento de IA

Descripción del requisito

El sistema debe permitir identificar y gestionar la
versión del modelo de Inteligencia Artificial (Poly)
que está operando en la plataforma.

características

1. El sistema mostrará en el perfil o configuración
la versión actual del modelo (ej. Poly-AI v2.4).

2. El sistema debe permitir al backend enrutar las
peticiones al modelo correspondiente según la
configuración global o el plan del usuario.

3. Visualización de notas de la versión
(changelog) para informar mejoras.

Prioridad

Media

Restricciones

La configuración técnica del modelo solo es
modificable por los desarrolladores o
administradores de alto nivel.

Interacción humano tecnología

Si

Interacción tecnología tecnología

Si

33

Requisitos no funcionales (RNF)
Campo
ID
Nombre
Categoría
Descripción

Contenido
1 RNF
Seguridad de la Base Datos
Seguridad
La DB debe contar con un sistema de
seguridad que permita proteger los
datos sensibles del usuario, evitando
exponerlos al publico

Métrica

Los datos sensibles del usuario deben
estar cifrados (ej. con AES-256 o bcrypt
para contraseñas) y solo accesibles por
usuarios autenticados con sesión activa.

Prioridad

Alta

Campo
ID
Nombre
Categoría
Descripción

Contenido
2 RNF
Eficiencia de la AI
Eficiencia
Poly (AI) debe responder a las
peticiones del usuario siguiendo
detalladamente sus instrucciones
brindando respuestas concretas y
eficientes.

Métrica

La IA debe responder a las peticiones del
usuario en un tiempo máximo de 3 segundos
bajo condiciones de carga ligera.

Prioridad

Alta

Campo
ID
Nombre
Categoría
Descripción

Contenido
3 RNF
Rendimiento del sistema
Rendimiento
El sistema debe tener un rendimiento
eficiente, evitando sobrecargas y
cargas lentas y pesadas.

Métrica

El sistema debe cargar en un máximo de 5
segundos el contenido bajo cargas ligeras
de contenido

Prioridad

Alta

34

Campo
ID
Nombre
Categoría
Descripción

Contenido
4 RNF
Mantenibilidad del sistema
Mantenibilidad
El sistema debe estar construido de
forma modular, permitiendo realizar
cambios, correcciones o mejoras sin
afectar otros componentes del
sistema.

Métrica

Cualquier modificación o corrección en un
módulo del sistema debe poder realizarse
en un máximo de 2 horas sin generar fallos
en los demás módulos.

Prioridad

Alta

Campo
ID
Nombre
Categoría
Descripción

Contenido
5 RNF
Eficiencia de almacenamiento
Eficiencia
El sistema debe gestionar el
almacenamiento de contenido de
forma eficiente y dinámica, evitando
que el peso acumulado de los
archivos afecte el rendimiento de la
plataforma.

Métrica

El sistema debe almacenar y recuperar
archivos de hasta 10 MB en un tiempo
máximo de 3 segundos bajo condiciones
de carga ligera.

Prioridad

Alta

Conclusión
Revisando y observando a lo largo de la plataforma se pudo intuir que a pesar
de que alterna a la escritura a mano, pudo fomentar a creatividad de generar
nuevas ideas de algo que ya existe, incluir formas de ver diferente lo habitual y
dar al paso una gran imaginación de literatura desarrollada con las nuevas
tecnologías. Este proyecto es importante porque permite dar posibilidades a
esas cosas que quedaron terminadas y darle un camino distinto con el que se
imbuyeron desde un principio. Fomenta la capacidad de expandir más el
35

sentido crítico para ver un mundo de posibilidades dejando de lado lo igual y
accediendo a un pequeño mundo de imaginaciones creativas.

Bibliografía
“Este proyecto es original de Keiner arismendy, ideas e información son propias
del autor.”

36

37

