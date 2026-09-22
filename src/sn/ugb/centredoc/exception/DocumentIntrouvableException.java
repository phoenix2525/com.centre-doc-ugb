package sn.ugb.centredoc.exception;

/**
 * Exception levée lorsqu'un document demandé n'existe pas en base de données
 * ou n'est pas accessible aux critères demandés.
 */
public class DocumentIntrouvableException extends CentreDocException {
    
    public DocumentIntrouvableException(String message) {
        super(message);
    }
}
