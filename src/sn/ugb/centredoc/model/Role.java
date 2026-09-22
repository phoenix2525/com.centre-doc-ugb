package sn.ugb.centredoc.model;

/**
 * Rôles des acteurs du système d'information.
 */
public enum Role {
    ADMIN("Administrateur"),
    GESTIONNAIRE("Gestionnaire de Centre"),
    ETUDIANT("Étudiant");

    private final String libelle;

    Role(String libelle) {
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
