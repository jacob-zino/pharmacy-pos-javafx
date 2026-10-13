package pharmacy_pos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Medicine m = new Medicine();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setPrice(rs.getDouble("price"));
                m.setStockQty(rs.getInt("stock_qty"));

                Date d = rs.getDate("expiry_date");
                if (d != null) {
                    m.setExpiryDate(d.toLocalDate());
                }

                boolean requiresRx = rs.getBoolean("requires_prescription");
                m.setRequiresPrescription(requiresRx);

                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insertMedicine(Medicine m) {
        String sql = "INSERT INTO medicines(name, price, stock_qty, expiry_date, requires_prescription) "
                + "VALUES(?,?,?,?,?)";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getName());
            ps.setDouble(2, m.getPrice());
            ps.setInt(3, m.getStockQty());

            LocalDate date = m.getExpiryDate();
            if (date != null) {
                ps.setDate(4, java.sql.Date.valueOf(date));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setBoolean(5, m.isRequiresPrescription());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name=?, price=?, stock_qty=?, expiry_date=?, requires_prescription=? "
                + "WHERE id=?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getName());
            ps.setDouble(2, m.getPrice());
            ps.setInt(3, m.getStockQty());

            LocalDate date = m.getExpiryDate();
            if (date != null) {
                ps.setDate(4, java.sql.Date.valueOf(date));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setBoolean(5, m.isRequiresPrescription());
            ps.setInt(6, m.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMedicine(int id) {
        String sql = "DELETE FROM medicines WHERE id=?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStockAfterSale(int medicineId, int quantity) {
        String sql = "UPDATE medicines SET stock_qty = stock_qty - ? WHERE id=?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, medicineId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}