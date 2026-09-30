package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.CentreDocException;
import sn.ugb.centredoc.model.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service de notifications visuelles (Bonus 3.3) : informe l'étudiant des nouveaux
 * documents publiés dans sa filière (son UFR).
 * Il combine deux sources (pour éviter les doublons) :
 * 1. Les dépôts récents (date d'ajout de moins de 7 jours) récupérés de la couche DAO,
 *    valables aussi bien en mode MySQL qu'en mode mémoire vive.
 * 2. Les publications enregistrées pendant la session courante,
 *    alimentées par DocumentService à chaque dépôt réussi.
 */
public class NotificationService {

    /** Fenêtre de nouveauté : un document est "nouveau" pendant 7 jours après son dépôt. */
    private static final long FENETRE_NOUVEAUTES_MS = 7L * 24 * 60 * 60 * 1000;

    private static NotificationService instance;

    /** Publications de la session courante : idUfr -> liste de documents (synchronisée). */
    private final Map<Integer, List<Document>> publicationsSession = new HashMap<>();

    private NotificationService() {
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Enregistre une publication de document pour notifier les étudiants de l'UFR concernée.
     * Appelé par DocumentService à chaque dépôt réussi.
     */
    public void notifierPublication(int idUfr, Document doc) {
        if (idUfr <= 0 || doc == null) {
            return;
        }
        synchronized (publicationsSession) {
            List<Document> liste = publicationsSession.get(idUfr);
            if (liste == null) {
                liste = new ArrayList<>();
                publicationsSession.put(idUfr, liste);
            }
            if (!liste.contains(doc)) {
                liste.add(doc);
            }
        }
    }

    /**
     * Liste consolidée et dédupliquée des nouveautés d'une UFR, triée de la plus récente à la plus ancienne.
     */
    public List<Document> listerNouveautes(int idUfr) throws CentreDocException {
        List<Document> nouveautes = new ArrayList<>();
        if (idUfr <= 0) {
            return nouveautes;
        }

        try {
            // Déduplication via un HashSet (l'égalité de Document est basée sur l'identifiant)
            Set<Document> uniques = new HashSet<>();

            // 1. Dépôts récents persistés (fenêtre de 7 jours)
            for (Document d : DAOFactory.getDocumentDAO().listerParUfr(idUfr)) {
                Timestamp dateAjout = d.getDateAjout();
                if (dateAjout != null && (System.currentTimeMillis() - dateAjout.getTime()) <= FENETRE_NOUVEAUTES_MS) {
                    if (uniques.add(d)) {
                        nouveautes.add(d);
                    }
                }
            }

            // 2. Publications de la session courante (au cas où la date d'ajout n'aurait pas été renseignée)
            synchronized (publicationsSession) {
                List<Document> session = publicationsSession.get(idUfr);
                if (session != null) {
                    for (Document d : session) {
                        if (uniques.add(d)) {
                            nouveautes.add(d);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération des notifications : " + e.getMessage(), e);
        }

        // Tri : les dépôts les plus récents en premier (dates nulles en dernier)
        nouveautes.sort((a, b) -> {
            Timestamp ta = a.getDateAjout();
            Timestamp tb = b.getDateAjout();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return nouveautes;
    }
}
