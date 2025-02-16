package Controllers;

import Models.Reclamation;
import Models.typeR;
import Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class UserReclamationController implements Initializable {
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<typeR> typeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private Button submitButton;
    @FXML private Button clearButton;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private ListView<Reclamation> reclamationTable;
    @FXML private Button profileButton;

    private final ReclamationService reclamationService = new ReclamationService();
    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();
    private FilteredList<Reclamation> filteredReclamations;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize ComboBox
        typeComboBox.getItems().addAll(typeR.values());
        
        // Setup custom cell factory for ListView
        reclamationTable.setCellFactory(lv -> new ListCell<Reclamation>() {
            @Override
            protected void updateItem(Reclamation reclamation, boolean empty) {
                super.updateItem(reclamation, empty);
                if (empty || reclamation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create the main container
                    VBox container = new VBox();
                    container.getStyleClass().add("reclamation-item");

                    // Header with type and date
                    HBox header = new HBox();
                    header.getStyleClass().add("reclamation-header");
                    header.setSpacing(15);

                    Label typeLabel = new Label(reclamation.getType().toString());
                    typeLabel.getStyleClass().add("reclamation-type");

                    Label dateLabel = new Label(dateFormat.format(reclamation.getDate()));
                    dateLabel.getStyleClass().add("reclamation-date");

                    header.getChildren().addAll(typeLabel, dateLabel);

                    // Description
                    Label descriptionLabel = new Label(reclamation.getDescription());
                    descriptionLabel.getStyleClass().add("reclamation-description");
                    descriptionLabel.setWrapText(true);

                    // Footer with status and actions
                    HBox footer = new HBox();
                    footer.getStyleClass().add("reclamation-footer");
                    footer.setSpacing(15);

                    // Status badge
                    Label statusLabel = new Label("En attente");
                    statusLabel.getStyleClass().addAll("status-badge", "status-pending");

                    // Delete button
                    Button deleteButton = new Button("🗑️");
                    deleteButton.getStyleClass().add("delete-button");
                    deleteButton.setOnAction(event -> handleDeleteReclamation(reclamation));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    footer.getChildren().addAll(statusLabel, spacer, deleteButton);

                    // Add all elements to the container
                    container.getChildren().addAll(header, descriptionLabel, footer);
                    setGraphic(container);
                }
            }
        });

        // Load reclamations
        loadReclamations();

        // Setup search functionality
        setupSearch();
        
        // Setup buttons
        setupButtons();
    }

    private void setupButtons() {
        refreshButton.setOnAction(event -> loadReclamations());
        submitButton.setOnAction(event -> handleSubmitReclamation());
        clearButton.setOnAction(event -> clearForm());
        profileButton.setOnAction(event -> handleProfile());
    }

    private void handleProfile() {
        // Implement profile action here
        System.out.println("Profile clicked");
    }

    private void loadReclamations() {
        reclamationsList.clear();
        try {
            reclamationsList.addAll(reclamationService.getAll());
            reclamationTable.setItems(reclamationsList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réclamations: " + e.getMessage());
        }
    }

    private void setupSearch() {
        filteredReclamations = new FilteredList<>(reclamationsList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredReclamations.setPredicate(reclamation -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return reclamation.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                       reclamation.getType().toString().toLowerCase().contains(lowerCaseFilter);
            });
            reclamationTable.setItems(filteredReclamations);
        });
    }

    private void handleSubmitReclamation() {
        if (validateForm()) {
            Reclamation newReclamation = new Reclamation(
                0, // ID will be set by the database
                descriptionField.getText(),
                typeComboBox.getValue(),
                0, // Current user's ID will be set here
                0, // Coach ID will be set based on context
                java.sql.Date.valueOf(datePicker.getValue())
            );

            try {
                if (reclamationService.create(newReclamation)) {
                    clearForm();
                    loadReclamations();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation soumise avec succès!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "La réclamation n'a pas pu être ajoutée.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la réclamation: " + e.getMessage());
            }
        }
    }

    private void handleDeleteReclamation(Reclamation reclamation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reclamationService.delete(reclamation.getIdReclamation());
                    loadReclamations();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation supprimée avec succès!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la réclamation: " + e.getMessage());
                }
            }
        });
    }

    private boolean validateForm() {
        String errorMessage = "";
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            errorMessage += "La description est requise.\n";
        }
        if (typeComboBox.getValue() == null) {
            errorMessage += "Le type de réclamation est requis.\n";
        }
        if (datePicker.getValue() == null) {
            errorMessage += "La date est requise.\n";
        }

        if (!errorMessage.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage);
            return false;
        }
        return true;
    }

    private void clearForm() {
        descriptionField.clear();
        typeComboBox.setValue(null);
        datePicker.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 