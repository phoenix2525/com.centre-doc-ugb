package sn.ugb.centredoc.dao.jdbc;

import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.dao.DemandeAccesDAO;
import sn.ugb.centredoc.model.DemandeAcces;
import sn.ugb.centredoc.model.StatutDemande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC (MySQL, PreparedStatement + try-with-resources) pour
 * la gestion des demandes d'accès aux documents sous embargo (Bonus 3.3).
 */
public class DemandeAccesDAOJdbc implements DemandeAccesDAO {

    private DemandeAcces mapResultSetToDemande(ResultSet rs) throws SQLException {
        DemandeAcces da = new DemandeAcces();
        da.setIdDemande(rs.getInt("id_demande"));
        da.setIdEtudiant(rs.getInt("id_etudiant"));
        da.setIdDocument(rs.getInt("id_document"));
        da.setMotif(rs.getString("motif"));
        da.setStatut(StatutDemande.valueOf(rs.getString("statut")));
        da.setDateDemande(rs.getTimestamp("date_demande"));
        da.setNomEtudiant(rs.getString("nom_etudiant"));
        da.setEmailEtudiant(rs.getString("email_etudiant"));
        da.setCodeEtudiant(rs.getString("code_etudiant"));
        da.setTitreDocument(rs.getString("titre_document"));
        da.setUfrDocument(rs.getString("ufr_document"));
        return da;
    }

    private final String BASE_SELECT =
            "SELECT da.id_demande, da.id_etudiant, da.id_document, da.motif, da.statut, da.date_demande, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_etudiant, u.email AS email_etudiant, u.code_etudiant, " +
            "       d.titre AS titre_document, f.nom AS ufr_document " +
            "FROM demande_acces da " +
            "INNER JOIN utilisateurs u ON da.id_etudiant = u.id_utilisateur " +
            "INNER JOIN documents d ON da.id_document = d.id_document " +
            "INNER JOIN ufr f ON d.id_ufr = f.id_ufr ";

    @Override
    public List<DemandeAcces> listerTous() throws SQLException {
        List<DemandeAcces> liste = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY da.date_demande DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSetToDemande(rs));
            }
        }
        return liste;
    }

    @Override
    public List<DemandeAcces> listerParEtudiant(int idEtudiant) throws SQLException {
        List<DemandeAcces> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE da.id_etudiant = ? ORDER BY da.date_demande DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEtudiant);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToDemande(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public List<DemandeAcces> listerParUfr(int idUfr) throws SQLException {
        // Règle 4 : une demande est rattachée à l'UFR du document visé
        List<DemandeAcces> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE d.id_ufr = ? ORDER BY da.date_demande DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUfr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToDemande(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public DemandeAcces trouverParId(int idDemande) throws SQLException {
        String sql = BASE_SELECT + "WHERE da.id_demande = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDemande(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void ajouter(DemandeAcces demande) throws SQLException {
        String sql = "INSERT INTO demande_acces (id_etudiant, id_document, motif, statut) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, demande.getIdEtudiant());
            ps.setInt(2, demande.getIdDocument());
            ps.setString(3, demande.getMotif());
            ps.setString(4, demande.getStatut().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    demande.setIdDemande(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(DemandeAcces demande) throws SQLException {
        String sql = "UPDATE demande_acces SET motif = ?, statut = ? WHERE id_demande = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, demande.getMotif());
            ps.setString(2, demande.getStatut().name());
            ps.setInt(3, demande.getIdDemande());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int idDemande) throws SQLException {
        String sql = "DELETE FROM demande_acces WHERE id_demande = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            ps.executeUpdate();
        }
    }
}
