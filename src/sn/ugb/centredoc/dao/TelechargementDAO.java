package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.model.Telechargement;
import java.util.List;
import java.util.Map;

/**
 * Interface DAO pour la traçabilité des téléchargements et les statistiques.
 * Utilise List et Map conformément aux collections étudiées en POO2.
 */
public interface TelechargementDAO {
    void enregistrer(Telechargement t) throws Exception;
    List<Telechargement> listerTous() throws Exception;
    List<Telechargement> listerParUtilisateur(int idUtilisateur) throws Exception;
    List<Telechargement> listerParDocument(int idDocument) throws Exception;
    List<Telechargement> listerParUfr(int idUfr) throws Exception;
    
    // Méthodes pour le module de statistiques (retournent des Map de java.util)
    Map<String, Integer> getStatsTopDocuments(int limit) throws Exception;
    Map<String, Integer> getStatsParUfr() throws Exception;
    Map<String, Integer> getStatsParDiscipline() throws Exception;
    Map<Integer, Integer> getStatsParAnnee() throws Exception;
}
