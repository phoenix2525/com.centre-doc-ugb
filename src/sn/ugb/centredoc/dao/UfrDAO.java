package sn.ugb.centredoc.dao;

import sn.ugb.centredoc.model.Ufr;
import java.util.List;

/**
 * Interface DAO pour la gestion des UFR.
 * Utilise java.util.List conformément aux directives académiques.
 */
public interface UfrDAO {
    List<Ufr> listerTous() throws Exception;
    Ufr trouverParId(int idUfr) throws Exception;
    Ufr trouverParCode(String code) throws Exception;
    void ajouter(Ufr ufr) throws Exception;
}
