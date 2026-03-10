package com.libraryai.backend.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Manejo de conexión a base de datos.
 */
public class DatabaseConnection {
    private static final Dotenv ENV = Dotenv.load();
    private static final String URL = ENV.get("DB_URL");
    private static final String USER = ENV.get("DB_USER");
    private static final String PASSWD_DB = ENV.get("DB_PASSWD");

    /**
     * Intenta establecer una conexión con la base de datos utilizando las
     * credenciales
     * proporcionadas en las variables de entorno.
     *
     * @return Connection Objeto de conexión a la base de datos.
     * @throws SQLException Si ocurre un error al intentar conectar.
     */
    public static Connection getConnection() throws SQLException {
        // La conexión no debe intentar arrancar con configuración incompleta.
        if (isBlank(URL) || isBlank(USER) || isBlank(PASSWD_DB)) {
            throw new SQLException(
                    "Faltan variables de entorno para la base de datos (DB_URL, DB_USER, DB_PASSWD).");
        }

        System.out.println("|-- Conexión DB --|");
        System.out.println("Intentando conexión con DB...");

        Connection connection = DriverManager.getConnection(URL, USER, PASSWD_DB);
        System.out.println("Conexión exitosa\n");

        return connection;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
