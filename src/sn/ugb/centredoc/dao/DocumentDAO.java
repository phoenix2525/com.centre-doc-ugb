package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.model.Document;
import java.util.List;

/**
 * Interface DAO pour la gestion du fonds documentaire (thèses et mémoires).
 * Manipulation exclusive via java.util.List.
 */
public interface DocumentDAO {
    List<Document> listerTous() throws Exception;
    List<Document> listerParUfr(int idUfr) throws Exception;
    List<Document> rechercher(String titre, String auteur, String motCle, Integer annee, Integer idUfr, String discipline) throws Exception;
    Document trouverParId(int idDocument) throws Exception;
    void ajouter(Document doc) throws Exception;
    void modifier(Document doc) throws Exception;
    void supprimer(int idDocument) throws Exception;
}
