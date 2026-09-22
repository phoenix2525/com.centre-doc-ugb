package sn.ugb.centredoc.model;

/**
 * Entité Administrateur : gère les comptes gestionnaires et les centres de documentation.
 */
public class Administrateur extends Utilisateur {

    public Administrateur() {
        super();
        this.role = Role.ADMIN;
    }

    public Administrateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.ADMIN);
    }

    public Administrateur(String nom, String prenom, String email, String motDePasse) {
        this(0, nom, prenom, email, motDePasse);
    }
}
