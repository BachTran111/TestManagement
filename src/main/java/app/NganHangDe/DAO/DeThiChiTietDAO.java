package app.NganHangDe.DAO;

import app.NganHangDe.Model.DeThiChiTiet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeThiChiTietDAO {
    public void add(DeThiChiTiet chiTiet) throws SQLException {
        String sql = "INSERT INTO de_thi_chi_tiet (de_thi_id, cau_hoi_id, question_number) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chiTiet.getDeThiId());
            stmt.setInt(2, chiTiet.getCauHoiId());
            stmt.setInt(3, chiTiet.getQuestionNumber());
            stmt.executeUpdate();
        }
    }

    public List<DeThiChiTiet> findByDeThiId(int deThiId) throws SQLException {
        List<DeThiChiTiet> list = new ArrayList<>();
        String sql = "SELECT * FROM de_thi_chi_tiet WHERE de_thi_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deThiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DeThiChiTiet ct = new DeThiChiTiet();
                ct.setId(rs.getInt("id"));
                ct.setDeThiId(rs.getInt("de_thi_id"));
                ct.setCauHoiId(rs.getInt("cau_hoi_id"));
                ct.setQuestionNumber(rs.getInt("question_number"));
                list.add(ct);
            }
        }
        return list;
    }

    public void deleteByDeThiId(int deThiId) throws SQLException {
        String sql = "DELETE FROM de_thi_chi_tiet WHERE de_thi_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deThiId);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM de_thi_chi_tiet WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
