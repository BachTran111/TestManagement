package app.NganHangDe.DAO;

import app.NganHangDe.Model.DeThi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeThiDAO {
    public void create(DeThi deThi) throws SQLException {
        String sql = "INSERT INTO de_thi (name, description, date) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, deThi.getName());
            stmt.setString(2, deThi.getDescription());
            stmt.setDate(3, new java.sql.Date(deThi.getDate().getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    deThi.setId(rs.getInt(1));
                }
            }
        }
    }

    public DeThi findById(int id) throws SQLException {
        String sql = "SELECT * FROM de_thi WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DeThi deThi = new DeThi();
                    deThi.setId(rs.getInt("id"));
                    deThi.setName(rs.getString("name"));
                    deThi.setDescription(rs.getString("description"));
                    deThi.setDate(rs.getDate("date"));
                    return deThi;
                }
            }
        }
        return null;
    }

    public List<DeThi> findAll() throws SQLException {
        List<DeThi> list = new ArrayList<>();
        String sql = "SELECT * FROM de_thi";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DeThi deThi = new DeThi();
                deThi.setId(rs.getInt("id"));
                deThi.setName(rs.getString("name"));
                deThi.setDescription(rs.getString("description"));
                deThi.setDate(rs.getDate("date"));
                list.add(deThi);
            }
        }
        return list;
    }

    public void update(DeThi deThi) throws SQLException {
        String sql = "UPDATE de_thi SET name = ?, description = ?, date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deThi.getName());
            stmt.setString(2, deThi.getDescription());
            stmt.setDate(3, new java.sql.Date(deThi.getDate().getTime()));
            stmt.setInt(4, deThi.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM de_thi WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

//    public int countQuestions(int deThiId) throws SQLException {
//        String sql = "SELECT COUNT(*) FROM de_thi_chi_tiet WHERE de_thi_id = ?";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setInt(1, deThiId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//            }
//        }
//        return 0;
//    }
}
