package sn.ugb.centredoc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entité représentant une trace de téléchargement d'un document par un utilisateur.
 */
public class Telechargement {
    private int idTelechargement;
    private int idUtilisateur;
    private int idDocument;
    private Timestamp dateTelechargement;

    // Métadonnées associées pour l'affichage dans les tableaux
    private String nomUtilisateur;
    private String emailUtilisateur;
    private String codeEtudiant;
    private String titreDocument;
    private String ufrDocument;

    public Telechargement() {
    }

    public Telechargement(int idTelechargement, int idUtilisateur, int idDocument, Timestamp dateTelechargement) {
        this.idTelechargement = idTelechargement;
        this.idUtilisateur = idUtilisateur;
        this.idDocument = idDocument;
        this.dateTelechargement = dateTelechargement;
    }

    public Telechargement(int idUtilisateur, int idDocument) {
        this(0, idUtilisateur, idDocument, new Timestamp(System.currentTimeMillis()));
    }

    public int getIdTelechargement() {
        return idTelechargement;
    }

    public void setIdTelechargement(int idTelechargement) {
        this.idTelechargement = idTelechargement;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(int idDocument) {
        this.idDocument = idDocument;
    }

    public Timestamp getDateTelechargement() {
        return dateTelechargement;
    }

    public void setDateTelechargement(Timestamp dateTelechargement) {
        this.dateTelechargement = dateTelechargement;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }

    public void setEmailUtilisateur(String emailUtilisateur) {
        this.emailUtilisateur = emailUtilisateur;
    }

    public String getCodeEtudiant() {
        return codeEtudiant;
    }

    public void setCodeEtudiant(String codeEtudiant) {
        this.codeEtudiant = codeEtudiant;
    }

    public String getTitreDocument() {
        return titreDocument;
    }

    public void setTitreDocument(String titreDocument) {
        this.titreDocument = titreDocument;
    }

    public String getUfrDocument() {
        return ufrDocument;
    }

    public void setUfrDocument(String ufrDocument) {
        this.ufrDocument = ufrDocument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Telechargement that = (Telechargement) o;
        return idTelechargement == that.idTelechargement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTelechargement);
    }

    @Override
    public String toString() {
        return "Telechargement #" + idTelechargement + " [" + dateTelechargement + "] doc=" + idDocument + " user=" + idUtilisateur;
    }
}
