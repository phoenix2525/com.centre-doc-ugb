package sn.ugb.centredoc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entité représentant un document académique (Thèse ou Mémoire) dans le fonds documentaire.
 */
public class Document {
    private int idDocument;
    private String titre;
    private String auteur;       // Étudiant rédacteur
    private String encadrant;    // Directeur de recherche / encadrant
    private int annee;
    private TypeDocument type;
    private int idUfr;
    private String nomUfr;
    private String discipline;
    private String resume;
    private String motsCles;
    private String cheminPdf;
    private NiveauAcces niveauAcces;
    private Timestamp dateAjout;

    public Document() {
        this.niveauAcces = NiveauAcces.TELECHARGEABLE;
    }

    public Document(int idDocument, String titre, String auteur, String encadrant, int annee,
                    TypeDocument type, int idUfr, String discipline, String resume,
                    String motsCles, String cheminPdf, NiveauAcces niveauAcces) {
        this.idDocument = idDocument;
        this.titre = titre;
        this.auteur = auteur;
        this.encadrant = encadrant;
        this.annee = annee;
        this.type = type;
        this.idUfr = idUfr;
        this.discipline = discipline;
        this.resume = resume;
        this.motsCles = motsCles;
        this.cheminPdf = cheminPdf;
        this.niveauAcces = (niveauAcces != null) ? niveauAcces : NiveauAcces.TELECHARGEABLE;
    }

    public Document(String titre, String auteur, String encadrant, int annee,
                    TypeDocument type, int idUfr, String discipline, String resume,
                    String motsCles, String cheminPdf, NiveauAcces niveauAcces) {
        this(0, titre, auteur, encadrant, annee, type, idUfr, discipline, resume, motsCles, cheminPdf, niveauAcces);
    }

    public int getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(int idDocument) {
        this.idDocument = idDocument;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getEncadrant() {
        return encadrant;
    }

    public void setEncadrant(String encadrant) {
        this.encadrant = encadrant;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public TypeDocument getType() {
        return type;
    }

    public void setType(TypeDocument type) {
        this.type = type;
    }

    public int getIdUfr() {
        return idUfr;
    }

    public void setIdUfr(int idUfr) {
        this.idUfr = idUfr;
    }

    public String getNomUfr() {
        return nomUfr;
    }

    public void setNomUfr(String nomUfr) {
        this.nomUfr = nomUfr;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public void setMotsCles(String motsCles) {
        this.motsCles = motsCles;
    }

    public String getCheminPdf() {
        return cheminPdf;
    }

    public void setCheminPdf(String cheminPdf) {
        this.cheminPdf = cheminPdf;
    }

    public NiveauAcces getNiveauAcces() {
        return niveauAcces;
    }

    public void setNiveauAcces(NiveauAcces niveauAcces) {
        this.niveauAcces = niveauAcces;
    }

    public Timestamp getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Timestamp dateAjout) {
        this.dateAjout = dateAjout;
    }

    public boolean isTelechargeable() {
        return this.niveauAcces == NiveauAcces.TELECHARGEABLE;
    }

    public boolean isConsultationSeule() {
        return this.niveauAcces == NiveauAcces.CONSULTATION_SEULE;
    }

    public boolean isRestreint() {
        return this.niveauAcces == NiveauAcces.RESTREINT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return idDocument == document.idDocument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDocument);
    }

    @Override
    public String toString() {
        return "[" + type + "] " + titre + " (" + annee + ") - " + auteur;
    }
}
