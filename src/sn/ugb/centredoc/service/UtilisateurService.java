package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.UfrDAO;
import sn.ugb.centredoc.dao.UtilisateurDAO;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;

import java.util.List;

/**
 * Service métier pour la gestion des utilisateurs (Module Administrateur).
 */
public class UtilisateurService {
    private final UtilisateurDAO utilisateurDAO;
    private final UfrDAO ufrDAO;

    public UtilisateurService() {
        this.utilisateurDAO = DAOFactory.getUtilisateurDAO();
        this.ufrDAO = DAOFactory.getUfrDAO();
    }

    private void verifierDroitAdmin(Utilisateur operateur) throws AccesRefuseException {
        if (operateur == null || operateur.getRole() != Role.ADMIN) {
            throw new AccesRefuseException("Accès refusé : Seul un administrateur peut gérer les utilisateurs.");
        }
    }

    /**
     * Crée un nouveau compte gestionnaire affecté à une UFR.
     */
    public Gestionnaire creerGestionnaire(String nom, String prenom, String email, String motDePasse,
                                         int idUfr, Utilisateur operateur)
            throws AccesRefuseException, ChampInvalideException, DoublonException, CentreDocException {
        verifierDroitAdmin(operateur);

        if (nom == null || nom.trim().isEmpty()) {
            throw new ChampInvalideException("Le nom du gestionnaire est obligatoire.");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new ChampInvalideException("Le prénom du gestionnaire est obligatoire.");
        }
        AuthService.validerEmailUGB(email);

        if (motDePasse == null || motDePasse.trim().length() < 4) {
            throw new ChampInvalideException("Le mot de passe doit comporter au moins 4 caractères.");
        }
        if (idUfr <= 0) {
            throw new ChampInvalideException("L'affectation à une UFR est obligatoire pour un gestionnaire.");
        }

        try {
            // Vérification de l'UFR
            Ufr ufr = ufrDAO.trouverParId(idUfr);
            if (ufr == null) {
                throw new ChampInvalideException("L'UFR sélectionnée (ID " + idUfr + ") n'existe pas.");
            }

            // Vérification de l'unicité de l'email
            if (utilisateurDAO.existeEmail(email.trim(), 0)) {
                throw new DoublonException("Un utilisateur avec l'email " + email + " existe déjà.");
            }

            Gestionnaire g = new Gestionnaire(nom.trim().toUpperCase(), prenom.trim(), email.trim().toLowerCase(), motDePasse, idUfr);
            g.setNomUfr(ufr.getNom());
            utilisateurDAO.ajouter(g);
            return g;
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la création du gestionnaire : " + e.getMessage(), e);
        }
    }

