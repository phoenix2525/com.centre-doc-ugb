package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.dao.jdbc.*;
import sn.ugb.centredoc.dao.memory.*;

/**
 * Fabrique abstraite (Factory Pattern) pour l'instanciation des DAOs.
 * Permet de basculer de manière transparente entre :
 * 1. Le mode standard JDBC MySQL (requis pour l'évaluation officielle)
 * 2. Le mode Mémoire basé sur les collections Java (utilisé en démonstration si MySQL n'est pas démarré)
 */
public class DAOFactory {
    private static boolean useJdbc;
    private static boolean initialized = false;

    private static UfrDAO ufrDAO;
    private static UtilisateurDAO utilisateurDAO;
    private static DocumentDAO documentDAO;
    private static TelechargementDAO telechargementDAO;

    private static synchronized void init() {
        if (!initialized) {
            // Teste la disponibilité de MySQL sur le port 3306
            boolean mysqlReachable = DBConnection.isMySQLAvailable();
            setUseJdbc(mysqlReachable);
            initialized = true;
        }
    }

    public static synchronized void setUseJdbc(boolean jdbc) {
        useJdbc = jdbc;
        if (useJdbc) {
            ufrDAO = new UfrDAOJdbc();
            utilisateurDAO = new UtilisateurDAOJdbc();
            documentDAO = new DocumentDAOJdbc();
            telechargementDAO = new TelechargementDAOJdbc();
        } else {
            ufrDAO = new UfrDAOMemory();
            utilisateurDAO = new UtilisateurDAOMemory();
            documentDAO = new DocumentDAOMemory();
            telechargementDAO = new TelechargementDAOMemory();
        }
    }

    public static boolean isUsingJdbc() {
        init();
        return useJdbc;
    }

    public static UfrDAO getUfrDAO() {
        init();
        return ufrDAO;
    }

    public static UtilisateurDAO getUtilisateurDAO() {
        init();
        return utilisateurDAO;
    }

    public static DocumentDAO getDocumentDAO() {
        init();
        return documentDAO;
    }

    public static TelechargementDAO getTelechargementDAO() {
        init();
        return telechargementDAO;
    }
}
