// src/dao/PayPerViewDAO.java
package dao;

import util.DBUtil;

import java.sql.*;

public class PayPerViewDAO {

    public boolean buyArticle(int userID, int articleID) {
        String sql = """
            INSERT INTO PayPerView (UserID, ArticleID, PurchaseDate)
            VALUES (?, ?, NOW())
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, articleID);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Pay-per-view failed: " + e.getMessage());
            return false;
        }
    }

    public boolean hasAccess(int userID, int articleID) {
        String sql = """
            SELECT * FROM PayPerView
            WHERE UserID = ? AND ArticleID = ?
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, articleID);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Access check failed: " + e.getMessage());
            return false;
        }
    }
}
