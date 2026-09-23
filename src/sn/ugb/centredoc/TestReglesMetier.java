package sn.ugb.centredoc;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.AuthService;
import sn.ugb.centredoc.service.DocumentService;
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
        Administrateur admin = new Administrateur(1, "DIOP", "Amadou", "admin@ugb.edu.sn", "admin123");
        try {
            userService.creerGestionnaire("Test", "Nom", "moussa.diakhame@ugb.edu.sn", "pass", 1, admin);
            affirmer(false, "Création avec email existant aurait dû lever une DoublonException.");
        } catch (DoublonException e) {
            affirmer(true, "Création avec email dupliqué refusée avec DoublonException : " + e.getMessage());
        } catch (Exception e) {
            affirmer(false, "Mauvaise exception levée : " + e.getClass().getName());
        }

        // -------------------------------------------------------------
        // BILAN FINAL
        // -------------------------------------------------------------
        System.out.println("\n===============================================================");
        System.out.println("RÉSULTAT : " + testsReussis + " / " + testsTotal + " tests réussis.");
        System.out.println("===============================================================");
    }
}
