package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.dao.TelechargementDAO;
import sn.ugb.centredoc.model.Document;
import sn.ugb.centredoc.model.Telechargement;
import sn.ugb.centredoc.model.Ufr;

import java.util.*;

/**
 * Implémentation en mémoire de TelechargementDAO utilisant java.util.* (List, ArrayList, Map, LinkedHashMap, TreeMap).
 */
public class TelechargementDAOMemory implements TelechargementDAO {
    private final DataStore store = DataStore.getInstance();

    @Override
    public void enregistrer(Telechargement t) {
        store.enregistrerTelechargement(t);
    }

    @Override
    public List<Telechargement> listerTous() {
        List<Telechargement> res = new ArrayList<>(store.getTelechargements());
        res.sort((t1, t2) -> t2.getDateTelechargement().compareTo(t1.getDateTelechargement()));
        return res;
    }

    @Override
    public List<Telechargement> listerParUtilisateur(int idUtilisateur) {
        List<Telechargement> res = new ArrayList<>();
        for (Telechargement t : store.getTelechargements()) {
            if (t.getIdUtilisateur() == idUtilisateur) {
                res.add(t);
            }
        }
        res.sort((t1, t2) -> t2.getDateTelechargement().compareTo(t1.getDateTelechargement()));
        return res;
    }

    @Override
    public List<Telechargement> listerParDocument(int idDocument) {
        List<Telechargement> res = new ArrayList<>();
        for (Telechargement t : store.getTelechargements()) {
            if (t.getIdDocument() == idDocument) {
                res.add(t);
            }
        }
        res.sort((t1, t2) -> t2.getDateTelechargement().compareTo(t1.getDateTelechargement()));
        return res;
    }

    @Override
    public List<Telechargement> listerParUfr(int idUfr) {
        List<Telechargement> res = new ArrayList<>();
        for (Telechargement t : store.getTelechargements()) {
            Document d = store.getDocuments().get(t.getIdDocument());
            if (d != null && d.getIdUfr() == idUfr) {
                res.add(t);
            }
        }
        res.sort((t1, t2) -> t2.getDateTelechargement().compareTo(t1.getDateTelechargement()));
        return res;
    }

    @Override
    public Map<String, Integer> getStatsTopDocuments(int limit) {
        Map<String, Integer> countMap = new HashMap<>();
        for (Telechargement t : store.getTelechargements()) {
            Document d = store.getDocuments().get(t.getIdDocument());
            if (d != null) {
                countMap.put(d.getTitre(), countMap.getOrDefault(d.getTitre(), 0) + 1);
            }
        }
        // Trier par nombre de téléchargements décroissant
        List<Map.Entry<String, Integer>> list = new ArrayList<>(countMap.entrySet());
        list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        Map<String, Integer> result = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count++ >= limit) break;
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public Map<String, Integer> getStatsParUfr() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Ufr u : store.getUfrs().values()) {
            result.put(u.getCode(), 0);
        }
        for (Telechargement t : store.getTelechargements()) {
            Document d = store.getDocuments().get(t.getIdDocument());
            if (d != null) {
                Ufr ufr = store.getUfrs().get(d.getIdUfr());
                if (ufr != null) {
                    result.put(ufr.getCode(), result.getOrDefault(ufr.getCode(), 0) + 1);
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Integer> getStatsParDiscipline() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Document d : store.getDocuments().values()) {
            String dis = d.getDiscipline();
            result.put(dis, result.getOrDefault(dis, 0) + 1);
        }
        return result;
    }

    @Override
    public Map<Integer, Integer> getStatsParAnnee() {
        Map<Integer, Integer> result = new TreeMap<>();
        for (Document d : store.getDocuments().values()) {
            int annee = d.getAnnee();
            result.put(annee, result.getOrDefault(annee, 0) + 1);
        }
        return result;
    }
}
