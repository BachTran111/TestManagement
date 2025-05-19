package app.NganHangDe.DAO;

import app.NganHangDe.Model.DapAn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DapAnDAO {
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Create a new answer
     */
    public void create(DapAn dapAn) throws SQLException {
        String sql = "INSERT INTO dap_an (cau_hoi_id, content, is_correct) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, dapAn.getCauHoiId());
            stmt.setString(2, dapAn.getContent());
            stmt.setBoolean(3, dapAn.getCorrect());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    dapAn.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Find answer by ID
     */
    public DapAn findById(int id) throws SQLException {
        String sql = "SELECT * FROM dap_an WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find all answers
     */
    public List<DapAn> findAll() throws SQLException {
        List<DapAn> list = new ArrayList<>();
        String sql = "SELECT * FROM dap_an";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Update an existing answer
     */
    public void update(DapAn dapAn) throws SQLException {
        String sql = "UPDATE dap_an SET content = ?, is_correct = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dapAn.getContent());
            stmt.setBoolean(2, dapAn.getCorrect());
            stmt.setInt(3, dapAn.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Delete answer by ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dap_an WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Find answers by question ID
     */
    public List<DapAn> findByCauHoiId(int cauHoiId) throws SQLException {
        List<DapAn> list = new ArrayList<>();
        String sql = "SELECT * FROM dap_an WHERE cau_hoi_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cauHoiId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Delete all answers for a given question
     */
    public void deleteByCauHoiId(int cauHoiId) throws SQLException {
        String sql = "DELETE FROM dap_an WHERE cau_hoi_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cauHoiId);
            stmt.executeUpdate();
        }
    }

    /**
     * Helper to map ResultSet row to DapAn model
     */
    private DapAn mapRow(ResultSet rs) throws SQLException {
        DapAn da = new DapAn();
        da.setId(rs.getInt("id"));
        da.setCauHoiId(rs.getInt("cau_hoi_id"));
        da.setContent(rs.getString("content"));
        da.setCorrect(rs.getBoolean("is_correct"));
        return da;
    }
}
