package com.libraryai.backend.config;

import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Manejo de conexion a base de datos.
 */
public class DbConnection {
    private static final String URL = Dotenv.load().get("DB_URL");
    private static final String USER = Dotenv.load().get("DB_USER");
    private static final String PASSWD_DB = Dotenv.load().get("DB_PASSWD");

    /**
     * Intenta establecer una conexión con la base de datos utilizando las
     * credenciales
     * proporcionadas en las variables de entorno.
     *
     * @return Connection Objeto de conexión a la base de datos.
     * @throws SQLException Si ocurre un error al intentar conectar.
     */
    public static Connection getConnection() throws SQLException {
        // Validación: Verifica si alguna de las variables vitales está vacía
        if (URL.isEmpty() && USER.isEmpty() && PASSWD_DB.isEmpty()) {
            System.err.println(
                    "ERROR CRÍTICO: Faltan variables de entorno para la base de datos (DB_URL, DB_USER, DB_PASSWD)."
                );
        }

        System.out.println("|-- Conexion DB --|");
        System.out.println("Intentando conexión con DB...");

        Connection connection = DriverManager.getConnection(URL, USER, PASSWD_DB);
        System.out.println("Conexión exitosa\n");

        return connection;
    }
}
