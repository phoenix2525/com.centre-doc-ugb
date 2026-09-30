package sn.ugb.centredoc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entité représentant une demande d'accès formulée par un étudiant pour
 * consulter un document placé sous embargo (accès restreint) — Bonus 3.3.
 * L'acceptation de la demande accorde uniquement la consultation des
 * métadonnées et du résumé : le téléchargement du PDF reste formellement bloqué (Règle 1).
 */
public class DemandeAcces {
    private int idDemande;
    private int idEtudiant;
    private int idDocument;
    private String motif;
    private StatutDemande statut;
    private Timestamp dateDemande;

    // Métadonnées associées pour l'affichage dans les tableaux
    private String nomEtudiant;
    private String emailEtudiant;
    private String codeEtudiant;
    private String titreDocument;
    private String ufrDocument;

    public DemandeAcces() {
        this.statut = StatutDemande.EN_ATTENTE;
    }

    public DemandeAcces(int idEtudiant, int idDocument, String motif) {
        this(0, idEtudiant, idDocument, motif, StatutDemande.EN_ATTENTE,
                new Timestamp(System.currentTimeMillis()));
    }

    public DemandeAcces(int idDemande, int idEtudiant, int idDocument, String motif,
                        StatutDemande statut, Timestamp dateDemande) {
        this.idDemande = idDemande;
        this.idEtudiant = idEtudiant;
        this.idDocument = idDocument;
        this.motif = motif;
        this.statut = (statut != null) ? statut : StatutDemande.EN_ATTENTE;
        this.dateDemande = dateDemande;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public int getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(int idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public int getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(int idDocument) {
        this.idDocument = idDocument;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }

    public Timestamp getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Timestamp dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public void setNomEtudiant(String nomEtudiant) {
        this.nomEtudiant = nomEtudiant;
    }

    public String getEmailEtudiant() {
        return emailEtudiant;
    }

    public void setEmailEtudiant(String emailEtudiant) {
        this.emailEtudiant = emailEtudiant;
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

    public boolean isEnAttente() {
        return this.statut == StatutDemande.EN_ATTENTE;
    }

    public boolean isAcceptee() {
        return this.statut == StatutDemande.ACCEPTEE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemandeAcces demande = (DemandeAcces) o;
        return idDemande == demande.idDemande;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDemande);
    }

    @Override
    public String toString() {
        return "Demande #" + idDemande + " [" + statut + "] etudiant=" + idEtudiant + " doc=" + idDocument;
    }
}
