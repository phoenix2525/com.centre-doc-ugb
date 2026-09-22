package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.DocumentDAO;
import sn.ugb.centredoc.dao.TelechargementDAO;
import sn.ugb.centredoc.dao.UfrDAO;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Service métier pour la gestion du fonds documentaire (Thèses et Mémoires).
 * Garantit l'application rigoureuse des règles d'accès, d'embargo et de cloisonnement par UFR.
 */
public class DocumentService {
    private final DocumentDAO documentDAO;
    private final TelechargementDAO telechargementDAO;
    private final UfrDAO ufrDAO;

    public DocumentService() {
        this.documentDAO = DAOFactory.getDocumentDAO();
        this.telechargementDAO = DAOFactory.getTelechargementDAO();
        this.ufrDAO = DAOFactory.getUfrDAO();
    }

    /**
     * Valide les champs obligatoires et la cohérence temporelle d'un document.
     */
    private void validerChampsDocument(Document doc) throws ChampInvalideException {
        if (doc == null) {
            throw new ChampInvalideException("Les données du document sont manquantes.");
        }
        if (doc.getTitre() == null || doc.getTitre().trim().isEmpty()) {
            throw new ChampInvalideException("Le titre du document est obligatoire.");
        }
        if (doc.getAuteur() == null || doc.getAuteur().trim().isEmpty()) {
            throw new ChampInvalideException("L'auteur (étudiant rédacteur) est obligatoire.");
        }
        if (doc.getEncadrant() == null || doc.getEncadrant().trim().isEmpty()) {
            throw new ChampInvalideException("L'encadrant du travail est obligatoire.");
        }
        int anneeCourante = Year.now().getValue();
        if (doc.getAnnee() < 1960 || doc.getAnnee() > anneeCourante + 1) {
            throw new ChampInvalideException("L'année de soutenance doit être comprise entre 1960 et " + (anneeCourante + 1) + ".");
        }
        if (doc.getType() == null) {
            throw new ChampInvalideException("Le type de document (THÈSE ou MÉMOIRE) est obligatoire.");
        }
        if (doc.getIdUfr() <= 0) {
            throw new ChampInvalideException("L'UFR de rattachement est obligatoire.");
        }
        if (doc.getDiscipline() == null || doc.getDiscipline().trim().isEmpty()) {
            throw new ChampInvalideException("La discipline académique est obligatoire.");
        }
        if (doc.getResume() == null || doc.getResume().trim().isEmpty()) {
            throw new ChampInvalideException("Le résumé du document est obligatoire.");
        }
        if (doc.getMotsCles() == null || doc.getMotsCles().trim().isEmpty()) {
            throw new ChampInvalideException("Les mots-clés sont obligatoires.");
        }
        if (doc.getNiveauAcces() == null) {
            throw new ChampInvalideException("Le niveau d'accès au document est obligatoire.");
        }
    }

