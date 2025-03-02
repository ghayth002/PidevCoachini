package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * Classe utilitaire pour la détection et le filtrage des mots inappropriés
 */
public class BadWordFilter {
    
    // Liste locale de mots inappropriés (en cas d'échec de l'API)
    private static final Set<String> DEFAULT_BAD_WORDS = new HashSet<>(Arrays.asList(
        "merde", "putain", "connard", "salope", "pute", "enculé", "bite", "couille", 
        "fuck", "shit", "bitch", "asshole", "bastard", "damn", "cunt", "dick", "pussy"
    ));
    
    // API URL pour la détection des mots inappropriés
    private static final String API_URL = "https://api.apilayer.com/bad_words?censor_character=*";
    private static final String API_KEY = "8hXTHLeRIMBtt33XJLrtchAIZV3WXlMu"; // Remplacez par votre clé API
    
    /**
     * Vérifie si le texte contient des mots inappropriés en utilisant l'API
     * @param text Le texte à vérifier
     * @return true si le texte contient des mots inappropriés, false sinon
     */
    public static boolean containsBadWords(String text) {
        try {
            // Essayer d'abord avec l'API
            return checkWithAPI(text);
        } catch (Exception e) {
            // En cas d'échec, utiliser la liste locale
            System.err.println("Erreur lors de l'appel à l'API de détection de mots inappropriés: " + e.getMessage());
            return checkWithLocalList(text);
        }
    }
    
    /**
     * Vérifie si le texte contient des mots inappropriés en utilisant l'API
     * @param text Le texte à vérifier
     * @return true si le texte contient des mots inappropriés, false sinon
     * @throws IOException En cas d'erreur lors de l'appel à l'API
     */
    private static boolean checkWithAPI(String text) throws IOException {
        // Encoder le texte pour l'URL
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        
        // Créer la connexion HTTP
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("apikey", API_KEY);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        
        // Envoyer le texte à l'API
        try (var os = connection.getOutputStream()) {
            byte[] input = encodedText.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Lire la réponse
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // Analyser la réponse JSON
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getBoolean("bad_words_found");
    }
    
    /**
     * Vérifie si le texte contient des mots inappropriés en utilisant la liste locale
     * @param text Le texte à vérifier
     * @return true si le texte contient des mots inappropriés, false sinon
     */
    private static boolean checkWithLocalList(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Convertir le texte en minuscules pour une comparaison insensible à la casse
        String lowerText = text.toLowerCase();
        
        // Vérifier chaque mot de la liste
        for (String badWord : DEFAULT_BAD_WORDS) {
            // Utiliser une expression régulière pour trouver le mot entier
            String regex = "\\b" + Pattern.quote(badWord) + "\\b";
            if (Pattern.compile(regex).matcher(lowerText).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Censure les mots inappropriés dans le texte
     * @param text Le texte à censurer
     * @return Le texte avec les mots inappropriés censurés
     */
    public static String censorBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String censoredText = text;
        
        // Censurer chaque mot de la liste
        for (String badWord : DEFAULT_BAD_WORDS) {
            // Créer une chaîne de '*' de la même longueur que le mot
            String replacement = "*".repeat(badWord.length());
            
            // Utiliser une expression régulière pour remplacer le mot entier
            String regex = "(?i)\\b" + Pattern.quote(badWord) + "\\b";
            censoredText = censoredText.replaceAll(regex, replacement);
        }
        
        return censoredText;
    }
} 