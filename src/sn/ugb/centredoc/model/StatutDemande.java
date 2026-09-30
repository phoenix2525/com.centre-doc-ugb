package sn.ugb.centredoc.model;

/**
 * Statuts applicables à une demande d'accès à un document sous embargo (Bonus 3.3).
 */
public enum StatutDemande {
    EN_ATTENTE("En attente de validation"),
    ACCEPTEE("Accès accordé"),
    REFUSEE("Demande refusée");

    private final String libelle;

    StatutDemande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