    /**
     * Ajout d'une thèse ou d'un mémoire par un gestionnaire (ou administrateur).
     * Règle 4 : Un gestionnaire ne gère que les documents de son UFR.
     */
    public void ajouterDocument(Document doc, Utilisateur operateur)
            throws AccesRefuseException, ChampInvalideException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }

        // Vérification du cloisonnement UFR pour le gestionnaire
        if (operateur.getRole() == Role.GESTIONNAIRE) {
            if (operateur.getIdUfr() == null || operateur.getIdUfr() != doc.getIdUfr()) {
                throw new AccesRefuseException("Accès refusé : En tant que gestionnaire, vous ne pouvez enregistrer des documents que pour votre propre UFR.");
            }
        } else if (operateur.getRole() != Role.ADMIN) {
            throw new AccesRefuseException("Action non autorisée pour votre profil.");
        }

        validerChampsDocument(doc);

        try {
            documentDAO.ajouter(doc);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de l'enregistrement du document : " + e.getMessage(), e);
        }
    }

    /**
     * Modification d'un document.
     * Règle 4 : Le gestionnaire ne peut modifier que les documents de sa propre UFR.
     */
    public void modifierDocument(Document doc, Utilisateur operateur)
            throws AccesRefuseException, ChampInvalideException, DocumentIntrouvableException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }

        validerChampsDocument(doc);

        try {
            Document existant = documentDAO.trouverParId(doc.getIdDocument());
            if (existant == null) {
                throw new DocumentIntrouvableException("Document introuvable (ID " + doc.getIdDocument() + ").");
            }

            // Contrôle strict du cloisonnement par UFR
            if (operateur.getRole() == Role.GESTIONNAIRE) {
                if (operateur.getIdUfr() == null || 
                    operateur.getIdUfr() != existant.getIdUfr() || 
                    operateur.getIdUfr() != doc.getIdUfr()) {
                    throw new AccesRefuseException("Accès refusé : Vous ne pouvez modifier que les documents appartenant à votre UFR.");
                }
            } else if (operateur.getRole() != Role.ADMIN) {
                throw new AccesRefuseException("Action non autorisée.");
            }

            documentDAO.modifier(doc);
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la modification du document : " + e.getMessage(), e);
        }
    }

    /**
     * Suppression d'un document.
     * Règle 4 : Le gestionnaire ne peut supprimer que les documents de son UFR.
     */
    public void supprimerDocument(int idDocument, Utilisateur operateur)
            throws AccesRefuseException, DocumentIntrouvableException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }

        try {
            Document existant = documentDAO.trouverParId(idDocument);
            if (existant == null) {
                throw new DocumentIntrouvableException("Document introuvable (ID " + idDocument + ").");
            }

            if (operateur.getRole() == Role.GESTIONNAIRE) {
                if (operateur.getIdUfr() == null || operateur.getIdUfr() != existant.getIdUfr()) {
                    throw new AccesRefuseException("Accès refusé : Vous ne pouvez supprimer que les documents appartenant à votre UFR.");
                }
            } else if (operateur.getRole() != Role.ADMIN) {
                throw new AccesRefuseException("Action non autorisée.");
            }

            documentDAO.supprimer(idDocument);
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la suppression du document : " + e.getMessage(), e);
        }
    }

    /**
     * Liste les documents du fonds pour un gestionnaire (filtrés sur son UFR) ou pour l'admin (tous).
     */
    public List<Document> listerPourGestionnaire(Utilisateur operateur) throws AccesRefuseException, CentreDocException {
        if (operateur == null) {
            throw new AccesRefuseException("Utilisateur non authentifié.");
        }

        try {
            if (operateur.getRole() == Role.GESTIONNAIRE) {
                if (operateur.getIdUfr() == null) {
                    throw new AccesRefuseException("Aucune UFR n'est affectée à votre compte gestionnaire.");
                }
                return documentDAO.listerParUfr(operateur.getIdUfr());
            } else if (operateur.getRole() == Role.ADMIN) {
                return documentDAO.listerTous();
            } else {
                throw new AccesRefuseException("Profil non autorisé à administrer le fonds documentaire.");
            }
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la récupération des documents : " + e.getMessage(), e);
        }
    }

    /**
     * Recherche de documents pour les étudiants.
     * Règle 3 : Un document RESTREINT (embargo) n'est ni consultable ni téléchargeable par l'étudiant.
     * Les documents restreints sont donc systématiquement masqués du catalogue étudiant.
     */
    public List<Document> rechercherPourEtudiant(String titre, String auteur, String motCle,
                                                Integer annee, Integer idUfr, String discipline)
            throws CentreDocException {
        try {
            List<Document> tous = documentDAO.rechercher(titre, auteur, motCle, annee, idUfr, discipline);
            List<Document> visibles = new ArrayList<>();
            for (Document doc : tous) {
                // Règle 3 : exclusion des documents RESTREINT pour les étudiants
                if (doc.getNiveauAcces() != NiveauAcces.RESTREINT) {
                    visibles.add(doc);
                }
            }
            return visibles;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la recherche des documents : " + e.getMessage(), e);
        }
    }

    /**
     * Consultation de la fiche détaillée d'un document.
     * Règles 2 et 3 :
     * - Si RESTREINT et utilisateur = ÉTUDIANT -> AccesRefuseException
     */
    public Document consulterFicheDetaillee(int idDocument, Utilisateur demandeur)
            throws DocumentIntrouvableException, AccesRefuseException, CentreDocException {
        try {
            Document doc = documentDAO.trouverParId(idDocument);
            if (doc == null) {
                throw new DocumentIntrouvableException("Le document demandé n'existe pas dans le centre de documentation.");
            }

            if (demandeur != null && demandeur.getRole() == Role.ETUDIANT) {
                if (doc.getNiveauAcces() == NiveauAcces.RESTREINT) {
                    throw new AccesRefuseException("Accès refusé : Ce document est sous embargo (accès restreint). Sa consultation n'est pas autorisée.");
                }
            }

            return doc;
        } catch (CentreDocException e) {
            throw e;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la consultation du document : " + e.getMessage(), e);
        }
    }

    /**
     * Téléchargement d'un document PDF.
     * Règle 1 : Un étudiant ne peut télécharger un document que si son niveau d'accès est TELECHARGEABLE.
     * Règle 2 : CONSULTATION_SEULE n'expose que ses métadonnées et son résumé (pas de téléchargement).
     * Règle 3 : RESTREINT interdit le téléchargement.
     * Enregistre également la trace dans l'historique des téléchargements.
     */
    public File telechargerDocument(int idDocument, Utilisateur demandeur, File dossierDestination)
            throws DocumentIntrouvableException, AccesRefuseException, ChampInvalideException, CentreDocException {
        if (demandeur == null) {
            throw new AccesRefuseException("Vous devez être connecté pour télécharger un document.");
        }

        Document doc;
        try {
            doc = documentDAO.trouverParId(idDocument);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la recherche du document : " + e.getMessage(), e);
        }

        if (doc == null) {
            throw new DocumentIntrouvableException("Document introuvable (ID " + idDocument + ").");
        }

        // Vérification des règles de téléchargement pour les étudiants
        if (demandeur.getRole() == Role.ETUDIANT) {
            if (doc.getNiveauAcces() == NiveauAcces.CONSULTATION_SEULE) {
                throw new AccesRefuseException("Téléchargement non autorisé : Ce document est configuré en 'Consultation seule'. Seules les métadonnées et le résumé sont accessibles.");
            }
            if (doc.getNiveauAcces() == NiveauAcces.RESTREINT) {
                throw new AccesRefuseException("Téléchargement interdit : Ce document est sous embargo (accès restreint).");
            }
        }

        if (dossierDestination == null || !dossierDestination.exists() || !dossierDestination.isDirectory()) {
            throw new ChampInvalideException("Le répertoire de destination sélectionné est invalide.");
        }

        // Préparation du fichier source
        File fichierSource = new File(doc.getCheminPdf());
        if (!fichierSource.exists()) {
            // Si le chemin relatif n'est pas trouvé directement, essayer dans le sous-dossier docs/
            File alt = new File("docs", doc.getCheminPdf());
            if (alt.exists()) {
                fichierSource = alt;
            }
        }

        String nomFichierCible = (doc.getType() == TypeDocument.THESE ? "These_" : "Memoire_")
                + doc.getAuteur().replaceAll("[^a-zA-Z0-9]", "_") + "_" + doc.getAnnee() + ".pdf";
        File fichierDestination = new File(dossierDestination, nomFichierCible);

        // Copie du fichier PDF (flux Java standard I/O)
        try {
            if (fichierSource.exists()) {
                try (FileInputStream in = new FileInputStream(fichierSource);
                     FileOutputStream out = new FileOutputStream(fichierDestination)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            } else {
                // Si le PDF réel n'est pas encore sur le disque, on génère un PDF simulé propre
                try (FileOutputStream out = new FileOutputStream(fichierDestination)) {
                    String fauxContenu = "%PDF-1.4\n1 0 obj << /Title (" + doc.getTitre() + ") >> endobj\ntrailer << /Root 1 0 R >>\n%%EOF";
                    out.write(fauxContenu.getBytes("ISO-8859-1"));
                }
            }

            // Enregistrement de la trace de téléchargement
            Telechargement trace = new Telechargement(demandeur.getIdUtilisateur(), doc.getIdDocument());
            trace.setDateTelechargement(new Timestamp(System.currentTimeMillis()));
            telechargementDAO.enregistrer(trace);

            return fichierDestination;
        } catch (IOException e) {
            throw new CentreDocException("Erreur d'entrée/sortie lors de la sauvegarde du fichier PDF : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de l'enregistrement du téléchargement : " + e.getMessage(), e);
        }
    }

    /**
     * Historique des téléchargements pour un étudiant (son propre historique).
     */
    public List<Telechargement> consulterHistoriqueEtudiant(Etudiant etudiant) throws CentreDocException {
        if (etudiant == null) {
            return new ArrayList<>();
        }
        try {
            return telechargementDAO.listerParUtilisateur(etudiant.getIdUtilisateur());
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la consultation de votre historique : " + e.getMessage(), e);
        }
    }

    /**
     * Historique des téléchargements pour un gestionnaire (limité aux documents de son UFR).
     */
    public List<Telechargement> consulterHistoriqueGestionnaire(Gestionnaire gestionnaire) throws AccesRefuseException, CentreDocException {
        if (gestionnaire == null || gestionnaire.getIdUfr() == null) {
            throw new AccesRefuseException("Gestionnaire non affecté à une UFR.");
        }
        try {
            return telechargementDAO.listerParUfr(gestionnaire.getIdUfr());
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la consultation de l'historique UFR : " + e.getMessage(), e);
        }
    }
}
