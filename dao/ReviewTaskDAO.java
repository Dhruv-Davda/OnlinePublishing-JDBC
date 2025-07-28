// src/dao/ReviewTaskDAO.java
package dao;

import util.DBUtil;

import java.sql.*;
import java.util.Scanner;

public class ReviewTaskDAO {

    // Create a review task when article is submitted (for simplicity we assign to first editor)
    public void assignTask(int articleID) {
        String sql = """
            INSERT INTO ReviewTask (ArticleID, EditorStaffID, Status, RequestedAt, Notes)
            VALUES (?, ?, 'pending', NOW(), 'Auto-assigned')
            """;

        int editorID = findAnyEditor(); // you can randomize this or rotate

        if (editorID == -1) {
            System.out.println("❌ No editor found.");
            return;
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, articleID);
            stmt.setInt(2, editorID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to assign review task: " + e.getMessage());
        }
    }

    private int findAnyEditor() {
        String sql = """
            SELECT u.UserID
            FROM User u
            JOIN UserRole ur ON u.UserID = ur.UserID
            JOIN Role r ON ur.RoleID = r.RoleID
            WHERE r.RoleName = 'editor' AND u.Status = 'Active'
            LIMIT 1
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (SQLException e) {
            System.out.println("Error finding editor: " + e.getMessage());
        }
        return -1;
    }

    public void showPendingTasks(int editorID) {
        String sql = """
            SELECT rt.TaskID, a.Title, a.ArticleID, rt.Notes, rt.RequestedAt
            FROM ReviewTask rt
            JOIN Article a ON rt.ArticleID = a.ArticleID
            WHERE rt.EditorStaffID = ? AND rt.Status = 'pending'
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, editorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.printf("Task ID: %d | Article ID: %d | Title: %s | Requested: %s\n",
                        rs.getInt("TaskID"),
                        rs.getInt("ArticleID"),
                        rs.getString("Title"),
                        rs.getTimestamp("RequestedAt"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
        }
    }

    public void completeTask(int taskID, boolean approve, String note) {
        String updateTask = """
            UPDATE ReviewTask
            SET Status = ?, CompletedAt = NOW(), Notes = ?
            WHERE TaskID = ?
            """;

        String updateArticle = """
            UPDATE Article
            SET Status = ?, PublishedAt = ?
            WHERE ArticleID = (SELECT ArticleID FROM ReviewTask WHERE TaskID = ?)
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(updateTask);
             PreparedStatement stmt2 = conn.prepareStatement(updateArticle)) {

            String newStatus = approve ? "approved" : "rejected";
            String articleStatus = approve ? "published" : "draft";

            stmt1.setString(1, newStatus);
            stmt1.setString(2, note);
            stmt1.setInt(3, taskID);
            stmt1.executeUpdate();

            stmt2.setString(1, articleStatus);
            stmt2.setTimestamp(2, approve ? new Timestamp(System.currentTimeMillis()) : null);
            stmt2.setInt(3, taskID);
            stmt2.executeUpdate();

            System.out.println(approve ? "✅ Article published!" : "❌ Article rejected & sent back to draft.");

        } catch (SQLException e) {
            System.out.println("Error completing review: " + e.getMessage());
        }
    }
}
