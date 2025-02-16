package Services;

import Models.Reclamation;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant toutes les opérations liées aux réclamations
 * Implémente l'interface Crud pour les opérations standard
 */
public class ReclamationService implements Crud<Reclamation> {
    // Attribut pour la connexion à la base de données
    private Connection conn;

    /**
     * Constructeur du service
     * Initialise la connexion à la base de données
     */
    public ReclamationService() {
        // Obtention de l'instance unique de la base de données
        MyDb database = MyDb.getInstance();
        // Récupération de la connexion
        this.conn = database.getConn();
        // Vérification de la connexion
        if (this.conn == null) {
            System.err.println("Erreur : Connexion à la base de données non établie !");
        }
    }

    /**
     * Crée une nouvelle réclamation dans la base de données
     * @param obj La réclamation à créer
     * @return true si la création est réussie, false sinon
     */
    @Override
    public boolean create(Reclamation obj) throws Exception {
        // Requête SQL pour l'insertion d'une nouvelle réclamation
        String sql = "INSERT INTO reclamation(description, typeR, id_coach, id_adherent, date) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Préparation des paramètres de la requête
            stmt.setString(1, obj.getDescription());    // Ajout de la description
            stmt.setString(2, obj.getType().name());    // Ajout du type de réclamation
            stmt.setInt(3, obj.getId_coach());          // Ajout de l'ID du coach
            stmt.setInt(4, obj.getId_adherent());       // Ajout de l'ID de l'adhérent
            stmt.setDate(5, new java.sql.Date(obj.getDate().getTime()));  // Ajout de la date

            // Exécution de la requête et récupération du résultat
            int res = stmt.executeUpdate();
            if (res > 0) {
                System.out.println("Ajout de la réclamation avec succès !");
                return true;
            } else {
                System.out.println("Aucune réclamation ajoutée.");
            }
        } catch (SQLException e) {
            // Gestion des erreurs SQL
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * Met à jour une réclamation existante
     * @param obj La réclamation à mettre à jour
     * @return true si la mise à jour est réussie, false sinon
     */
    @Override
    public boolean update(Reclamation obj) throws Exception {
        // Requête SQL pour la mise à jour
        String sql = "UPDATE reclamation SET description = ?, typeR = ?, id_coach = ?, id_adherent = ?, date = ? WHERE idReclamation = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Mise à jour des champs
            stmt.setString(1, obj.getDescription());
            stmt.setString(2, obj.getType().name());
            stmt.setInt(3, obj.getId_coach());
            stmt.setInt(4, obj.getId_adherent());
            stmt.setDate(5, new java.sql.Date(obj.getDate().getTime()));
            stmt.setInt(6, obj.getIdReclamation());

            // Exécution de la mise à jour
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Réclamation mise à jour avec succès !");
                return true;
            } else {
                System.out.println("Aucune réclamation mise à jour.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * Supprime une réclamation de la base de données
     * @param id L'identifiant de la réclamation à supprimer
     */
    @Override
    public void delete(int id) throws Exception {
        // Requête SQL pour la suppression
        String sql = "DELETE FROM reclamation WHERE idReclamation = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Configuration de l'ID à supprimer
            stmt.setInt(1, id);

            // Exécution de la suppression
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Suppression de la réclamation réussie !");
            } else {
                System.out.println("Aucune réclamation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Récupère toutes les réclamations de la base de données
     * @return Liste de toutes les réclamations
     */
    @Override
    public List<Reclamation> getAll() throws Exception {
        // Requête SQL pour récupérer toutes les réclamations
        String sql = "SELECT * FROM reclamation";
        // Liste pour stocker les résultats
        List<Reclamation> reclamations = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Parcours des résultats
            while (rs.next()) {
                // Création d'un objet Reclamation pour chaque enregistrement
                Reclamation obj = new Reclamation(
                    rs.getInt("idReclamation"),         // ID de la réclamation
                    rs.getString("description"),         // Description
                    Models.typeR.valueOf(rs.getString("typeR")),  // Type
                    rs.getInt("id_coach"),              // ID du coach
                    rs.getInt("id_adherent"),           // ID de l'adhérent
                    rs.getDate("date")                  // Date
                );
                // Ajout à la liste des réclamations
                reclamations.add(obj);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reclamations;
    }

    /**
     * Récupère une réclamation spécifique par son ID
     * @param id L'identifiant de la réclamation recherchée
     * @return La réclamation trouvée ou null si non trouvée
     */
    @Override
    public Reclamation getById(int id) throws Exception {
        // Requête SQL pour récupérer une réclamation spécifique
        String sql = "SELECT * FROM reclamation WHERE idReclamation = ?";
        Reclamation obj = null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Configuration de l'ID recherché
            stmt.setInt(1, id);
            // Exécution de la requête
            ResultSet rs = stmt.executeQuery();

            // Si une réclamation est trouvée
            if (rs.next()) {
                // Création de l'objet Reclamation avec les données
                obj = new Reclamation(
                    rs.getInt("idReclamation"),
                    rs.getString("description"),
                    Models.typeR.valueOf(rs.getString("type")),
                    rs.getInt("id_coach"),
                    rs.getInt("id_adherent"),
                    rs.getDate("date")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return obj;
    }
}
