package sn.ugb.centredoc.exception;

/**
 * Exception levée lors d'une tentative de création d'une entité dont un champ unique
 * (email, code étudiant, identifiant) existe déjà.
 */
public class DoublonException extends CentreDocException {
    
    public DoublonException(String message) {
        super(message);
    }
}
