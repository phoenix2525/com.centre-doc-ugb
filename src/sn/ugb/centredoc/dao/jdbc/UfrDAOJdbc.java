package sn.ugb.centredoc.dao.jdbc;

import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.dao.UfrDAO;
import sn.ugb.centredoc.model.Ufr;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC pour la gestion des UFR.
 */
public class UfrDAOJdbc implements UfrDAO {

    @Override
    public List<Ufr> listerTous() throws SQLException {
        List<Ufr> liste = new ArrayList<>();
        String sql = "SELECT id_ufr, code, nom FROM ufr ORDER BY id_ufr";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Ufr(
                        rs.getInt("id_ufr"),
                        rs.getString("code"),
                        rs.getString("nom")
                ));
            }
        }
        return liste;
    }

    @Override
    public Ufr trouverParId(int idUfr) throws SQLException {
        String sql = "SELECT id_ufr, code, nom FROM ufr WHERE id_ufr = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUfr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ufr(
                            rs.getInt("id_ufr"),
                            rs.getString("code"),
                            rs.getString("nom")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public Ufr trouverParCode(String code) throws SQLException {
        String sql = "SELECT id_ufr, code, nom FROM ufr WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ufr(
                            rs.getInt("id_ufr"),
                            rs.getString("code"),
                            rs.getString("nom")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public void ajouter(Ufr ufr) throws SQLException {
        String sql = "INSERT INTO ufr (code, nom) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ufr.getCode());
            ps.setString(2, ufr.getNom());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ufr.setIdUfr(generatedKeys.getInt(1));
                }
            }
        }
    }
}
