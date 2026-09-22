package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.dao.DocumentDAO;
import sn.ugb.centredoc.model.Document;
import sn.ugb.centredoc.model.Ufr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implémentation en mémoire de DocumentDAO utilisant java.util.* (List, ArrayList, Comparator).
 */
public class DocumentDAOMemory implements DocumentDAO {
    private final DataStore store = DataStore.getInstance();

    @Override
    public List<Document> listerTous() {
        List<Document> liste = new ArrayList<>(store.getDocuments().values());
        liste.sort((d1, d2) -> {
            int comp = Integer.compare(d2.getAnnee(), d1.getAnnee());
            return (comp != 0) ? comp : d1.getTitre().compareToIgnoreCase(d2.getTitre());
        });
        return liste;
    }

    @Override
    public List<Document> listerParUfr(int idUfr) {
        List<Document> res = new ArrayList<>();
        for (Document d : store.getDocuments().values()) {
            if (d.getIdUfr() == idUfr) {
                res.add(d);
            }
        }
        res.sort((d1, d2) -> Integer.compare(d2.getAnnee(), d1.getAnnee()));
        return res;
    }

    @Override
    public List<Document> rechercher(String titre, String auteur, String motCle, Integer annee, Integer idUfr, String discipline) {
        List<Document> res = new ArrayList<>();
        String t = (titre != null) ? titre.trim().toLowerCase() : "";
        String a = (auteur != null) ? auteur.trim().toLowerCase() : "";
        String m = (motCle != null) ? motCle.trim().toLowerCase() : "";
        String dis = (discipline != null) ? discipline.trim().toLowerCase() : "";

        for (Document doc : store.getDocuments().values()) {
            if (!t.isEmpty() && !doc.getTitre().toLowerCase().contains(t)) continue;
            if (!a.isEmpty() && !doc.getAuteur().toLowerCase().contains(a)) continue;
            if (!m.isEmpty() && !doc.getMotsCles().toLowerCase().contains(m)) continue;
            if (annee != null && annee > 0 && doc.getAnnee() != annee) continue;
            if (idUfr != null && idUfr > 0 && doc.getIdUfr() != idUfr) continue;
            if (!dis.isEmpty() && !doc.getDiscipline().toLowerCase().contains(dis)) continue;
            res.add(doc);
        }

        res.sort((d1, d2) -> Integer.compare(d2.getAnnee(), d1.getAnnee()));
        return res;
    }

    @Override
    public Document trouverParId(int idDocument) {
        return store.getDocuments().get(idDocument);
    }

    @Override
    public void ajouter(Document doc) {
        if (doc.getIdDocument() <= 0) {
            doc.setIdDocument(store.getNextDocId());
        }
        if (doc.getDateAjout() == null) {
            doc.setDateAjout(new Timestamp(System.currentTimeMillis()));
        }
        Ufr ufr = store.getUfrs().get(doc.getIdUfr());
        if (ufr != null) {
            doc.setNomUfr(ufr.getNom());
        }
        store.getDocuments().put(doc.getIdDocument(), doc);
    }

    @Override
    public void modifier(Document doc) {
        Ufr ufr = store.getUfrs().get(doc.getIdUfr());
        if (ufr != null) {
            doc.setNomUfr(ufr.getNom());
        }
        store.getDocuments().put(doc.getIdDocument(), doc);
    }

    @Override
    public void supprimer(int idDocument) {
        store.getDocuments().remove(idDocument);
    }
}
