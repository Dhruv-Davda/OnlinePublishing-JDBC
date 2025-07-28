// src/dao/ArticleViewDAO.java
package dao;

import util.DBUtil;

import java.sql.*;

public class ArticleViewDAO {

    public void recordView(int userID, int articleID) {
        String checkSql = """
            SELECT * FROM ArticleView WHERE UserID = ? AND ArticleID = ?
        """;

        String insertSql = """
            INSERT INTO ArticleView (UserID, ArticleID, ViewCount)
            VALUES (?, ?, 1)
        """;

        String updateSql = """
            UPDATE ArticleView SET ViewCount = ViewCount + 1
            WHERE UserID = ? AND ArticleID = ?
        """;

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userID);
            checkStmt.setInt(2, articleID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, userID);
                updateStmt.setInt(2, articleID);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userID);
                insertStmt.setInt(2, articleID);
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Error recording article view: " + e.getMessage());
        }
    }

    public void showUserViews(int userID) {
        String sql = """
            SELECT a.Title, v.ViewCount
            FROM ArticleView v
            JOIN Article a ON v.ArticleID = a.ArticleID
            WHERE v.UserID = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            System.out.println("📊 Your Article Views:");
            while (rs.next()) {
                System.out.printf("📖 %s — %d views\n", rs.getString("Title"), rs.getInt("ViewCount"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching views: " + e.getMessage());
        }
    }
}
