package sn.ugb.centredoc.dao;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestionnaire de connexion JDBC multi-SGBD pour l'application Centre de Documentation UGB.
 * Supporte nativement PostgreSQL (recommandé) et MySQL.
 * Gère la persistance de configuration et l'initialisation automatisée du schéma.
 */
public class DBConnection {

    public enum DatabaseType {
        POSTGRESQL("PostgreSQL", 5432, "postgres", "org.postgresql.Driver"),
        MYSQL("MySQL", 3306, "root", "com.mysql.cj.jdbc.Driver");

        private final String nom;
        private final int portParDefaut;
        private final String utilisateurParDefaut;
        private final String driverClass;

        DatabaseType(String nom, int portParDefaut, String utilisateurParDefaut, String driverClass) {
            this.nom = nom;
            this.portParDefaut = portParDefaut;
            this.utilisateurParDefaut = utilisateurParDefaut;
            this.driverClass = driverClass;
        }

        public String getNom() { return nom; }
        public int getPortParDefaut() { return portParDefaut; }
        public String getUtilisateurParDefaut() { return utilisateurParDefaut; }
        public String getDriverClass() { return driverClass; }
    }

    private static final String CONFIG_FILE = "db_config.properties";

    private static DatabaseType databaseType = DatabaseType.POSTGRESQL;
    private static String host = "localhost";
    private static int port = 5432;
    private static String database = "centre_doc";
    private static String user = "postgres";
    private static String password = "";

    static {
        // Enregistrement des pilotes JDBC
        chargerPilote(DatabaseType.POSTGRESQL.getDriverClass());
        chargerPilote(DatabaseType.MYSQL.getDriverClass());

        // Chargement de la configuration existante ou détection automatique
        if (!chargerConfiguration()) {
            detecterServeurParDefaut();
        }
    }

    private static void chargerPilote(String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            System.err.println("Avertissement : Pilote JDBC " + driverClass + " non présent : " + e.getMessage());
        }
    }

    private static void detecterServeurParDefaut() {
        // Si PostgreSQL (5432) est joignable, l'activer en priorité
        if (testerPort("localhost", 5432, 500)) {
            databaseType = DatabaseType.POSTGRESQL;
            port = 5432;
            user = "postgres";
            password = "";
        } else if (testerPort("localhost", 3306, 500)) {
            databaseType = DatabaseType.MYSQL;
            port = 3306;
            user = "root";
            password = "";
        }
    }

    private static boolean testerPort(String h, int p, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(h, p), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private DBConnection() {
    }

    public static String getUrl() {
        return getUrlPourBase(database);
    }

    public static String getUrlPourBase(String nomBase) {
        if (databaseType == DatabaseType.POSTGRESQL) {
            return "jdbc:postgresql://" + host + ":" + port + "/" + (nomBase != null ? nomBase : "postgres");
        } else {
            return "jdbc:mysql://" + host + ":" + port + "/" + (nomBase != null ? nomBase : "")
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), user, password);
    }

    /**
     * Teste si le port du serveur de base de données configuré est accessible.
     */
    public static boolean isPortAvailable() {
        return testerPort(host, port, 1000);
    }

    /**
     * Teste si la connexion directe à la base 'centre_doc' est opérationnelle.
     */
    public static boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rétro-compatibilité pour le code existant appelant isMySQLAvailable.
     */
    public static boolean isMySQLAvailable() {
        return isDatabaseAvailable();
    }

    /**
     * Initialise la base de données et exécute les scripts DDL/DML.
     */
    public static void initialiserBaseDepuisScript(File scriptSql) throws Exception {
        if (scriptSql == null || !scriptSql.exists()) {
            throw new IllegalArgumentException("Fichier script SQL introuvable.");
        }

        if (databaseType == DatabaseType.POSTGRESQL) {
            initialiserPostgreSQL(scriptSql);
        } else {
            initialiserMySQL(scriptSql);
        }
    }

    private static void initialiserPostgreSQL(File scriptSql) throws Exception {
        // 1. Connexion à la base système 'postgres' pour vérifier/créer la base 'centre_doc'
        String systemUrl = getUrlPourBase("postgres");
        try (Connection conn = DriverManager.getConnection(systemUrl, user, password);
             Statement stmt = conn.createStatement()) {

            boolean baseExiste = false;
            try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + database + "'")) {
                if (rs.next()) {
                    baseExiste = true;
                }
            }

            if (!baseExiste) {
                stmt.execute("CREATE DATABASE " + database);
            }
        }

        // 2. Connexion à la base 'centre_doc' pour exécuter le script
        String targetUrl = getUrlPourBase(database);
        try (Connection conn = DriverManager.getConnection(targetUrl, user, password);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(scriptSql, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
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

    private static void initialiserMySQL(File scriptSql) throws Exception {
        String rootUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true&characterEncoding=UTF-8";
        try (Connection conn = DriverManager.getConnection(rootUrl, user, password);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(scriptSql, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
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

    public static DatabaseType getDatabaseType() { return databaseType; }
    public static void setDatabaseType(DatabaseType type) { databaseType = type; }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }

    public static void setCredentials(DatabaseType type, String hostParam, int portParam, String dbParam, String userParam, String passParam) {
        databaseType = type;
        host = hostParam;
        port = portParam;
        database = dbParam;
        user = userParam;
        password = passParam;
        sauvegarderConfiguration();
    }

    public static boolean chargerConfiguration() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return false;
        try (FileInputStream in = new FileInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            String typeStr = p.getProperty("db.type", "POSTGRESQL");
            try {
                databaseType = DatabaseType.valueOf(typeStr.toUpperCase());
            } catch (Exception e) {
                databaseType = DatabaseType.POSTGRESQL;
            }
            host = p.getProperty("db.host", "localhost");
            port = Integer.parseInt(p.getProperty("db.port", String.valueOf(databaseType.getPortParDefaut())));
            database = p.getProperty("db.database", "centre_doc");
            user = p.getProperty("db.user", databaseType.getUtilisateurParDefaut());
            password = p.getProperty("db.password", "");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sauvegarderConfiguration() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            Properties p = new Properties();
            p.setProperty("db.type", databaseType.name());
            p.setProperty("db.host", host);
            p.setProperty("db.port", String.valueOf(port));
            p.setProperty("db.database", database);
            p.setProperty("db.user", user);
            p.setProperty("db.password", password);
            p.store(out, "Configuration Base de Données Centre de Documentation UGB");
        } catch (Exception ignored) {
        }
    }
}
