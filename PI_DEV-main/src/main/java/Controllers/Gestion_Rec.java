package Controllers;

import Models.Reclamation;
import Models.Reponse;
import Models.typeR;
import Services.ReclamationService;
import Services.ReponseService;
import Utils.EmailService;
import Utils.MyDb;
import Utils.ValidationUtils;
import Utils.BadWordFilter;
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
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> idColumn;
    @FXML private TableColumn<Reclamation, String> descriptionColumn;
    @FXML private TableColumn<Reclamation, typeR> typeColumn;
    @FXML private TableColumn<Reclamation, Integer> coachColumn;
    @FXML private TableColumn<Reclamation, Integer> adherentColumn;
    @FXML private TableColumn<Reclamation, Date> dateColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;
    @FXML private Button refreshTableButton;
    @FXML private TextField search;
    @FXML private FontIcon searchIcon;
    @FXML private Button returnButton;

    private ReclamationService reclamationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            reclamationService = new ReclamationService();
            
            // Setup table columns
            setupTableColumns();
            loadReclamations();

            // Add refresh handler
            refreshTableButton.setOnAction(e -> loadReclamations());

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
            refreshTableButton.setGraphic(refreshIcon);

            // Add return button handler
            returnButton.setOnAction(event -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
                    Stage stage = (Stage) returnButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Could not return to dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });
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
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    handleDelete(reclamation);
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
                            
                            // Change delete button to delete response
                            deleteButton.setText("Supprimer réponse");
                            deleteButton.setGraphic(createDeleteIcon());
                            deleteButton.getStyleClass().addAll("action-button", "delete");
                            deleteButton.setOnAction(event -> handleDeleteReponse(existingReponse));
                            
                            container.getChildren().addAll(actionButton, deleteButton);
                        } else {
                            actionButton.setText("Répondre");
                            actionButton.setGraphic(createReplyIcon());
                            actionButton.getStyleClass().addAll("action-button", "reply");
                            
                            // Change delete button to delete reclamation
                            deleteButton.setText("Supprimer");
                            deleteButton.setGraphic(createDeleteIcon());
                            deleteButton.getStyleClass().addAll("action-button", "delete");
                            deleteButton.setOnAction(event -> handleDelete(reclamation));
                            
                            container.getChildren().addAll(actionButton, deleteButton);
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
            reclamationTable.setItems(FXCollections.observableArrayList(reclamationService.getAll()));
            reclamationTable.refresh();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Reclamation reclamation) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reclamationService.delete(reclamation.getIdReclamation());
                        showAlert("Succès", "Réclamation supprimée avec succès", Alert.AlertType.INFORMATION);
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

            // Add email field
            TextField emailField = new TextField();
            emailField.setPromptText("Email de l'adhérent");

            // Add censor button
            Button censorButton = new Button("Censurer le texte");
            censorButton.setOnAction(e -> {
                String censoredText = BadWordFilter.censorBadWords(contenuField.getText());
                contenuField.setText(censoredText);
            });

            grid.add(new Label("Contenu:"), 0, 0);
            grid.add(contenuField, 1, 0);
            grid.add(censorButton, 2, 0);
            grid.add(new Label("Date:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);

            // Style the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            
            // Enable/Disable save button depending on whether content is empty
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

            // Validate both content and email
            contenuField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValidContent = newValue.length() >= 10; // Minimum 10 characters
                boolean containsBadWords = BadWordFilter.containsBadWords(newValue);
                
                if (containsBadWords) {
                    contenuField.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px;");
                    saveButton.setDisable(true);
                    showBadWordAlert();
                } else {
                    contenuField.setStyle(isValidContent ? "-fx-border-color: #28a745; -fx-border-width: 2px;" : "-fx-border-color: #ffa500; -fx-border-width: 2px;");
                    saveButton.setDisable(!isValidContent || emailField.getText().trim().isEmpty());
                }
            });
            
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValidEmail = newValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") || newValue.isEmpty();
                boolean isValidContent = !contenuField.getText().trim().isEmpty();
                saveButton.setDisable(!isValidEmail || !isValidContent);
            });

            // Convert the result to Reponse object when save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new Reponse(
                        0, // ID will be generated by database
                        reclamation.getIdReclamation(),
                        java.sql.Date.valueOf(datePicker.getValue()),
                        contenuField.getText(),
                        Reponse.STATUS_RESOLUE // Set default status to RESOLUE when creating a new response
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
                        // Send email notification
                        boolean emailSent = EmailService.sendResponseNotification(reclamation, reponse, emailField.getText().trim());
                        
                        if (emailSent) {
                            showAlert("Succès", "Réponse ajoutée avec succès et email envoyé", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Attention", "Réponse ajoutée mais l'envoi de l'email a échoué", Alert.AlertType.WARNING);
                        }
                        
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

            // Add email field
            TextField emailField = new TextField();
            emailField.setPromptText("Email de l'adhérent");

            // Add censor button
            Button censorButton = new Button("Censurer le texte");
            censorButton.setOnAction(e -> {
                String censoredText = BadWordFilter.censorBadWords(contenuField.getText());
                contenuField.setText(censoredText);
            });

            grid.add(new Label("Contenu:"), 0, 0);
            grid.add(contenuField, 1, 0);
            grid.add(censorButton, 2, 0);
            grid.add(new Label("Date:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);

            // Style the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            
            // Enable/Disable save button depending on whether content and email are empty
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(contenuField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty());

            contenuField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValidContent = newValue.length() >= 10; // Minimum 10 characters
                boolean containsBadWords = BadWordFilter.containsBadWords(newValue);
                
                if (containsBadWords) {
                    contenuField.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px;");
                    saveButton.setDisable(true);
                    showBadWordAlert();
                } else {
                    contenuField.setStyle(isValidContent ? "-fx-border-color: #28a745; -fx-border-width: 2px;" : "-fx-border-color: #ffa500; -fx-border-width: 2px;");
                    saveButton.setDisable(!isValidContent || emailField.getText().trim().isEmpty());
                }
            });
            
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValidEmail = newValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") || newValue.isEmpty();
                boolean isValidContent = !contenuField.getText().trim().isEmpty();
                saveButton.setDisable(!isValidEmail || !isValidContent);
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
                        // Update the status to RESOLUE when modifying a response
                        reponse.setStatus(Reponse.STATUS_RESOLUE);
                        
                        // Send email notification for the update
                        boolean emailSent = EmailService.sendResponseNotification(reclamation, reponse, emailField.getText().trim());
                        
                        if (emailSent) {
                            showAlert("Succès", "Réponse modifiée avec succès et email envoyé", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Attention", "Réponse modifiée mais l'envoi de l'email a échoué", Alert.AlertType.WARNING);
                        }
                        
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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

    /**
     * Affiche une alerte pour informer l'utilisateur que le texte contient des mots inappropriés
     */
    private void showBadWordAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Contenu inapproprié");
        alert.setHeaderText("Mots inappropriés détectés");
        alert.setContentText("Votre texte contient des mots inappropriés. Veuillez reformuler votre message de manière professionnelle.");
        alert.showAndWait();
    }
}
