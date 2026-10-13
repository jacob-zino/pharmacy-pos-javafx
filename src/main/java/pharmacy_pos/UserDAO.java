package pharmacy_pos;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /** Returns the user if the password matches its stored BCrypt hash, otherwise null. */
    public User verifyLogin(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password");
                if (hash == null || !hash.startsWith("$2") || !BCrypt.checkpw(password, hash)) {
                    return null;
                }
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setRole(rs.getString("role"));
                return u; // the hash is not kept in memory
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Creates a user with a BCrypt-hashed password. */
    public void createUser(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }
}
