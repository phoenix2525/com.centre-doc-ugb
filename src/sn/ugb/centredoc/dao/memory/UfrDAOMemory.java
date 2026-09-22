package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.dao.UfrDAO;
import sn.ugb.centredoc.model.Ufr;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation en mémoire de UfrDAO utilisant les collections Java.
 */
public class UfrDAOMemory implements UfrDAO {
    private final DataStore store = DataStore.getInstance();

    @Override
    public List<Ufr> listerTous() {
        return new ArrayList<>(store.getUfrs().values());
    }

    @Override
    public Ufr trouverParId(int idUfr) {
        return store.getUfrs().get(idUfr);
    }

    @Override
    public Ufr trouverParCode(String code) {
        for (Ufr u : store.getUfrs().values()) {
            if (u.getCode().equalsIgnoreCase(code)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void ajouter(Ufr ufr) {
        if (ufr.getIdUfr() <= 0) {
            ufr.setIdUfr(store.getUfrs().size() + 1);
        }
        store.getUfrs().put(ufr.getIdUfr(), ufr);
    }
}
