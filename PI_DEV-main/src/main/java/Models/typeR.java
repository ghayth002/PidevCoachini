package Models;

/**
 * Énumération des types de réclamations possibles dans l'application
 * Cette énumération permet de standardiser les catégories de réclamations
 * et de limiter les types possibles à des valeurs prédéfinies
 */
public enum typeR {
    /**
     * Réclamations liées aux produits
     * Exemple: qualité du produit, problème de livraison, etc.
     */
    PRODUIT,

    /**
     * Réclamations concernant les coachs
     * Exemple: comportement, professionnalisme, retards, etc.
     */
    COACH,

    /**
     * Réclamations venant des adhérents
     * Exemple: problèmes d'accès, difficultés avec les services, etc.
     */
    ADHERENT,

    /**
     * Réclamations liées aux événements
     * Exemple: organisation, annulation, problèmes lors de l'événement, etc.
     */
    EVENEMENT
}
