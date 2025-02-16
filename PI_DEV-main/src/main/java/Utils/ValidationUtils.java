package Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    // Validation pour les champs texte génériques
    public static boolean validateRequiredField(TextField field, String fieldName) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            showError(fieldName + " est requis.");
            field.setStyle("-fx-border-color: red;");
            return false;
        }
        field.setStyle("");
        return true;
    }
    
    // Validation pour les zones de texte
    public static boolean validateTextArea(TextArea textArea, String fieldName, int minLength) {
        if (textArea.getText() == null || textArea.getText().trim().length() < minLength) {
            showError(fieldName + " doit contenir au moins " + minLength + " caractères.");
            textArea.setStyle("-fx-border-color: red;");
            return false;
        }
        textArea.setStyle("");
        return true;
    }
    
    // Validation pour les IDs numériques
    public static boolean validateNumericId(TextField field, String fieldName) {
        if (!Pattern.matches("\\d+", field.getText())) {
            showError(fieldName + " doit être un nombre valide.");
            field.setStyle("-fx-border-color: red;");
            return false;
        }
        field.setStyle("");
        return true;
    }
    
    // Validation pour les emails
    public static boolean validateEmail(TextField field) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, field.getText())) {
            showError("Format d'email invalide.");
            field.setStyle("-fx-border-color: red;");
            return false;
        }
        field.setStyle("");
        return true;
    }
    
    // Validation pour les numéros de téléphone
    public static boolean validatePhone(TextField field) {
        String phoneRegex = "^[0-9]{8}$";
        if (!Pattern.matches(phoneRegex, field.getText())) {
            showError("Le numéro de téléphone doit contenir 8 chiffres.");
            field.setStyle("-fx-border-color: red;");
            return false;
        }
        field.setStyle("");
        return true;
    }
    
    // Validation pour les mots de passe
    public static boolean validatePassword(TextField field) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        if (!Pattern.matches(passwordRegex, field.getText())) {
            showError("Le mot de passe doit contenir au moins 8 caractères, incluant majuscules, minuscules, chiffres et caractères spéciaux.");
            field.setStyle("-fx-border-color: red;");
            return false;
        }
        field.setStyle("");
        return true;
    }
    
    // Affichage des messages d'erreur
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Affichage des messages de succès
    public static void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 