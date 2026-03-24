Estructura de recursos SQL del backend

- `schema/`
  Contiene el esquema principal listo para crear la base desde cero.

- `seed/`
  Contiene el reset, el seed demo y las credenciales asociadas a esos datos.

- `queries/`
  Contiene consultas de apoyo para QA, validación manual y revisión rápida de datos.

Flujo recomendado:

1. Ejecutar `schema/LC_v3_SQL_final.sql`
2. Si quieres dejar la base limpia para demo, ejecutar `seed/LC_reset_datos_demo.sql`
3. Cargar datos demo con `seed/LC_demo_seed_completo.sql`
4. Validar con `queries/pruebas.sql`
