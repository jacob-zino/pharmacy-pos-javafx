package pharmacy_pos;

import java.sql.*;
import java.time.LocalDate;

public class SaleDAO {

    public boolean saveSale(Sale sale) {
        String insertSaleSql =
                "INSERT INTO sales (sale_datetime, total_amount, user_id) VALUES (?, ?, ?)";
        String insertItemSql =
                "INSERT INTO sale_items (sale_id, medicine_id, quantity, unit_price, subtotal) " +
                        "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); 

            int saleId;
            try (PreparedStatement psSale = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {

                psSale.setTimestamp(1, Timestamp.valueOf(sale.getSaleDateTime()));
                psSale.setDouble(2, sale.getTotalAmount());
                psSale.setInt(3, sale.getUserId());

                int affected = psSale.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = psSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {

                for (SaleItem item : sale.getItems()) {
                    psItem.setInt(1, saleId);
                    psItem.setInt(2, item.getMedicine().getId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.setDouble(5, item.getLineTotal());
                    psItem.addBatch();
                }

                psItem.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public double getTodaySalesTotal() {
        String sql = "SELECT IFNULL(SUM(total_amount), 0) FROM sales WHERE DATE(sale_datetime) = CURDATE()";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return 0.0;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTodaySalesCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_datetime) = CURDATE()";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return 0;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}