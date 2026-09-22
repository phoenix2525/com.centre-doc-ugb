package sn.ugb.centredoc.dao.jdbc;

import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.dao.DocumentDAO;
import sn.ugb.centredoc.model.Document;
import sn.ugb.centredoc.model.NiveauAcces;
import sn.ugb.centredoc.model.TypeDocument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC pour la gestion du fonds documentaire.
 */
public class DocumentDAOJdbc implements DocumentDAO {

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setIdDocument(rs.getInt("id_document"));
        doc.setTitre(rs.getString("titre"));
        doc.setAuteur(rs.getString("auteur"));
        doc.setEncadrant(rs.getString("encadrant"));
        doc.setAnnee(rs.getInt("annee"));
        doc.setType(TypeDocument.valueOf(rs.getString("type")));
        doc.setIdUfr(rs.getInt("id_ufr"));
        doc.setNomUfr(rs.getString("ufr_nom"));
        doc.setDiscipline(rs.getString("discipline"));
        doc.setResume(rs.getString("resume"));
        doc.setMotsCles(rs.getString("mots_cles"));
        doc.setCheminPdf(rs.getString("chemin_pdf"));
        doc.setNiveauAcces(NiveauAcces.valueOf(rs.getString("niveau_acces")));
        doc.setDateAjout(rs.getTimestamp("date_ajout"));
        return doc;
    }

    private final String BASE_SELECT =
            "SELECT d.id_document, d.titre, d.auteur, d.encadrant, d.annee, d.type, " +
            "       d.id_ufr, d.discipline, d.resume, d.mots_cles, d.chemin_pdf, d.niveau_acces, d.date_ajout, " +
            "       f.nom AS ufr_nom " +
            "FROM documents d " +
            "INNER JOIN ufr f ON d.id_ufr = f.id_ufr ";

    @Override
    public List<Document> listerTous() throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY d.annee DESC, d.titre ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSetToDocument(rs));
            }
        }
        return liste;
    }

    @Override
    public List<Document> listerParUfr(int idUfr) throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE d.id_ufr = ? ORDER BY d.annee DESC, d.titre ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUfr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToDocument(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public List<Document> rechercher(String titre, String auteur, String motCle, Integer annee, Integer idUfr, String discipline) throws SQLException {
        List<Document> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (titre != null && !titre.trim().isEmpty()) {
            sql.append("AND LOWER(d.titre) LIKE ? ");
            params.add("%" + titre.trim().toLowerCase() + "%");
        }
        if (auteur != null && !auteur.trim().isEmpty()) {
            sql.append("AND LOWER(d.auteur) LIKE ? ");
            params.add("%" + auteur.trim().toLowerCase() + "%");
        }
        if (motCle != null && !motCle.trim().isEmpty()) {
            sql.append("AND LOWER(d.mots_cles) LIKE ? ");
            params.add("%" + motCle.trim().toLowerCase() + "%");
        }
        if (annee != null && annee > 0) {
            sql.append("AND d.annee = ? ");
            params.add(annee);
        }
        if (idUfr != null && idUfr > 0) {
            sql.append("AND d.id_ufr = ? ");
            params.add(idUfr);
        }
        if (discipline != null && !discipline.trim().isEmpty()) {
            sql.append("AND LOWER(d.discipline) LIKE ? ");
            params.add("%" + discipline.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY d.annee DESC, d.titre ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, (String) p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToDocument(rs));
                }
            }
        }
        return liste;
    }

    @Override
    public Document trouverParId(int idDocument) throws SQLException {
        String sql = BASE_SELECT + "WHERE d.id_document = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDocument);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void ajouter(Document doc) throws SQLException {
        String sql = "INSERT INTO documents (titre, auteur, encadrant, annee, type, id_ufr, discipline, resume, mots_cles, chemin_pdf, niveau_acces) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doc.getTitre());
            ps.setString(2, doc.getAuteur());
            ps.setString(3, doc.getEncadrant());
            ps.setInt(4, doc.getAnnee());
            ps.setString(5, doc.getType().name());
            ps.setInt(6, doc.getIdUfr());
            ps.setString(7, doc.getDiscipline());
            ps.setString(8, doc.getResume());
            ps.setString(9, doc.getMotsCles());
            ps.setString(10, doc.getCheminPdf());
            ps.setString(11, doc.getNiveauAcces().name());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    doc.setIdDocument(gk.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Document doc) throws SQLException {
        String sql = "UPDATE documents SET titre = ?, auteur = ?, encadrant = ?, annee = ?, type = ?, " +
                     "       id_ufr = ?, discipline = ?, resume = ?, mots_cles = ?, chemin_pdf = ?, niveau_acces = ? " +
                     "WHERE id_document = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getTitre());
            ps.setString(2, doc.getAuteur());
            ps.setString(3, doc.getEncadrant());
            ps.setInt(4, doc.getAnnee());
            ps.setString(5, doc.getType().name());
            ps.setInt(6, doc.getIdUfr());
            ps.setString(7, doc.getDiscipline());
            ps.setString(8, doc.getResume());
            ps.setString(9, doc.getMotsCles());
            ps.setString(10, doc.getCheminPdf());
            ps.setString(11, doc.getNiveauAcces().name());
            ps.setInt(12, doc.getIdDocument());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int idDocument) throws SQLException {
        String sql = "DELETE FROM documents WHERE id_document = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDocument);
            ps.executeUpdate();
        }
    }
}
