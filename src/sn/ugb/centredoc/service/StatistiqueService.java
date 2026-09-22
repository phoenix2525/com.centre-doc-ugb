package sn.ugb.centredoc.service;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.DocumentDAO;
import sn.ugb.centredoc.dao.TelechargementDAO;
import sn.ugb.centredoc.dao.UtilisateurDAO;
import sn.ugb.centredoc.exception.CentreDocException;
import sn.ugb.centredoc.model.Document;
import sn.ugb.centredoc.model.NiveauAcces;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Service pour le Module optionnel 3.2 (Statistiques et Rapports) et 3.3 (Export de données).
 * Exploite largement java.util.Map, java.util.List et les collections du cours.
 */
public class StatistiqueService {
    private final DocumentDAO documentDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final TelechargementDAO telechargementDAO;

    public StatistiqueService() {
        this.documentDAO = DAOFactory.getDocumentDAO();
        this.utilisateurDAO = DAOFactory.getUtilisateurDAO();
        this.telechargementDAO = DAOFactory.getTelechargementDAO();
    }

    public Map<String, Integer> getStatistiquesGlobales() throws CentreDocException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        try {
            int nbDocs = documentDAO.listerTous().size();
            int nbUsers = utilisateurDAO.listerTous().size();
            int nbTelechargements = telechargementDAO.listerTous().size();

            stats.put("Total Documents", nbDocs);
            stats.put("Total Utilisateurs", nbUsers);
            stats.put("Total Téléchargements", nbTelechargements);
            return stats;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors du calcul des statistiques globales : " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getDocumentsLesPlusTelecharges(int limit) throws CentreDocException {
        try {
            return telechargementDAO.getStatsTopDocuments(limit);
        } catch (Exception e) {
            throw new CentreDocException("Erreur stats top documents : " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getActiviteParUfr() throws CentreDocException {
        try {
            return telechargementDAO.getStatsParUfr();
        } catch (Exception e) {
            throw new CentreDocException("Erreur stats activité par UFR : " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getRepartitionParDiscipline() throws CentreDocException {
        try {
            return telechargementDAO.getStatsParDiscipline();
        } catch (Exception e) {
            throw new CentreDocException("Erreur stats disciplines : " + e.getMessage(), e);
        }
    }

    public Map<Integer, Integer> getRepartitionParAnnee() throws CentreDocException {
        try {
            return telechargementDAO.getStatsParAnnee();
        } catch (Exception e) {
            throw new CentreDocException("Erreur stats années : " + e.getMessage(), e);
        }
    }

    public List<Document> getDocumentsRestreints() throws CentreDocException {
        try {
            List<Document> tous = documentDAO.listerTous();
            List<Document> restreints = new ArrayList<>();
            for (Document d : tous) {
                if (d.getNiveauAcces() == NiveauAcces.RESTREINT) {
                    restreints.add(d);
                }
            }
            return restreints;
        } catch (Exception e) {
            throw new CentreDocException("Erreur lors de la liste des documents restreints : " + e.getMessage(), e);
        }
    }

    /**
     * Export des données et statistiques au format CSV (Module 3.3 Bonus).
     */
    public File exporterStatistiquesCSV(File fichierCible) throws CentreDocException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichierCible))) {
            writer.println("=== RAPPORT STATISTIQUE - CENTRE DE DOCUMENTATION UGB ===");
            writer.println();

            writer.println("1. STATISTIQUES GLOBALES");
            writer.println("Indicateur,Valeur");
            for (Map.Entry<String, Integer> entry : getStatistiquesGlobales().entrySet()) {
                writer.println("\"" + entry.getKey() + "\"," + entry.getValue());
            }
            writer.println();

            writer.println("2. DOCUMENTS LES PLUS TELECHARGES (TOP 5)");
            writer.println("Titre du document,Nombre de téléchargements");
            for (Map.Entry<String, Integer> entry : getDocumentsLesPlusTelecharges(5).entrySet()) {
                writer.println("\"" + entry.getKey().replace("\"", "\"\"") + "\"," + entry.getValue());
            }
            writer.println();

            writer.println("3. TELECHARGEMENTS PAR UFR");
            writer.println("UFR,Nombre de téléchargements");
            for (Map.Entry<String, Integer> entry : getActiviteParUfr().entrySet()) {
                writer.println("\"" + entry.getKey() + "\"," + entry.getValue());
            }
            writer.println();

            writer.println("4. FONDS DOCUMENTAIRE PAR DISCIPLINE");
            writer.println("Discipline,Nombre de documents");
            for (Map.Entry<String, Integer> entry : getRepartitionParDiscipline().entrySet()) {
                writer.println("\"" + entry.getKey() + "\"," + entry.getValue());
            }
            writer.println();

            writer.println("5. DOCUMENTS EN ACCES RESTREINT (EMBARGO)");
            writer.println("ID,Titre,Auteur,Annee,UFR");
            for (Document doc : getDocumentsRestreints()) {
                writer.println(doc.getIdDocument() + ",\"" + doc.getTitre().replace("\"", "\"\"") + "\",\"" +
                        doc.getAuteur() + "\"," + doc.getAnnee() + ",\"" + doc.getNomUfr() + "\"");
            }

            return fichierCible;
        } catch (IOException e) {
            throw new CentreDocException("Erreur d'écriture du rapport CSV : " + e.getMessage(), e);
        }
    }
}
