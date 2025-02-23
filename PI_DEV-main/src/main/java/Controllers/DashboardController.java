package Controllers;

import Models.typeR;
import Services.StatisticsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML private PieChart typeChart;
    @FXML private LineChart<String, Number> dailyChart;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label pendingReclamationsLabel;
    @FXML private Label resolvedReclamationsLabel;
    @FXML private Button refreshButton;
    @FXML private Button reclamationButton;

    private StatisticsService statisticsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statisticsService = new StatisticsService();
        
        // Setup refresh button
        FontIcon refreshIcon = new FontIcon("fas-sync-alt");
        refreshIcon.setIconColor(Color.WHITE);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> loadStatistics());

        // Setup navigation
        reclamationButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Gestion_Rec.fxml"));
                Stage stage = (Stage) reclamationButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception ex) {
                showAlert("Erreur", "Erreur de navigation: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        // Load initial statistics
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Load summary statistics
            updateSummaryStats();
            
            // Load type distribution chart
            updateTypeChart();
            
            // Load daily trend chart
            updateDailyChart();
            
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateSummaryStats() throws SQLException {
        int total = statisticsService.getTotalReclamations();
        int pending = statisticsService.getPendingReclamations();
        int resolved = statisticsService.getResolvedReclamations();

        totalReclamationsLabel.setText(String.valueOf(total));
        pendingReclamationsLabel.setText(String.valueOf(pending));
        resolvedReclamationsLabel.setText(String.valueOf(resolved));
    }

    private void updateTypeChart() throws SQLException {
        typeChart.getData().clear();
        Map<typeR, Integer> typeStats = statisticsService.getReclamationsByType();
        
        for (Map.Entry<typeR, Integer> entry : typeStats.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey().toString(), entry.getValue());
            typeChart.getData().add(slice);
        }
    }

    private void updateDailyChart() throws SQLException {
        dailyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réclamations par jour");
        
        Map<String, Integer> dailyStats = statisticsService.getReclamationsByDay();
        for (Map.Entry<String, Integer> entry : dailyStats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        dailyChart.getData().add(series);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 