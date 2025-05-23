package app.NganHangDe.DAO;

import app.NganHangDe.Model.CauHoi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CauHoiDAO {
    // 1. CRUD Cơ bản
    public void create(CauHoi cauHoi) throws SQLException {
        String sql = "INSERT INTO cau_hoi (content, am_thanh_id, type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cauHoi.getContent());
            stmt.setObject(2, cauHoi.getAmThanhId(), Types.INTEGER);
            stmt.setString(3, cauHoi.getType());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cauHoi.setId(rs.getInt(1));
                }
            }
        }
    }

    public CauHoi findById(int id) throws SQLException {
        String sql = "SELECT * FROM cau_hoi WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCauHoi(rs);
                }
            }
        }
        return null;
    }

    public List<CauHoi> findAll() throws SQLException {
        List<CauHoi> cauHois = new ArrayList<>();
        String sql = "SELECT * FROM cau_hoi";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cauHois.add(mapResultSetToCauHoi(rs));
            }
        }
        return cauHois;
    }

    public void update(CauHoi cauHoi) throws SQLException {
        String sql = "UPDATE cau_hoi SET content = ?, am_thanh_id = ?, type = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cauHoi.getContent());
            stmt.setObject(2, cauHoi.getAmThanhId(), Types.INTEGER);
            stmt.setString(3, cauHoi.getType());
            stmt.setInt(4, cauHoi.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM cau_hoi WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // 2. Các phương thức nghiệp vụ đặc thù
    public List<CauHoi> findByDeThi(int deThiId) throws SQLException {
        List<CauHoi> cauHois = new ArrayList<>();
        String sql = "SELECT ch.* FROM cau_hoi ch " +
                "JOIN de_thi_chi_tiet dtct ON ch.id = dtct.cau_hoi_id " +
                "WHERE dtct.de_thi_id = ? " +
                "ORDER BY dtct.question_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deThiId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cauHois.add(mapResultSetToCauHoi(rs));
                }
            }
        }
        return cauHois;
    }

    public List<CauHoi> findByType(String type) throws SQLException {
        List<CauHoi> cauHois = new ArrayList<>();
        String sql = "SELECT * FROM cau_hoi WHERE type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cauHois.add(mapResultSetToCauHoi(rs));
                }
            }
        }
        return cauHois;
    }

    public List<CauHoi> findNotInDeThi(int deThiId) throws SQLException {
        List<CauHoi> cauHois = new ArrayList<>();
        String sql = "SELECT * FROM cau_hoi WHERE id NOT IN " +
                "(SELECT cau_hoi_id FROM de_thi_chi_tiet WHERE de_thi_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deThiId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cauHois.add(mapResultSetToCauHoi(rs));
                }
            }
        }
        return cauHois;
    }

    // 3. Helper method
    private CauHoi mapResultSetToCauHoi(ResultSet rs) throws SQLException {
        CauHoi cauHoi = new CauHoi();
        cauHoi.setId(rs.getInt("id"));
        cauHoi.setContent(rs.getString("content"));
        cauHoi.setAmThanhId(rs.getObject("am_thanh_id") != null ? rs.getInt("am_thanh_id") : null);
        cauHoi.setType(rs.getString("type"));
        return cauHoi;
    }
}


