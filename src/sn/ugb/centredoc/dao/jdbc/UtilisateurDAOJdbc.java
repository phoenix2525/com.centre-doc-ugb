package sn.ugb.centredoc.dao.jdbc;

import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.dao.UtilisateurDAO;
import sn.ugb.centredoc.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC pour la gestion des utilisateurs.
 */
public class UtilisateurDAOJdbc implements UtilisateurDAO {

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_utilisateur");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        String motDePasse = rs.getString("mot_de_passe");
        String roleStr = rs.getString("role");
        int idUfrInt = rs.getInt("id_ufr");
        Integer idUfr = rs.wasNull() ? null : idUfrInt;
        String nomUfr = rs.getString("ufr_nom");
        String codeEtudiant = rs.getString("code_etudiant");

        Role role = Role.valueOf(roleStr);
        Utilisateur user;

        switch (role) {
            case ADMIN:
                user = new Administrateur(id, nom, prenom, email, motDePasse);
                break;
            case GESTIONNAIRE:
                user = new Gestionnaire(id, nom, prenom, email, motDePasse, (idUfr != null ? idUfr : 0));
                break;
            case ETUDIANT:
                user = new Etudiant(id, nom, prenom, email, codeEtudiant, idUfr);
                break;
            default:
                throw new IllegalArgumentException("Rôle inconnu : " + roleStr);
        }

        user.setIdUfr(idUfr);
        user.setNomUfr(nomUfr);
        user.setCodeEtudiant(codeEtudiant);
        return user;
    }

    private final String BASE_SELECT = 
            "SELECT u.id_utilisateur, u.nom, u.prenom, u.email, u.mot_de_passe, u.role, u.id_ufr, u.code_etudiant, " +
            "       f.nom AS ufr_nom " +
            "FROM utilisateurs u " +
            "LEFT JOIN ufr f ON u.id_ufr = f.id_ufr ";

    @Override
    public List<Utilisateur> listerTous() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY u.id_utilisateur";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSetToUtilisateur(rs));
            }
        }
        return liste;
    }

    @Override
    public List<Utilisateur> rechercher(String critere) throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = BASE_SELECT + 
                "WHERE LOWER(u.nom) LIKE ? OR LOWER(u.prenom) LIKE ? OR LOWER(u.email) LIKE ? " +
                "ORDER BY u.nom, u.prenom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (critere != null ? critere.trim().toLowerCase() : "") + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToUtilisateur(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public List<Utilisateur> listerParRole(Role role) throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE u.role = ? ORDER BY u.nom, u.prenom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToUtilisateur(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public Utilisateur trouverParId(int idUtilisateur) throws SQLException {
        String sql = BASE_SELECT + "WHERE u.id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Utilisateur trouverParEmail(String email) throws SQLException {
        String sql = BASE_SELECT + "WHERE LOWER(u.email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email != null ? email.trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Utilisateur trouverEtudiant(String prenom, String nom, String email, String codeEtudiant) throws SQLException {
        String sql = BASE_SELECT + 
                "WHERE u.role = 'ETUDIANT' " +
                "  AND LOWER(u.prenom) = ? " +
                "  AND LOWER(u.nom) = ? " +
                "  AND LOWER(u.email) = ? " +
                "  AND LOWER(u.code_etudiant) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prenom != null ? prenom.trim().toLowerCase() : "");
            ps.setString(2, nom != null ? nom.trim().toLowerCase() : "");
            ps.setString(3, email != null ? email.trim().toLowerCase() : "");
            ps.setString(4, codeEtudiant != null ? codeEtudiant.trim().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void ajouter(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, id_ufr, code_etudiant) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail().trim().toLowerCase());
            ps.setString(4, utilisateur.getMotDePasse());
            ps.setString(5, utilisateur.getRole().name());
            if (utilisateur.getIdUfr() != null && utilisateur.getIdUfr() > 0) {
                ps.setInt(6, utilisateur.getIdUfr());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, utilisateur.getCodeEtudiant());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    utilisateur.setIdUtilisateur(gk.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, id_ufr = ?, code_etudiant = ? " +
                     "WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail().trim().toLowerCase());
            ps.setString(4, utilisateur.getMotDePasse());
            if (utilisateur.getIdUfr() != null && utilisateur.getIdUfr() > 0) {
                ps.setInt(5, utilisateur.getIdUfr());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, utilisateur.getCodeEtudiant());
            ps.setInt(7, utilisateur.getIdUtilisateur());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int idUtilisateur) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existeEmail(String email, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE LOWER(email) = ? AND id_utilisateur != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email != null ? email.trim().toLowerCase() : "");
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean existeCodeEtudiant(String codeEtudiant, int excludeId) throws SQLException {
        if (codeEtudiant == null || codeEtudiant.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE LOWER(code_etudiant) = ? AND id_utilisateur != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codeEtudiant.trim().toLowerCase());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
