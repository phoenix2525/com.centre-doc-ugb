package sn.ugb.centredoc.exception;

/**
 * Exception levée lorsqu'un utilisateur tente d'effectuer une action non autorisée :
 * - Étudiant tentant de télécharger un document non téléchargeable (CONSULTATION_SEULE ou RESTREINT).
 * - Étudiant tentant d'accéder à un document sous embargo (RESTREINT).
 * - Gestionnaire tentant d'ajouter, modifier ou supprimer un document d'une autre UFR.
 */
public class AccesRefuseException extends CentreDocException {
    
    public AccesRefuseException(String message) {
        super(message);
    }
}
