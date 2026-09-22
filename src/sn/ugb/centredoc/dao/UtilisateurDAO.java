package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.model.Role;
import sn.ugb.centredoc.model.Utilisateur;
import java.util.List;

/**
 * Interface DAO pour la gestion des utilisateurs (Admin, Gestionnaire, Étudiant).
 * Utilise java.util.List conformément aux directives académiques.
 */
public interface UtilisateurDAO {
    List<Utilisateur> listerTous() throws Exception;
    List<Utilisateur> rechercher(String critere) throws Exception;
    List<Utilisateur> listerParRole(Role role) throws Exception;
    Utilisateur trouverParId(int idUtilisateur) throws Exception;
    Utilisateur trouverParEmail(String email) throws Exception;
    Utilisateur trouverEtudiant(String prenom, String nom, String email, String codeEtudiant) throws Exception;
    void ajouter(Utilisateur utilisateur) throws Exception;
    void modifier(Utilisateur utilisateur) throws Exception;
    void supprimer(int idUtilisateur) throws Exception;
    boolean existeEmail(String email, int excludeId) throws Exception;
    boolean existeCodeEtudiant(String codeEtudiant, int excludeId) throws Exception;
}