    /**
     * Modifie un compte gestionnaire existant.
     */
    public void modifierGestionnaire(int idGestionnaire, String nom, String prenom, String email,
                                    String motDePasse, int idUfr, Utilisateur operateur)
            throws AccesRefuseException, ChampInvalideException, DoublonException, DocumentIntrouvableException, CentreDocException {
        verifierDroitAdmin(operateur);

        if (nom == null || nom.trim().isEmpty()) {
            throw new ChampInvalideException("Le nom est obligatoire.");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new ChampInvalideException("Le prénom est obligatoire.");
        }
        AuthService.validerEmailUGB(email);
        if (idUfr <= 0) {
            throw new ChampInvalideException("L'affectation à une UFR est obligatoire pour un gestionnaire.");
        }

        try {
            Utilisateur existant = utilisateurDAO.trouverParId(idGestionnaire);
            if (existant == null) {
                throw new DocumentIntrouvableException("Gestionnaire introuvable (ID " + idGestionnaire + ").");
            }
            if (existant.getRole() != Role.GESTIONNAIRE) {
                throw new AccesRefuseException("L'utilisateur ciblé n'est pas un gestionnaire.");
            }

            Ufr ufr = ufrDAO.trouverParId(idUfr);
            if (ufr == null) {
                throw new ChampInvalideException("L'UFR sélectionnée n'existe pas.");
            }

            // Vérifier doublon email (en excluant l'utilisateur courant)
            if (utilisateurDAO.existeEmail(email.trim(), idGestionnaire)) {
                throw new DoublonException("L'email " + email + " est déjà utilisé par un autre compte.");
            }

            existant.setNom(nom.trim().toUpperCase());
            existant.setPrenom(prenom.trim());
            existant.setEmail(email.trim().toLowerCase());
            if (motDePasse != null && !motDePasse.trim().isEmpty()) {
                existant.setMotDePasse(motDePasse);
            }
            existant.setIdUfr(idUfr);
            existant.setNomUfr(ufr.getNom());

            utilisateurDAO.modifier(existant);
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la modification du gestionnaire : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un compte gestionnaire.
     */
    public void supprimerGestionnaire(int idGestionnaire, Utilisateur operateur)
            throws AccesRefuseException, DocumentIntrouvableException, CentreDocException {
        verifierDroitAdmin(operateur);

        try {
            Utilisateur existant = utilisateurDAO.trouverParId(idGestionnaire);
            if (existant == null) {
                throw new DocumentIntrouvableException("Utilisateur introuvable (ID " + idGestionnaire + ").");
            }
            if (existant.getRole() == Role.ADMIN) {
                throw new AccesRefuseException("Impossible de supprimer un compte administrateur.");
            }

            utilisateurDAO.supprimer(idGestionnaire);
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la suppression de l'utilisateur : " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre un nouvel étudiant dans le système.
     */
    public Etudiant creerEtudiant(String nom, String prenom, String email, String codeEtudiant,
                                 Integer idUfr, Utilisateur operateur)
            throws AccesRefuseException, ChampInvalideException, DoublonException, CentreDocException {
        verifierDroitAdmin(operateur);

        if (nom == null || nom.trim().isEmpty()) {
            throw new ChampInvalideException("Le nom de l'étudiant est obligatoire.");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new ChampInvalideException("Le prénom de l'étudiant est obligatoire.");
        }
        AuthService.validerEmailUGB(email);
        if (codeEtudiant == null || codeEtudiant.trim().isEmpty()) {
            throw new ChampInvalideException("Le code étudiant est obligatoire.");
        }

        try {
            if (utilisateurDAO.existeEmail(email.trim(), 0)) {
                throw new DoublonException("L'email " + email + " est déjà associé à un autre compte.");
            }
            if (utilisateurDAO.existeCodeEtudiant(codeEtudiant.trim(), 0)) {
                throw new DoublonException("Le code étudiant " + codeEtudiant + " est déjà attribué.");
            }

            Etudiant etu = new Etudiant(nom.trim().toUpperCase(), prenom.trim(), email.trim().toLowerCase(), codeEtudiant.trim().toUpperCase(), idUfr);
            if (idUfr != null && idUfr > 0) {
                Ufr ufr = ufrDAO.trouverParId(idUfr);
                if (ufr != null) {
                    etu.setNomUfr(ufr.getNom());
                }
            }
            utilisateurDAO.ajouter(etu);
            return etu;
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la création de l'étudiant : " + e.getMessage(), e);
        }
    }

    /**
     * Liste tous les utilisateurs du système (pour l'administrateur).
     */
    public List<Utilisateur> listerTous(Utilisateur operateur) throws AccesRefuseException, CentreDocException {
        verifierDroitAdmin(operateur);
        try {
            return utilisateurDAO.listerTous();
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération des utilisateurs : " + e.getMessage(), e);
        }
    }

    /**
     * Recherche des utilisateurs par nom ou email (pour l'administrateur).
     */
    public List<Utilisateur> rechercher(String critere, Utilisateur operateur) throws AccesRefuseException, CentreDocException {
        verifierDroitAdmin(operateur);
        try {
            return utilisateurDAO.rechercher(critere);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la recherche des utilisateurs : " + e.getMessage(), e);
        }
    }

    public List<Ufr> listerUfrs() throws CentreDocException {
        try {
            return ufrDAO.listerTous();
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération des UFR : " + e.getMessage(), e);
        }
    }
}
