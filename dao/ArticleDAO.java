// src/dao/ArticleDAO.java
package dao;

import model.Article;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {

    public boolean createArticle(Article article) {
        String sql = """
                INSERT INTO Article (AuthorID, Title, Body, Excerpt, Price, CreatedAt, Status, PrimaryTopicID)
                VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, article.getAuthorID());
            stmt.setString(2, article.getTitle());
            stmt.setString(3, article.getBody());
            stmt.setString(4, article.getExcerpt());
            stmt.setDouble(5, article.getPrice());
            stmt.setString(6, article.getStatus());
            stmt.setInt(7, article.getPrimaryTopicID());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error creating article: " + e.getMessage());
            return false;
        }
    }

    public List<Article> getDraftsByAuthor(int authorID) {
        String sql = "SELECT * FROM Article WHERE AuthorID = ? AND Status = 'draft'";
        List<Article> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Article a = new Article(
                    rs.getInt("AuthorID"),
                    rs.getString("Title"),
                    rs.getString("Body"),
                    rs.getString("Excerpt"),
                    rs.getDouble("Price"),
                    rs.getInt("PrimaryTopicID")
                );
                a.setArticleID(rs.getInt("ArticleID"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching drafts: " + e.getMessage());
        }

        return list;
    }

    public boolean submitForReview(int articleID, int authorID) {
        String sql = "UPDATE Article SET Status = 'review', UpdatedAt = NOW() WHERE ArticleID = ? AND AuthorID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, articleID);
            stmt.setInt(2, authorID);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error submitting for review: " + e.getMessage());
            return false;
        }
    }
}
