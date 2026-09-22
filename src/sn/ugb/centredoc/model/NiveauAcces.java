package sn.ugb.centredoc.model;

/**
 * Niveaux d'accès applicables aux documents du fonds documentaire.
 */
public enum NiveauAcces {
    TELECHARGEABLE("Téléchargeable"),
    CONSULTATION_SEULE("Consultation seule (métadonnées et résumé)"),
    RESTREINT("Restreint (embargo)");

    private final String libelle;

    NiveauAcces(String libelle) {
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
