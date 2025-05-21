package app.NganHangDe.DAO;

import app.NganHangDe.Model.AmThanh;

import java.sql.*;

public class AmThanhDAO {
    public Integer create(AmThanh amThanh) throws SQLException {
        String sql = "INSERT INTO am_thanh (file_path, description) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, amThanh.getFilePath());
            stmt.setString(2, amThanh.getDescription());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM am_thanh WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
