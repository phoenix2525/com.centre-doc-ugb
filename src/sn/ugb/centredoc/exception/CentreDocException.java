package sn.ugb.centredoc.exception;

/**
 * Exception de base pour toutes les exceptions métier du Centre de Documentation UGB.
 * Hérite de java.lang.Exception selon les directives pédagogiques du cours POO2.
 */
public class CentreDocException extends Exception {
    
    public CentreDocException(String message) {
        super(message);
    }
    
    public CentreDocException(String message, Throwable cause) {
        super(message, cause);
    }
}
