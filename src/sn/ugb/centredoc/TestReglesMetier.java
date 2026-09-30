package sn.ugb.centredoc;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.AuthService;
import sn.ugb.centredoc.service.DemandeAccesService;
import sn.ugb.centredoc.service.DocumentService;
import sn.ugb.centredoc.service.NotificationService;
import sn.ugb.centredoc.service.UtilisateurService;

import java.io.File;
import java.util.List;

/**
 * Classe de validation automatique des règles métier strictes du sujet de POO2.
 * Permet de prouver formellement le bon fonctionnement de toutes les exigences.
 */
public class TestReglesMetier {

    private static int testsReussis = 0;
    private static int testsTotal = 0;

    private static void affirmer(boolean condition, String description) {
        testsTotal++;
        if (condition) {
            System.out.println("  [OK] " + description);
            testsReussis++;
        } else {
            System.err.println("  [ECHEC] " + description);
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("TESTS DE VALIDATION DES RÈGLES MÉTIER ACADÉMIQUES (POO2 - UGB)");
        System.out.println("===============================================================\n");

        AuthService authService = new AuthService();
        DocumentService docService = new DocumentService();
        UtilisateurService userService = new UtilisateurService();
        DemandeAccesService demandeService = new DemandeAccesService();

        // -------------------------------------------------------------
        // TEST 1 : Format de l'adresse email (@ugb.edu.sn)
        // -------------------------------------------------------------
        System.out.println("1. Test validation du format email (@ugb.edu.sn) :");
        try {
            AuthService.validerEmailUGB("etudiant@gmail.com");
            affirmer(false, "L'email hors @ugb.edu.sn aurait dû être rejeté.");
        } catch (ChampInvalideException e) {
            affirmer(true, "Email hors @ugb.edu.sn correctement rejeté avec ChampInvalideException : " + e.getMessage());
        }

        try {
            AuthService.validerEmailUGB("moussa.diop@ugb.edu.sn");
            affirmer(true, "Email valide @ugb.edu.sn accepté.");
        } catch (ChampInvalideException e) {
            affirmer(false, "L'email valide a été rejeté à tort.");
        }

        // -------------------------------------------------------------
        // TEST 2 : Authentification Étudiant (sans mot de passe)
        // -------------------------------------------------------------
        System.out.println("\n2. Test authentification Étudiant (sans mot de passe) :");
        Etudiant etuConnecte = null;
        try {
            etuConnecte = authService.authentifierEtudiant("Ibrahima", "SOW", "ibrahima.sow@ugb.edu.sn", "P28 0145");
            affirmer(etuConnecte != null && etuConnecte.getRole() == Role.ETUDIANT,
                    "Étudiant Ibrahima SOW authentifié avec succès sans mot de passe.");
        } catch (Exception e) {
            affirmer(false, "Échec d'authentification étudiant valide : " + e.getMessage());
        }

        try {
            authService.authentifierEtudiant("Faux", "Nom", "faux@ugb.edu.sn", "CODE-INCONNU");
            affirmer(false, "L'étudiant inexistant aurait dû lever une AuthentificationException.");
        } catch (AuthentificationException e) {
            affirmer(true, "Authentification refusée pour étudiant inexistant (AuthentificationException levée).");
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // TEST 3 : Cloisonnement UFR Gestionnaire (Règle 4)
        // -------------------------------------------------------------
        System.out.println("\n3. Test cloisonnement UFR du gestionnaire (Règle 4) :");
        Gestionnaire gestSAT = new Gestionnaire(2, "DIAKHAME", "Moussa", "moussa.diakhame@ugb.edu.sn", "pass123", 1); // UFR 1 = SAT
        gestSAT.setNomUfr("Sciences Appliquées et de Technologie");

        Document docUFR3 = new Document(
                "Mémoire d'économie", "Auteur Test", "Encadrant Test", 2024,
                TypeDocument.MEMOIRE, 3, "Économie", "Résumé...", "mots", "test.pdf", NiveauAcces.TELECHARGEABLE
        );

        try {
            docService.ajouterDocument(docUFR3, gestSAT);
            affirmer(false, "Le gestionnaire SAT aurait dû être bloqué pour ajouter un document en UFR SEG (3).");
        } catch (AccesRefuseException e) {
            affirmer(true, "Gestionnaire bloqué lors de l'ajout hors UFR avec AccesRefuseException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // TEST 4 : Recherche & Embargo RESTREINT pour Étudiant (Règle 3)
        // -------------------------------------------------------------
        System.out.println("\n4. Test masquage des documents RESTREINT pour l'étudiant (Règle 3) :");
        try {
            List<Document> catalogueEtu = docService.rechercherPourEtudiant(null, null, null, null, null, null);
            boolean contientRestreint = false;
            for (Document d : catalogueEtu) {
                if (d.getNiveauAcces() == NiveauAcces.RESTREINT) {
                    contientRestreint = true;
                    break;
                }
            }
            affirmer(!contientRestreint, "Aucun document RESTREINT (embargo) n'apparaît dans le catalogue étudiant.");
        } catch (Exception e) {
            affirmer(false, "Erreur recherche étudiant : " + e.getMessage());
        }

        try {
            docService.consulterFicheDetaillee(5, etuConnecte); // Document 5 = RESTREINT
            affirmer(false, "La consultation d'un document RESTREINT par un étudiant aurait dû être interdite.");
        } catch (AccesRefuseException e) {
            affirmer(true, "Consultation d'un document RESTREINT refusée avec AccesRefuseException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // TEST 5 : Téléchargement conditionné (Règles 1 et 2)
        // -------------------------------------------------------------
        System.out.println("\n5. Test téléchargement selon niveau d'accès (Règles 1 et 2) :");
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // Document 3 = CONSULTATION_SEULE
        try {
            docService.telechargerDocument(3, etuConnecte, tempDir);
            affirmer(false, "Le téléchargement d'un document CONSULTATION_SEULE aurait dû être refusé.");
        } catch (AccesRefuseException e) {
            affirmer(true, "Téléchargement d'un document CONSULTATION_SEULE bloqué avec AccesRefuseException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // Document 1 = TELECHARGEABLE
        try {
            File resultat = docService.telechargerDocument(1, etuConnecte, tempDir);
            affirmer(resultat != null && resultat.exists(), "Téléchargement d'un document TELECHARGEABLE réussi : " + resultat.getName());
        } catch (Exception e) {
            affirmer(false, "Échec téléchargement document autorisé : " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 6 : Unicité des identifiants (DoublonException)
        // -------------------------------------------------------------
        System.out.println("\n6. Test unicité des identifiants (DoublonException) :");
        Administrateur admin = new Administrateur(1, "NDIAYE", "Ibrahima", "admin@ugb.edu.sn", "admin123");
        try {
            userService.creerGestionnaire("Test", "Nom", "moussa.diakhame@ugb.edu.sn", "pass", 1, admin);
            affirmer(false, "Création avec email existant aurait dû lever une DoublonException.");
        } catch (DoublonException e) {
            affirmer(true, "Création avec email dupliqué refusée avec DoublonException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // TEST 7 : Enrôlement d'étudiants par un gestionnaire
        // -------------------------------------------------------------
        System.out.println("\n7. Test enrôlement d'étudiant par un gestionnaire :");
        try {
            Etudiant nouvEtu = userService.creerEtudiant(
                    "DIAGNE", "Aissatou", "aissatou.diagne@ugb.edu.sn", "P30 0011", 1, gestSAT
            );
            affirmer(nouvEtu != null && "P30 0011".equals(nouvEtu.getCodeEtudiant()),
                    "Gestionnaire SAT a enrôlé avec succès l'étudiante Aïssatou DIAGNE dans son UFR (SAT).");

            Etudiant etuConnecteNouv = authService.authentifierEtudiant("Aissatou", "DIAGNE", "aissatou.diagne@ugb.edu.sn", "P30 0011");
            affirmer(etuConnecteNouv != null,
                    "Étudiante nouvellement enrôlée s'authentifie avec succès sans mot de passe.");
        } catch (Exception e) {
            affirmer(false, "Échec enrôlement étudiant par gestionnaire : " + e.getMessage());
        }

        try {
            Etudiant etuAutreUfr = userService.creerEtudiant(
                    "KANE", "Oumar", "oumar.kane@ugb.edu.sn", "P30 0022", 3, gestSAT
            );
            affirmer(etuAutreUfr != null && etuAutreUfr.getIdUfr() == 3,
                    "Gestionnaire SAT peut enrôler un étudiant pour une autre UFR (UFR SEG - 3) après avertissement/confirmation.");
        } catch (Exception e) {
            affirmer(false, "Échec enrôlement autre UFR : " + e.getMessage());
        }

        try {
            Etudiant imposteur = new Etudiant(99, "HACKER", "Test", "hacker@ugb.edu.sn", "P99 9999", 1);
            userService.creerEtudiant("FAIL", "User", "fail.user@ugb.edu.sn", "P30 9999", 1, imposteur);
            affirmer(false, "Un étudiant ne doit pas pouvoir enrôler un autre étudiant.");
        } catch (AccesRefuseException e) {
            affirmer(true, "Tentative d'enrôlement par un non-gestionnaire/non-admin bloquée avec AccesRefuseException.");
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // TEST 8 : Demande d'accès aux documents sous embargo (Bonus 3.3)
        // -------------------------------------------------------------
        System.out.println("\n8. Test demande d'accès aux documents sous embargo (Bonus) :");
        DemandeAcces demandeCreee = null;
        try {
            demandeCreee = demandeService.creerDemande(etuConnecte,
                    "Vulnérabilités cryptographiques des protocoles de communication de l'Internet des Objets (IoT)",
                    "Travaux de recherche encadrés nécessitant la consultation du mémoire sous embargo.");
            affirmer(demandeCreee != null && demandeCreee.isEnAttente(),
                    "Demande d'accès soumise avec succès pour le document sous embargo (statut EN_ATTENTE).");
        } catch (Exception e) {
            affirmer(false, "Échec de la soumission de la demande d'accès : " + e.getMessage());
        }

        try {
            demandeService.creerDemande(etuConnecte,
                    "Vulnérabilités cryptographiques des protocoles de communication de l'Internet des Objets (IoT)",
                    "Seconde demande de test.");
            affirmer(false, "Une seconde demande pour le même document aurait dû lever une DoublonException.");
        } catch (DoublonException e) {
            affirmer(true, "Doublon de demande refusé avec DoublonException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // La consultation reste refusée tant que la demande n'est pas acceptée (Règle 3)
        try {
            docService.consulterFicheDetaillee(5, etuConnecte);
            affirmer(false, "La consultation aurait dû rester bloquée avant acceptation de la demande.");
        } catch (AccesRefuseException e) {
            affirmer(true, "Consultation du document sous embargo toujours refusée avant acceptation de la demande.");
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // Acceptation par le gestionnaire SAT (Règle 4 : le document 5 appartient à l'UFR SAT)
        try {
            demandeService.traiterDemande(demandeCreee.getIdDemande(), true, gestSAT);
            affirmer(true, "Gestionnaire SAT a accepté la demande d'accès (document de son UFR).");
        } catch (Exception e) {
            affirmer(false, "Échec de l'acceptation de la demande : " + e.getMessage());
        }

        // La consultation est désormais autorisée (Bonus 3.3)
        try {
            Document docConsulte = docService.consulterFicheDetaillee(5, etuConnecte);
            affirmer(docConsulte != null && docConsulte.isRestreint(),
                    "Consultation du document sous embargo autorisée après acceptation de la demande.");
        } catch (Exception e) {
            affirmer(false, "Échec de la consultation après acceptation : " + e.getMessage());
        }

        // Le téléchargement reste formellement bloqué (Règle 1 préservée)
        try {
            docService.telechargerDocument(5, etuConnecte, tempDir);
            affirmer(false, "Le téléchargement d'un document RESTREINT aurait dû rester bloqué même après acceptation.");
        } catch (AccesRefuseException e) {
            affirmer(true, "Téléchargement du document sous embargo toujours bloqué après acceptation (Règle 1 préservée) : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // Une demande sur un document non restreint est refusée (inutile : document déjà accessible)
        try {
            demandeService.creerDemande(etuConnecte,
                    "Optimisation des requêtes distribuées dans les bases de données réparties", "Test.");
            affirmer(false, "Une demande sur un document non restreint aurait dû être refusée.");
        } catch (ChampInvalideException e) {
            affirmer(true, "Demande sur un document non sous embargo correctement refusée avec ChampInvalideException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // Nettoyage : suppression de la demande créée (garantit la ré-exécution de la suite en mode JDBC)
        try {
            if (demandeCreee != null) {
                DAOFactory.getDemandeAccesDAO().supprimer(demandeCreee.getIdDemande());
            }
        } catch (Exception ignored) {
        }

        // -------------------------------------------------------------
        // TEST 9 : Notifications des nouvelles publications (Bonus 3.3)
        // -------------------------------------------------------------
        System.out.println("\n9. Test notifications des nouvelles publications (Bonus) :");
        try {
            Document nouveauDoc = new Document(
                    "Mémoire de test notification automatique", "Auteur Test", "Encadrant Test", 2026,
                    TypeDocument.MEMOIRE, 1, "Informatique", "Résumé de test pour la notification.",
                    "test, notification", "test_notification.pdf", NiveauAcces.TELECHARGEABLE
            );
            docService.ajouterDocument(nouveauDoc, gestSAT);

            boolean contientNouveaute = false;
            for (Document d : NotificationService.getInstance().listerNouveautes(1)) {
                if (d.getIdDocument() == nouveauDoc.getIdDocument()) {
                    contientNouveaute = true;
                    break;
                }
            }
            affirmer(contientNouveaute,
                    "Le nouveau document publié dans l'UFR SAT apparaît dans les notifications étudiant.");

            // Nettoyage : suppression du document de test
            docService.supprimerDocument(nouveauDoc.getIdDocument(), gestSAT);
        } catch (Exception e) {
            affirmer(false, "Erreur test notifications : " + e.getMessage());
        }

        // -------------------------------------------------------------
        // BILAN FINAL
        // -------------------------------------------------------------
        System.out.println("\n===============================================================");
        System.out.println("RÉSULTAT : " + testsReussis + " / " + testsTotal + " tests réussis.");
        System.out.println("===============================================================");
    }
}
