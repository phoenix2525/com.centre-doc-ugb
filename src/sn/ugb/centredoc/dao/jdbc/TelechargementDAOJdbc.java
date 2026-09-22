package sn.ugb.centredoc.dao.jdbc;

import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.dao.TelechargementDAO;
import sn.ugb.centredoc.model.Telechargement;

import java.sql.*;
import java.util.*;

/**
 * Implémentation JDBC pour la gestion des téléchargements et des statistiques.
 * Utilise java.util.List et java.util.Map (Collections standard de POO2).
 */
public class TelechargementDAOJdbc implements TelechargementDAO {

    private Telechargement mapResultSet(ResultSet rs) throws SQLException {
        Telechargement t = new Telechargement();
        t.setIdTelechargement(rs.getInt("id_telechargement"));
        t.setIdUtilisateur(rs.getInt("id_utilisateur"));
        t.setIdDocument(rs.getInt("id_document"));
        t.setDateTelechargement(rs.getTimestamp("date_telechargement"));

        t.setNomUtilisateur(rs.getString("user_prenom") + " " + rs.getString("user_nom"));
        t.setEmailUtilisateur(rs.getString("user_email"));
        t.setCodeEtudiant(rs.getString("user_code"));
        t.setTitreDocument(rs.getString("doc_titre"));
        t.setUfrDocument(rs.getString("ufr_nom"));
        return t;
    }

    private final String BASE_SELECT =
            "SELECT t.id_telechargement, t.id_utilisateur, t.id_document, t.date_telechargement, " +
            "       u.nom AS user_nom, u.prenom AS user_prenom, u.email AS user_email, u.code_etudiant AS user_code, " +
            "       d.titre AS doc_titre, d.id_ufr, f.nom AS ufr_nom " +
            "FROM telechargements t " +
            "INNER JOIN utilisateurs u ON t.id_utilisateur = u.id_utilisateur " +
            "INNER JOIN documents d ON t.id_document = d.id_document " +
            "INNER JOIN ufr f ON d.id_ufr = f.id_ufr ";

    @Override
    public void enregistrer(Telechargement t) throws SQLException {
        String sql = "INSERT INTO telechargements (id_utilisateur, id_document, date_telechargement) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdUtilisateur());
            ps.setInt(2, t.getIdDocument());
            Timestamp ts = (t.getDateTelechargement() != null) ? t.getDateTelechargement() : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(3, ts);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    t.setIdTelechargement(gk.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Telechargement> listerTous() throws SQLException {
        List<Telechargement> liste = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY t.date_telechargement DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        }
        return liste;
    }

    @Override
    public List<Telechargement> listerParUtilisateur(int idUtilisateur) throws SQLException {
        List<Telechargement> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.id_utilisateur = ? ORDER BY t.date_telechargement DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSet(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public List<Telechargement> listerParDocument(int idDocument) throws SQLException {
        List<Telechargement> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.id_document = ? ORDER BY t.date_telechargement DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDocument);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSet(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public List<Telechargement> listerParUfr(int idUfr) throws SQLException {
        List<Telechargement> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE d.id_ufr = ? ORDER BY t.date_telechargement DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUfr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSet(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public Map<String, Integer> getStatsTopDocuments(int limit) throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT d.titre, COUNT(t.id_telechargement) AS nb " +
                     "FROM telechargements t " +
                     "INNER JOIN documents d ON t.id_document = d.id_document " +
                     "GROUP BY d.id_document, d.titre " +
                     "ORDER BY nb DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("titre"), rs.getInt("nb"));
                }
            }
        }
        return stats;
    }

    @Override
    public Map<String, Integer> getStatsParUfr() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT f.code, COUNT(t.id_telechargement) AS nb " +
                     "FROM ufr f " +
                     "LEFT JOIN documents d ON f.id_ufr = d.id_ufr " +
                     "LEFT JOIN telechargements t ON d.id_document = t.id_document " +
                     "GROUP BY f.id_ufr, f.code " +
                     "ORDER BY nb DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("code"), rs.getInt("nb"));
            }
        }
        return stats;
    }

    @Override
    public Map<String, Integer> getStatsParDiscipline() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT d.discipline, COUNT(d.id_document) AS nb " +
                     "FROM documents d " +
                     "GROUP BY d.discipline " +
                     "ORDER BY nb DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("discipline"), rs.getInt("nb"));
            }
        }
        return stats;
    }

    @Override
    public Map<Integer, Integer> getStatsParAnnee() throws SQLException {
        Map<Integer, Integer> stats = new TreeMap<>();
        String sql = "SELECT d.annee, COUNT(d.id_document) AS nb " +
                     "FROM documents d " +
                     "GROUP BY d.annee " +
                     "ORDER BY d.annee ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getInt("annee"), rs.getInt("nb"));
            }
        }
        return stats;
    }
}
