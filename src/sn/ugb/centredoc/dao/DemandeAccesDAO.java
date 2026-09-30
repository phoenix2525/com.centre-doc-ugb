package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.model.DemandeAcces;
import java.util.List;

/**
 * Interface DAO pour la gestion des demandes d'accès aux documents sous embargo (Bonus 3.3).
 * Manipulation exclusive via java.util.List.
 */
public interface DemandeAccesDAO {
    List<DemandeAcces> listerTous() throws Exception;
    List<DemandeAcces> listerParEtudiant(int idEtudiant) throws Exception;

    /**
     * Liste les demandes portant sur les documents d'une UFR donnée (Règle 4).
     */
    List<DemandeAcces> listerParUfr(int idUfr) throws Exception;

    DemandeAcces trouverParId(int idDemande) throws Exception;
    void ajouter(DemandeAcces demande) throws Exception;

    /**
     * Met à jour une demande (statut de traitement : ACCEPTEE / REFUSEE).
     */
    void modifier(DemandeAcces demande) throws Exception;
    void supprimer(int idDemande) throws Exception;
}
