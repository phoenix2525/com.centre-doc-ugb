package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.dao.UtilisateurDAO;
import sn.ugb.centredoc.model.Role;
import sn.ugb.centredoc.model.Ufr;
import sn.ugb.centredoc.model.Utilisateur;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implémentation en mémoire de UtilisateurDAO utilisant java.util.* (List, ArrayList, Iterator).
 */
public class UtilisateurDAOMemory implements UtilisateurDAO {
    private final DataStore store = DataStore.getInstance();

    @Override
    public List<Utilisateur> listerTous() {
        return new ArrayList<>(store.getUtilisateurs().values());
    }

    @Override
    public List<Utilisateur> rechercher(String critere) {
        List<Utilisateur> resultat = new ArrayList<>();
        String c = (critere != null) ? critere.trim().toLowerCase() : "";
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getNom().toLowerCase().contains(c) ||
                u.getPrenom().toLowerCase().contains(c) ||
                u.getEmail().toLowerCase().contains(c)) {
                resultat.add(u);
            }
        }
        return resultat;
    }

    @Override
    public List<Utilisateur> listerParRole(Role role) {
        List<Utilisateur> resultat = new ArrayList<>();
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getRole() == role) {
                resultat.add(u);
            }
        }
        return resultat;
    }

    @Override
    public Utilisateur trouverParId(int idUtilisateur) {
        return store.getUtilisateurs().get(idUtilisateur);
    }

    @Override
    public Utilisateur trouverParEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase();
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getEmail().toLowerCase().equals(e)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public Utilisateur trouverEtudiant(String prenom, String nom, String email, String codeEtudiant) {
        if (prenom == null || nom == null || email == null || codeEtudiant == null) return null;
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getRole() == Role.ETUDIANT &&
                u.getPrenom().equalsIgnoreCase(prenom.trim()) &&
                u.getNom().equalsIgnoreCase(nom.trim()) &&
                u.getEmail().equalsIgnoreCase(email.trim()) &&
                u.getCodeEtudiant() != null &&
                u.getCodeEtudiant().equalsIgnoreCase(codeEtudiant.trim())) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void ajouter(Utilisateur utilisateur) {
        if (utilisateur.getIdUtilisateur() <= 0) {
            utilisateur.setIdUtilisateur(store.getNextUserId());
        }
        if (utilisateur.getIdUfr() != null && utilisateur.getIdUfr() > 0) {
            Ufr ufr = store.getUfrs().get(utilisateur.getIdUfr());
            if (ufr != null) {
                utilisateur.setNomUfr(ufr.getNom());
            }
        }
        store.getUtilisateurs().put(utilisateur.getIdUtilisateur(), utilisateur);
    }

    @Override
    public void modifier(Utilisateur utilisateur) {
        if (utilisateur.getIdUfr() != null && utilisateur.getIdUfr() > 0) {
            Ufr ufr = store.getUfrs().get(utilisateur.getIdUfr());
            if (ufr != null) {
                utilisateur.setNomUfr(ufr.getNom());
            }
        }
        store.getUtilisateurs().put(utilisateur.getIdUtilisateur(), utilisateur);
    }

    @Override
    public void supprimer(int idUtilisateur) {
        store.getUtilisateurs().remove(idUtilisateur);
    }

    @Override
    public boolean existeEmail(String email, int excludeId) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getIdUtilisateur() != excludeId && u.getEmail().toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existeCodeEtudiant(String codeEtudiant, int excludeId) {
        if (codeEtudiant == null || codeEtudiant.trim().isEmpty()) return false;
        String c = codeEtudiant.trim().toLowerCase();
        for (Utilisateur u : store.getUtilisateurs().values()) {
            if (u.getIdUtilisateur() != excludeId &&
                u.getCodeEtudiant() != null &&
                u.getCodeEtudiant().toLowerCase().equals(c)) {
                return true;
            }
        }
        return false;
    }
}
