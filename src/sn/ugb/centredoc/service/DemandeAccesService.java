package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.DemandeAccesDAO;
import sn.ugb.centredoc.dao.DocumentDAO;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service métier des demandes d'accès aux documents sous embargo (Bonus 3.3).
 * Flux : l'étudiant soumet une demande (référence du document + motif), le gestionnaire
 * de l'UFR du document visé l'accepte ou la refuse (Règle 4).
 * L'acceptation accorde UNIQUEMENT la consultation des métadonnées et du résumé :
 * le téléchargement du PDF reste formellement bloqué (Règles 1, 2 et 3).
 */
public class DemandeAccesService {
    private final DemandeAccesDAO demandeAccesDAO;
    private final DocumentDAO documentDAO;

    public DemandeAccesService() {
        this.demandeAccesDAO = DAOFactory.getDemandeAccesDAO();
        this.documentDAO = DAOFactory.getDocumentDAO();
    }

    /**
     * Soumission d'une demande d'accès par un étudiant.
     * Le document visé est résolu à partir de la référence saisie (sans exposer le catalogue,
     * conformément à la Règle 3). Seuls les documents RESTREINT peuvent faire l'objet d'une demande.
     */
    public DemandeAcces creerDemande(Etudiant etudiant, String titreDocument, String motif)
            throws ChampInvalideException, DocumentIntrouvableException, DoublonException, CentreDocException {
        if (etudiant == null) {
            throw new AccesRefuseException("Vous devez être connecté en tant qu'étudiant pour soumettre une demande d'accès.");
        }
        if (titreDocument == null || titreDocument.trim().isEmpty()) {
            throw new ChampInvalideException("La référence (titre) du document demandé est obligatoire.");
        }
        if (motif == null || motif.trim().isEmpty()) {
            throw new ChampInvalideException("Le motif de la demande d'accès est obligatoire.");
        }

        // Résolution du document visé : correspondance exacte d'abord, puis correspondance partielle
        Document cible = null;
        try {
            List<Document> tous = documentDAO.listerTous();
            for (Document d : tous) {
                if (d.getTitre().trim().equalsIgnoreCase(titreDocument.trim())) {
                    cible = d;
                    break;
                }
            }
            if (cible == null) {
                String recherche = titreDocument.trim().toLowerCase();
                List<Document> candidats = new ArrayList<>();
                for (Document d : tous) {
                    if (d.getTitre().toLowerCase().contains(recherche)) {
                        candidats.add(d);
                    }
                }
                if (candidats.size() == 1) {
                    cible = candidats.get(0);
                } else if (candidats.size() > 1) {
                    throw new ChampInvalideException("Plusieurs documents correspondent à cette référence (" + candidats.size() + " résultats). Veuillez saisir le titre exact du document.");
                }
            }
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la recherche du document visé : " + e.getMessage(), e);
        }

        if (cible == null) {
            throw new DocumentIntrouvableException("Aucun document du fonds ne correspond à la référence saisie. Vérifiez le titre exact auprès de votre centre de documentation.");
        }
        if (cible.getNiveauAcces() != NiveauAcces.RESTREINT) {
            throw new ChampInvalideException("Ce document n'est pas sous embargo : il est directement accessible dans le catalogue. Une demande d'accès n'est pas nécessaire.");
        }

        // Contrôle d'unicité : une seule demande active (EN_ATTENTE ou ACCEPTEE) par étudiant et par document
        try {
            List<DemandeAcces> existantes = demandeAccesDAO.listerParEtudiant(etudiant.getIdUtilisateur());
            for (DemandeAcces da : existantes) {
                if (da.getIdDocument() == cible.getIdDocument() && da.getStatut() != StatutDemande.REFUSEE) {
                    throw new DoublonException("Vous avez déjà soumis une demande d'accès pour ce document (statut : " + da.getStatut().getLibelle() + ").");
                }
            }
        } catch (DoublonException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la vérification des demandes existantes : " + e.getMessage(), e);
        }

        DemandeAcces demande = new DemandeAcces(etudiant.getIdUtilisateur(), cible.getIdDocument(), motif.trim());
        try {
            demandeAccesDAO.ajouter(demande);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de l'enregistrement de la demande d'accès : " + e.getMessage(), e);
        }
        return demande;
    }

