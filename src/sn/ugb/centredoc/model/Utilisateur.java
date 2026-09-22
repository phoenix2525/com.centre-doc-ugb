package sn.ugb.centredoc.model;

import java.util.Objects;

/**
 * Classe de base représentant un utilisateur du système d'information.
 * Encapsule les données communes et illustre le polymorphisme du cours POO2.
 */
public abstract class Utilisateur {
    protected int idUtilisateur;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String motDePasse;
    protected Role role;
    protected Integer idUfr;
    protected String nomUfr;
    protected String codeEtudiant;

    public Utilisateur() {
    }

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse, Role role) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getIdUfr() {
        return idUfr;
    }

    public void setIdUfr(Integer idUfr) {
        this.idUfr = idUfr;
    }

    public String getNomUfr() {
        return nomUfr;
    }

    public void setNomUfr(String nomUfr) {
        this.nomUfr = nomUfr;
    }

    public String getCodeEtudiant() {
        return codeEtudiant;
    }

    public void setCodeEtudiant(String codeEtudiant) {
        this.codeEtudiant = codeEtudiant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        if (idUtilisateur > 0 && idUtilisateur == that.idUtilisateur) return true;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilisateur, email);
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role + " - " + email + ")";
    }
}
