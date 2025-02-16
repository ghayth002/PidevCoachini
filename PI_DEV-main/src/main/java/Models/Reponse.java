package Models;
import java.util.Date;

public class Reponse {
    private int id;
    private int id_reclamation;
    private Date date_reponse;
    private String contenu;

    // Constructeur
    public Reponse(int id, int id_reclamation, Date date_reponse, String contenu) {
        this.id = id;
        this.id_reclamation = id_reclamation;
        this.date_reponse = date_reponse;
        this.contenu = contenu;
    }

    // Méthodes
    public void afficher_reponse() {
        // TODO: Implémenter l'affichage des détails de la réponse
    }

    public void ajouter_reponse() {
        // TODO: Implémenter l'ajout d'une réponse dans la base de données
    }

    public void supprimer_reponse() {
        // TODO: Implémenter la suppression d'une réponse de la base de données
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getId_reclamation() { return id_reclamation; }
    public void setId_reclamation(int id_reclamation) { this.id_reclamation = id_reclamation; }

    public Date getDate_reponse() { return date_reponse; }
    public void setDate_reponse(Date date_reponse) { this.date_reponse = date_reponse; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    // Méthode toString
    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", id_reclamation=" + id_reclamation +
                ", date_reponse=" + date_reponse +
                ", contenu='" + contenu + '\'' +
                '}';
    }
}
