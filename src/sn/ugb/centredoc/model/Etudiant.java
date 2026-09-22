package sn.ugb.centredoc.model;

/**
 * Entité Étudiant : s'authentifie sans mot de passe (nom, prénom, email UGB, code étudiant).
 * Peut rechercher et télécharger les documents selon leur niveau d'accès.
 */
public class Etudiant extends Utilisateur {

    public Etudiant() {
        super();
        this.role = Role.ETUDIANT;
    }

    public Etudiant(int idUtilisateur, String nom, String prenom, String email, String codeEtudiant, Integer idUfr) {
        super(idUtilisateur, nom, prenom, email, null, Role.ETUDIANT);
        this.codeEtudiant = codeEtudiant;
        this.idUfr = idUfr;
    }

    public Etudiant(String nom, String prenom, String email, String codeEtudiant, Integer idUfr) {
        this(0, nom, prenom, email, codeEtudiant, idUfr);
    }
}
