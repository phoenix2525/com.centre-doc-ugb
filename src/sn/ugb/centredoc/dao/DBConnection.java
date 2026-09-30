package sn.ugb.centredoc.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestionnaire de connexion JDBC pour la base de données MySQL 'centre_doc'.
 * Utilise les bonnes pratiques académiques (PreparedStatement, gestion des fermetures).
 */
public class DBConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "centre_doc";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = ""; // Standard XAMPP/WAMP par défaut

    private static String host = DEFAULT_HOST;
    private static int port = DEFAULT_PORT;
    private static String database = DEFAULT_DATABASE;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        try {
            // Chargement explicite du pilote JDBC MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Avertissement : Pilote JDBC MySQL non chargé dans le classpath : " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    public static String getUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database 
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), user, password);
    }

    /**
     * Teste si le serveur MySQL est actuellement joignable sur le port configuré.
     * @return true si la connexion réussit, false sinon
     */
    public static boolean isMySQLAvailable() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&connectTimeout=1000",
                user, password)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initialise la base de données 'centre_doc' et exécute les scripts DDL/DML depuis un fichier SQL.
     */
    public static void initialiserBaseDepuisScript(File scriptSql) throws Exception {
        if (!scriptSql.exists()) {
            throw new IllegalArgumentException("Fichier script SQL introuvable : " + scriptSql.getAbsolutePath());
        }

        // Connexion au serveur MySQL sans spécifier de base (pour pouvoir créer centre_doc)
        String rootUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true&characterEncoding=UTF-8";
        try (Connection conn = DriverManager.getConnection(rootUrl, user, password);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(scriptSql, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignorer les commentaires
                if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String query = sb.toString().trim();
                    if (!query.isEmpty()) {
                        stmt.execute(query);
                    }
                    sb.setLength(0);
                }
            }
        }
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }

    public static void setCredentials(String hostParam, int portParam, String dbParam, String userParam, String passParam) {
        host = hostParam;
        port = portParam;
        database = dbParam;
        user = userParam;
        password = passParam;
    }
}
