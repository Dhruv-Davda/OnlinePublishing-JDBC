// src/dao/SubscriptionPlanDAO.java
package dao;

import util.DBUtil;
import java.sql.*;
import java.util.Scanner;

public class SubscriptionPlanDAO {

    public void showAllPlans() {
        String sql = "SELECT * FROM SubscriptionPlan";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Available Plans:");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Price: %.2f | Billing: %s\n",
                        rs.getInt("PlanID"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getString("BillingCycle"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching plans: " + e.getMessage());
        }
    }

    public boolean subscribe(int userID, int planID, boolean autoRenew) {
        String sql = """
            INSERT INTO Subscription (UserID, PlanID, StartDate, EndDate, AutoRenew, Status)
            VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), ?, 'active')
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            stmt.setInt(2, planID);
            stmt.setBoolean(3, autoRenew);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Subscription failed: " + e.getMessage());
            return false;
        }
    }

    public boolean hasActiveSubscription(int userID) {
        String sql = """
            SELECT * FROM Subscription
            WHERE UserID = ? AND Status = 'active' AND EndDate >= CURDATE()
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking subscription: " + e.getMessage());
            return false;
        }
    }
}
