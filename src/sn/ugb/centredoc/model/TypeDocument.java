package sn.ugb.centredoc.model;

/**
 * Type de document académique : THÈSE ou MÉMOIRE.
 */
public enum TypeDocument {
    THESE("Thèse"),
    MEMOIRE("Mémoire");

    private final String libelle;

    TypeDocument(String libelle) {
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
