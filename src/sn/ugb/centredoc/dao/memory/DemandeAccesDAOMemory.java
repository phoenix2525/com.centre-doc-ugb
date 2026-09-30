package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.dao.DemandeAccesDAO;
import sn.ugb.centredoc.model.DemandeAcces;
import sn.ugb.centredoc.model.Document;
import sn.ugb.centredoc.model.Utilisateur;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implémentation en mémoire de DemandeAccesDAO utilisant java.util.* (List, ArrayList, Comparator).
 */
public class DemandeAccesDAOMemory implements DemandeAccesDAO {
    private final DataStore store = DataStore.getInstance();

    @Override
    public List<DemandeAcces> listerTous() {
        List<DemandeAcces> liste = new ArrayList<>(store.getDemandes().values());
        trierParDateDesc(liste);
        return liste;
    }

    @Override
    public List<DemandeAcces> listerParEtudiant(int idEtudiant) {
        List<DemandeAcces> res = new ArrayList<>();
        for (DemandeAcces da : store.getDemandes().values()) {
            if (da.getIdEtudiant() == idEtudiant) {
                res.add(da);
            }
        }
        trierParDateDesc(res);
        return res;
    }

    @Override
    public List<DemandeAcces> listerParUfr(int idUfr) {
        // Règle 4 : une demande est rattachée à l'UFR du document visé
        List<DemandeAcces> res = new ArrayList<>();
        for (DemandeAcces da : store.getDemandes().values()) {
            Document doc = store.getDocuments().get(da.getIdDocument());
            if (doc != null && doc.getIdUfr() == idUfr) {
                res.add(da);
            }
        }
        trierParDateDesc(res);
        return res;
    }

    @Override
    public DemandeAcces trouverParId(int idDemande) {
        return store.getDemandes().get(idDemande);
    }

    @Override
    public void ajouter(DemandeAcces demande) {
        if (demande.getIdDemande() <= 0) {
            demande.setIdDemande(store.getNextDemandeId());
        }
        if (demande.getDateDemande() == null) {
            demande.setDateDemande(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        enrichirDemande(demande);
        store.getDemandes().put(demande.getIdDemande(), demande);
    }

    @Override
    public void modifier(DemandeAcces demande) {
        enrichirDemande(demande);
        store.getDemandes().put(demande.getIdDemande(), demande);
    }

    @Override
    public void supprimer(int idDemande) {
        store.getDemandes().remove(idDemande);
    }

    /**
     * Alimente les métadonnées d'affichage (étudiant et document) depuis le magasin.
     */
    private void enrichirDemande(DemandeAcces da) {
        Utilisateur u = store.getUtilisateurs().get(da.getIdEtudiant());
        if (u != null) {
            da.setNomEtudiant(u.getNomComplet());
            da.setEmailEtudiant(u.getEmail());
            da.setCodeEtudiant(u.getCodeEtudiant());
        }
        Document d = store.getDocuments().get(da.getIdDocument());
        if (d != null) {
            da.setTitreDocument(d.getTitre());
            da.setUfrDocument(d.getNomUfr());
        }
    }

    private void trierParDateDesc(List<DemandeAcces> liste) {
        liste.sort(Comparator.comparing(DemandeAcces::getDateDemande,
                Comparator.nullsLast(Comparator.reverseOrder())));
    }
}
