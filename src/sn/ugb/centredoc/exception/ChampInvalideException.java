package sn.ugb.centredoc.exception;

/**
 * Exception levée lorsqu'un champ de formulaire ne respecte pas les règles de validation métier :
 * - Email ne se terminant pas par @ugb.edu.sn
 * - Année de document incohérente
 * - Champ obligatoire non renseigné ou vide
 */
public class ChampInvalideException extends CentreDocException {
    
    public ChampInvalideException(String message) {
        super(message);
    }
}
