package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.UtilisateurDAO;
import sn.ugb.centredoc.exception.AuthentificationException;
import sn.ugb.centredoc.exception.ChampInvalideException;
import sn.ugb.centredoc.model.Etudiant;
import sn.ugb.centredoc.model.Role;
import sn.ugb.centredoc.model.Utilisateur;

/**
 * Service gérant l'authentification des différents acteurs.
 * Applique strictement les règles de validation et lève les exceptions appropriées.
 */
public class AuthService {
    private final UtilisateurDAO utilisateurDAO;

    public AuthService() {
        this.utilisateurDAO = DAOFactory.getUtilisateurDAO();
    }

    /**
     * Valide le format officiel de l'adresse email UGB (@ugb.edu.sn).
     */
    public static void validerEmailUGB(String email) throws ChampInvalideException {
        if (email == null || email.trim().isEmpty()) {
            throw new ChampInvalideException("L'adresse email est obligatoire.");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!cleanEmail.endsWith("@ugb.edu.sn") || cleanEmail.length() <= "@ugb.edu.sn".length()) {
            throw new ChampInvalideException("L'email doit impérativement être une adresse UGB valide (ex: prenom.nom@ugb.edu.sn).");
        }
    }

    /**
     * Authentification pour le personnel (Administrateur ou Gestionnaire) avec email et mot de passe.
     */
    public Utilisateur authentifierPersonnel(String email, String motDePasse)
            throws AuthentificationException, ChampInvalideException {
        validerEmailUGB(email);

        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            throw new ChampInvalideException("Le mot de passe est obligatoire.");
        }

        try {
            Utilisateur user = utilisateurDAO.trouverParEmail(email.trim().toLowerCase());
            if (user == null) {
                throw new AuthentificationException("Aucun compte personnel trouvé avec l'email : " + email);
            }

            if (user.getRole() == Role.ETUDIANT) {
                throw new AuthentificationException("Ce compte est un compte étudiant. Veuillez utiliser l'onglet 'Espace Étudiant'.");
            }

            if (!motDePasse.equals(user.getMotDePasse())) {
                throw new AuthentificationException("Mot de passe incorrect.");
            }

            return user;
        } catch (AuthentificationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthentificationException("Erreur système lors de l'authentification : " + e.getMessage());
        }
    }

    /**
     * Authentification pour les étudiants SANS mot de passe (prénom, nom, email UGB et code étudiant).
     * Règle 7 du sujet : les étudiants ne saisissent pas de mot de passe.
     */
    public Etudiant authentifierEtudiant(String prenom, String nom, String email, String codeEtudiant)
            throws AuthentificationException, ChampInvalideException {
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new ChampInvalideException("Le prénom de l'étudiant est obligatoire.");
        }
        if (nom == null || nom.trim().isEmpty()) {
            throw new ChampInvalideException("Le nom de l'étudiant est obligatoire.");
        }
        validerEmailUGB(email);
        if (codeEtudiant == null || codeEtudiant.trim().isEmpty()) {
            throw new ChampInvalideException("Le code étudiant est obligatoire.");
        }

        try {
            Utilisateur user = utilisateurDAO.trouverEtudiant(
                    prenom.trim(),
                    nom.trim(),
                    email.trim().toLowerCase(),
                    codeEtudiant.trim()
            );

            if (user == null) {
                throw new AuthentificationException("Authentification échouée : les informations saisies ne correspondent à aucun étudiant enregistré.");
            }

            if (!(user instanceof Etudiant)) {
                throw new AuthentificationException("L'utilisateur trouvé n'a pas le profil Étudiant.");
            }

            return (Etudiant) user;
        } catch (AuthentificationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthentificationException("Erreur système lors de l'authentification étudiant : " + e.getMessage());
        }
    }
}
