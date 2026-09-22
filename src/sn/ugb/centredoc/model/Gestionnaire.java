package sn.ugb.centredoc.model;

/**
 * Entité Gestionnaire de Centre de Documentation : affecté à une UFR spécifique,
 * gère le fonds documentaire de son UFR uniquement.
 */
public class Gestionnaire extends Utilisateur {

    public Gestionnaire() {
        super();
        this.role = Role.GESTIONNAIRE;
    }

    public Gestionnaire(int idUtilisateur, String nom, String prenom, String email, String motDePasse, int idUfr) {
        super(idUtilisateur, nom, prenom, email, motDePasse, Role.GESTIONNAIRE);
        this.idUfr = idUfr;
    }

    public Gestionnaire(String nom, String prenom, String email, String motDePasse, int idUfr) {
        this(0, nom, prenom, email, motDePasse, idUfr);
    }
}
