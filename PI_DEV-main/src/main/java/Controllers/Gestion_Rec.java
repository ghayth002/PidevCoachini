package Controllers;

import Models.Reclamation;
import Models.Reponse;
import Models.typeR;
import Services.ReclamationService;
import Services.ReponseService;
import Utils.MyDb;
import Utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.util.Optional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Gestion_Rec implements Initializable {
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<typeR> typeComboBox;
    @FXML private TextField coachIdField;
    @FXML private TextField adherentIdField;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> idColumn;
    @FXML private TableColumn<Reclamation, String> descriptionColumn;
    @FXML private TableColumn<Reclamation, typeR> typeColumn;
    @FXML private TableColumn<Reclamation, Integer> coachColumn;
    @FXML private TableColumn<Reclamation, Integer> adherentColumn;
    @FXML private TableColumn<Reclamation, Date> dateColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;
    @FXML private Button reponseNav;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Label descriptionLabel;
    @FXML private Label typeLabel;
    @FXML private Label coachIdLabel;
    @FXML private Label adherentIdLabel;
    @FXML private FontIcon searchIcon;
    @FXML private TextField search;

    private ReclamationService reclamationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            reclamationService = new ReclamationService();
            typeComboBox.setItems(FXCollections.observableArrayList(typeR.values()));
            
            // Create Ikonli icons
            FontIcon descriptionIcon = new FontIcon(FontAwesomeSolid.ALIGN_LEFT);
            FontIcon typeIcon = new FontIcon(FontAwesomeSolid.TAG);
            FontIcon idIcon = new FontIcon(FontAwesomeSolid.USER);
            
            // Style the icons
            descriptionIcon.setIconSize(16);
            typeIcon.setIconSize(16);
            idIcon.setIconSize(16);
            
            String iconColor = "#666666";
            descriptionIcon.setIconColor(Color.web(iconColor));
            typeIcon.setIconColor(Color.web(iconColor));
            idIcon.setIconColor(Color.web(iconColor));
            
            // Set the icons to labels
            descriptionLabel.setGraphic(descriptionIcon);
            typeLabel.setGraphic(typeIcon);
            coachIdLabel.setGraphic(idIcon);
            adherentIdLabel.setGraphic(idIcon);
            
            // Setup table columns
            setupTableColumns();
            
            loadReclamations();
            setupButtons();
            


            // Add refresh handler
            refreshButton.setOnAction(e -> loadReclamations());
            
            // Add clear form handler
            clearButton.setOnAction(e -> clearForm());
            
            // Add selection handler for table
            reclamationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                }
            });

            // Setup search functionality
            search.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    searchIcon.setIconLiteral("fas-times");
                    searchIcon.setOnMouseClicked(e -> {
                        search.clear();
                        loadReclamations();
                    });
                } else {
                    searchIcon.setIconLiteral("fas-search");
                    searchIcon.setOnMouseClicked(null);
                }
                // Implement your search logic here
            });
            
            // Style the refresh button icon
            FontIcon refreshIcon = new FontIcon("fas-sync-alt");
            refreshIcon.setIconColor(Color.WHITE);
            refreshButton.setGraphic(refreshIcon);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idReclamation"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        coachColumn.setCellValueFactory(new PropertyValueFactory<>("id_coach"));
        adherentColumn.setCellValueFactory(new PropertyValueFactory<>("id_adherent"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Setup the actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button actionButton = new Button();
            private final Button deleteButton = new Button();
            private Reponse existingReponse = null;
            
            {
                actionButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().addAll("action-button", "delete-button");
                
                // Set button sizes
                actionButton.setPrefSize(110, 32);
                deleteButton.setPrefSize(110, 32);
                
                // Add icons to buttons
                actionButton.setGraphic(createReplyIcon());
                deleteButton.setGraphic(createDeleteIcon());
                
                // Set graphic text gap
                actionButton.setGraphicTextGap(8);
                deleteButton.setGraphicTextGap(8);
                
                actionButton.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    if (existingReponse != null) {
                        handleModifierReponse(reclamation, existingReponse);
                    } else {
                        handleRepondre(reclamation);
                    }
                });

                deleteButton.setOnAction(event -> {
                    if (existingReponse != null) {
                        handleDeleteReponse(existingReponse);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    try {
                        ReponseService reponseService = new ReponseService();
                        // Clear any existing response
                        existingReponse = null;
                        // Get fresh response data
                        existingReponse = reponseService.getByReclamationId(reclamation.getIdReclamation());
                        
                        HBox container = new HBox(10);
                        container.setAlignment(javafx.geometry.Pos.CENTER);
                        container.setPadding(new Insets(5, 10, 5, 10));
                        
                        // Reset button styles
                        actionButton.getStyleClass().clear();
                        deleteButton.getStyleClass().clear();
                        
                        if (existingReponse != null) {
                            actionButton.setText("Modifier");
                            actionButton.setGraphic(createEditIcon());
                            actionButton.getStyleClass().addAll("action-button", "modify");
                            deleteButton.getStyleClass().addAll("action-button", "delete");
                            container.getChildren().addAll(actionButton, deleteButton);
                        } else {
                            actionButton.setText("Répondre");
                            actionButton.setGraphic(createReplyIcon());
                            actionButton.getStyleClass().addAll("action-button", "reply");
                            container.getChildren().add(actionButton);
                        }
                        
                        setGraphic(container);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadReclamations() {
        try {
            // Clear the current items
            reclamationTable.getItems().clear();
            
            // Load new items
            reclamationTable.setItems(FXCollections.observableArrayList(reclamationService.getAll()));
            
            // Force refresh of table
            reclamationTable.refresh();
            
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupButtons() {
        if (addButton != null) addButton.setOnAction(e -> handleAdd());
        if (updateButton != null) updateButton.setOnAction(e -> handleUpdate());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete());
        if (clearButton != null) clearButton.setOnAction(e -> clearForm());
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validation de la description (minimum 10 caractères)
        if (!ValidationUtils.validateTextArea(descriptionField, "Description", 10)) {
            isValid = false;
        }
        
        // Validation du type
        if (typeComboBox.getValue() == null) {
            ValidationUtils.showError("Le type de réclamation est requis.");
            typeComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            typeComboBox.setStyle("");
        }
        
        // Validation de l'ID du coach
        if (!ValidationUtils.validateNumericId(coachIdField, "ID Coach")) {
            isValid = false;
        }
        
        // Validation de l'ID de l'adhérent
        if (!ValidationUtils.validateNumericId(adherentIdField, "ID Adhérent")) {
            isValid = false;
        }

        return isValid;
    }
    
    // Méthode pour réinitialiser les styles des champs
    private void resetFieldStyles() {
        descriptionField.setStyle("");
        typeComboBox.setStyle("");
        coachIdField.setStyle("");
        adherentIdField.setStyle("");
    }
    
    // Méthode pour vérifier l'existence du coach
    private boolean validateCoachExists(int coachId) {
        try {
            String sql = "SELECT COUNT(*) FROM coach WHERE id = ?";
            try (PreparedStatement stmt = MyDb.getInstance().getConn().prepareStatement(sql)) {
                stmt.setInt(1, coachId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
            ValidationUtils.showError("Le coach avec l'ID " + coachId + " n'existe pas.");
            return false;
        } catch (SQLException e) {
            ValidationUtils.showError("Erreur lors de la vérification du coach: " + e.getMessage());
            return false;
        }
    }
    
    // Méthode pour vérifier l'existence de l'adhérent
    private boolean validateAdherentExists(int adherentId) {
        try {
            String sql = "SELECT COUNT(*) FROM adherent WHERE id = ?";
            try (PreparedStatement stmt = MyDb.getInstance().getConn().prepareStatement(sql)) {
                stmt.setInt(1, adherentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
            ValidationUtils.showError("L'adhérent avec l'ID " + adherentId + " n'existe pas.");
            return false;
        } catch (SQLException e) {
            ValidationUtils.showError("Erreur lors de la vérification de l'adhérent: " + e.getMessage());
            return false;
        }
    }
    
    @FXML
    private void handleAdd() {
        resetFieldStyles();
        if (validateForm()) {
            try {
                int coachId = Integer.parseInt(coachIdField.getText());
                int adherentId = Integer.parseInt(adherentIdField.getText());
                
                // Vérifier l'existence du coach et de l'adhérent
                if (!validateCoachExists(coachId) || !validateAdherentExists(adherentId)) {
                    return;
                }
                
                // Créer et sauvegarder la réclamation avec la date système
                Reclamation reclamation = new Reclamation(
                    0,
                    descriptionField.getText().trim(),
                    typeComboBox.getValue(),
                    coachId,
                    adherentId,
                    new java.sql.Date(System.currentTimeMillis()) // Use current system date
                );
                
                if (reclamationService.create(reclamation)) {
                    ValidationUtils.showSuccess("Réclamation ajoutée avec succès!");
                    clearForm();
                    loadReclamations();
                }
            } catch (Exception e) {
                ValidationUtils.showError("Erreur lors de l'ajout de la réclamation: " + e.getMessage());
            }
        }
    }

    private void handleUpdate() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
                return;
            }

            if (!validateForm()) {
                return;
            }

            // Update selected reclamation
            selected.setDescription(descriptionField.getText());
            selected.setType(typeComboBox.getValue());
            selected.setId_coach(Integer.parseInt(coachIdField.getText()));
            selected.setId_adherent(Integer.parseInt(adherentIdField.getText()));
            // Keep the original date when updating
            // selected.setDate(selected.getDate());

            reclamationService.update(selected);
            showAlert("Succès", "Réclamation mise à jour avec succès", Alert.AlertType.INFORMATION);
            clearForm();
            loadReclamations();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reclamationService.delete(selected.getIdReclamation());
                        showAlert("Succès", "Réclamation supprimée avec succès", Alert.AlertType.INFORMATION);
                        clearForm();
                        loadReclamations();
                    } catch (Exception e) {
                        showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (descriptionField.getText().isEmpty()) {
            errors.append("Description est requise\n");
        }
        if (typeComboBox.getValue() == null) {
            errors.append("Type est requis\n");
        }
        if (coachIdField.getText().isEmpty() || !coachIdField.getText().matches("\\d+")) {
            errors.append("ID Coach doit être un nombre valide\n");
        }
        if (adherentIdField.getText().isEmpty() || !adherentIdField.getText().matches("\\d+")) {
            errors.append("ID Adherent doit être un nombre valide\n");
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void populateForm(Reclamation reclamation) {
        descriptionField.setText(reclamation.getDescription());
        typeComboBox.setValue(reclamation.getType());
        coachIdField.setText(String.valueOf(reclamation.getId_coach()));
        adherentIdField.setText(String.valueOf(reclamation.getId_adherent()));
    }

    private void clearForm() {
        descriptionField.clear();
        typeComboBox.setValue(null);
        coachIdField.clear();
        adherentIdField.clear();
        reclamationTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleRepondre(Reclamation reclamation) {
        try {
            // Create the custom dialog
            Dialog<Reponse> dialog = new Dialog<>();
            dialog.setTitle("Répondre à la réclamation #" + reclamation.getIdReclamation());
            dialog.setHeaderText("Ajouter une réponse");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextArea contenuField = new TextArea();
            contenuField.setPromptText("Votre réponse...");
            contenuField.setPrefRowCount(3);
            contenuField.setWrapText(true);

            DatePicker datePicker = new DatePicker(LocalDate.now());

            grid.add(new Label("Contenu:"), 0, 0);
            grid.add(contenuField, 1, 0);
            grid.add(new Label("Date:"), 0, 1);
            grid.add(datePicker, 1, 1);

            // Style the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            
            // Enable/Disable save button depending on whether content is empty
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

            contenuField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty());
            });

            // Convert the result to Reponse object when save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new Reponse(
                        0, // ID will be generated by database
                        reclamation.getIdReclamation(),
                        java.sql.Date.valueOf(datePicker.getValue()),
                        contenuField.getText()
                    );
                }
                return null;
            });

            // Show the dialog and handle the result
            Optional<Reponse> result = dialog.showAndWait();
            result.ifPresent(reponse -> {
                try {
                    ReponseService reponseService = new ReponseService();
                    if (reponseService.create(reponse)) {
                        showAlert("Succès", "Réponse ajoutée avec succès", Alert.AlertType.INFORMATION);
                        
                        // Force refresh of the table and its cells
                        loadReclamations();
                        reclamationTable.refresh();
                        
                        // Force the specific row to update
                        int index = reclamationTable.getItems().indexOf(reclamation);
                        if (index >= 0) {
                            reclamationTable.getItems().set(index, reclamation);
                        }
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de l'ajout de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du dialogue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleModifierReponse(Reclamation reclamation, Reponse existingReponse) {
        try {
            // Create the custom dialog
            Dialog<Reponse> dialog = new Dialog<>();
            dialog.setTitle("Modifier la réponse à la réclamation #" + reclamation.getIdReclamation());
            dialog.setHeaderText("Modifier la réponse");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextArea contenuField = new TextArea();
            contenuField.setPromptText("Votre réponse...");
            contenuField.setPrefRowCount(3);
            contenuField.setWrapText(true);
            contenuField.setText(existingReponse.getContenu());

            DatePicker datePicker = new DatePicker(
                ((java.sql.Date) existingReponse.getDate_reponse()).toLocalDate()
            );

            grid.add(new Label("Contenu:"), 0, 0);
            grid.add(contenuField, 1, 0);
            grid.add(new Label("Date:"), 0, 1);
            grid.add(datePicker, 1, 1);

            // Style the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            
            // Enable/Disable save button depending on whether content is empty
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(contenuField.getText().trim().isEmpty());

            contenuField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty());
            });

            // Convert the result to Reponse object when save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    existingReponse.setContenu(contenuField.getText());
                    existingReponse.setDate_reponse(java.sql.Date.valueOf(datePicker.getValue()));
                    return existingReponse;
                }
                return null;
            });

            // Show the dialog and handle the result
            Optional<Reponse> result = dialog.showAndWait();
            result.ifPresent(reponse -> {
                try {
                    ReponseService reponseService = new ReponseService();
                    if (reponseService.update(reponse)) {
                        showAlert("Succès", "Réponse modifiée avec succès", Alert.AlertType.INFORMATION);
                        loadReclamations(); // Refresh the table to update the button state
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la modification de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du dialogue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteReponse(Reponse reponse) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la réponse");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ReponseService reponseService = new ReponseService();
                reponseService.delete(reponse.getId());
                showAlert("Succès", "Réponse supprimée avec succès", Alert.AlertType.INFORMATION);
                
                // Refresh the entire table to update the UI
                loadReclamations();
                
                // Force the table to refresh its cells
                reclamationTable.refresh();
                
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private Node createEditIcon() {
        // Edit icon SVG path
        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        editIcon.setFill(Color.WHITE);
        editIcon.setScaleX(0.7);
        editIcon.setScaleY(0.7);
        return editIcon;
    }

    private Node createDeleteIcon() {
        // Delete icon SVG path
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        deleteIcon.setFill(Color.WHITE);
        deleteIcon.setScaleX(0.7);
        deleteIcon.setScaleY(0.7);
        return deleteIcon;
    }

    private Node createReplyIcon() {
        // Reply icon SVG path
        SVGPath replyIcon = new SVGPath();
        replyIcon.setContent("M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z");
        replyIcon.setFill(Color.WHITE);
        replyIcon.setScaleX(0.7);
        replyIcon.setScaleY(0.7);
        return replyIcon;
    }
}