    /**
     * Traitement (acceptation ou refus) d'une demande par un gestionnaire ou l'administrateur.
     * Règle 4 : un gestionnaire ne peut traiter que les demandes portant sur les documents de son UFR.
     */
    public void traiterDemande(int idDemande, boolean accepter, Utilisateur operateur)
            throws AccesRefuseException, DocumentIntrouvableException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }

        DemandeAcces demande;
        Document doc;
        try {
            demande = demandeAccesDAO.trouverParId(idDemande);
            if (demande == null) {
                throw new DocumentIntrouvableException("Demande d'accès introuvable (ID " + idDemande + ").");
            }
            doc = documentDAO.trouverParId(demande.getIdDocument());
            if (doc == null) {
                throw new DocumentIntrouvableException("Le document visé par cette demande n'existe plus dans le fonds.");
            }
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération de la demande : " + e.getMessage(), e);
        }

        // Cloisonnement UFR (Règle 4) : le gestionnaire ne traite que les demandes de son UFR
        if (operateur.getRole() == Role.GESTIONNAIRE) {
            if (operateur.getIdUfr() == null || operateur.getIdUfr() != doc.getIdUfr()) {
                throw new AccesRefuseException("Accès refusé : vous ne pouvez traiter que les demandes portant sur les documents de votre UFR.");
            }
        } else if (operateur.getRole() != Role.ADMIN) {
            throw new AccesRefuseException("Action non autorisée pour votre profil.");
        }

        demande.setStatut(accepter ? StatutDemande.ACCEPTEE : StatutDemande.REFUSEE);
        try {
            demandeAccesDAO.modifier(demande);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors du traitement de la demande d'accès : " + e.getMessage(), e);
        }
    }

    /**
     * Liste les demandes pour un gestionnaire (documents de son UFR uniquement, Règle 4) ou pour l'admin (toutes).
     */
    public List<DemandeAcces> listerPourGestionnaire(Utilisateur operateur)
            throws AccesRefuseException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }
        try {
            if (operateur.getRole() == Role.GESTIONNAIRE) {
                if (operateur.getIdUfr() == null) {
                    throw new AccesRefuseException("Aucune UFR n'est affectée à votre compte gestionnaire.");
                }
                return demandeAccesDAO.listerParUfr(operateur.getIdUfr());
            } else if (operateur.getRole() == Role.ADMIN) {
                return demandeAccesDAO.listerTous();
            } else {
                throw new AccesRefuseException("Profil non autorisé à consulter les demandes d'accès.");
            }
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération des demandes d'accès : " + e.getMessage(), e);
        }
    }

    /**
     * Liste les demandes soumises par un étudiant (son propre registre).
     */
    public List<DemandeAcces> listerPourEtudiant(Etudiant etudiant) throws CentreDocException {
        if (etudiant == null) {
            return new ArrayList<>();
        }
        try {
            return demandeAccesDAO.listerParEtudiant(etudiant.getIdUtilisateur());
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération de vos demandes d'accès : " + e.getMessage(), e);
        }
    }

    /**
     * Indique si un étudiant dispose d'une demande d'accès ACCEPTEE pour un document donné.
     * Utilisé par DocumentService pour autoriser la consultation d'un document sous embargo.
     */
    public boolean aAccesAccorde(int idEtudiant, int idDocument) {
        try {
            for (DemandeAcces da : demandeAccesDAO.listerParEtudiant(idEtudiant)) {
                if (da.getIdDocument() == idDocument && da.getStatut() == StatutDemande.ACCEPTEE) {
                    return true;
                }
            }
        } catch (Exception e) {
            // En cas d'erreur technique, on refuse l'accès par sécurité
            return false;
        }
        return false;
    }
}
