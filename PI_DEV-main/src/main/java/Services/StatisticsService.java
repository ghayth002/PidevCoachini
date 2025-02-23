package Services;

import Models.typeR;
import Utils.MyDb;
import java.sql.*;
import java.util.*;

public class StatisticsService {
    private Connection conn;

    public StatisticsService() {
        this.conn = MyDb.getInstance().getConn();
    }

    public Map<typeR, Integer> getReclamationsByType() throws SQLException {
        Map<typeR, Integer> stats = new EnumMap<>(typeR.class);
        String sql = "SELECT typeR, COUNT(*) as count FROM reclamation GROUP BY typeR";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                typeR type = typeR.valueOf(rs.getString("typeR"));
                int count = rs.getInt("count");
                stats.put(type, count);
            }
        }
        return stats;
    }

    public Map<String, Integer> getReclamationsByDay() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT DATE(date) as day, COUNT(*) as count " +
                    "FROM reclamation " +
                    "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(date) " +
                    "ORDER BY day DESC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String day = rs.getDate("day").toString();
                int count = rs.getInt("count");
                stats.put(day, count);
            }
        }
        return stats;
    }

    public int getTotalReclamations() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM reclamation";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int getPendingReclamations() throws SQLException {
        String sql = "SELECT COUNT(*) as pending FROM reclamation r " +
                    "LEFT JOIN reponse rep ON r.idReclamation = rep.id_reclamation " +
                    "WHERE rep.id IS NULL";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("pending");
            }
        }
        return 0;
    }

    public int getResolvedReclamations() throws SQLException {
        String sql = "SELECT COUNT(*) as resolved FROM reclamation r " +
                    "INNER JOIN reponse rep ON r.idReclamation = rep.id_reclamation " +
                    "WHERE rep.status = 'RESOLUE'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("resolved");
            }
        }
        return 0;
    }
} 