package sn.ugb.centredoc.exception;

/**
 * Exception levée lors d'un échec d'authentification (email inconnu, mot de passe erroné,
 * ou informations d'étudiant invalides).
 */
public class AuthentificationException extends CentreDocException {
    
    public AuthentificationException(String message) {
        super(message);
    }
}
