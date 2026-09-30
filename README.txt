================================================================================
 UNIVERSITE GASTON BERGER (UGB) - SAINT-LOUIS
 UFR DE SCIENCES APPLIQUEES ET DE TECHNOLOGIE (UFR SAT)
 LICENCE 3 INFORMATIQUE / MIAGE - ANNEE UNIVERSITAIRE 2025-2026
 UE : PROGRAMMATION ORIENTEE OBJET 2 (POO2)
 ENSEIGNANTS : Dr. Fatou KAMARA-SANGARE / M. Moussa L. DIAKHAME
================================================================================
 PROJET : SYSTEME D'INFORMATION DES CENTRES DE DOCUMENTATION DE L'UGB
          GESTION DES THESES ET MEMOIRES
================================================================================

1. PRESENTATION DU PROJET
-------------------------
Cette application Java Swing a ete concue pour informatiser la gestion et la 
consultation des thèses et mémoires au sein des centres de documentation de 
l'Université Gaston Berger (UGB).

Elle implemente fidelement les trois acteurs du systeme :
- L'ADMINISTRATEUR : gestion globale des gestionnaires et rattachement aux UFR.
- LE GESTIONNAIRE DE CENTRE : enrichissement et organisation du fonds documentaire 
  de son UFR, definition des niveaux d'acces et consultation de l'activite.
- L'ETUDIANT : authentification sans mot de passe, recherche multicritere 
  dynamique, previsualisation des fiches et telechargement securise du PDF 
  selon les droits d'acces.

Des modules avances de statistiques graphiques (camemberts et histogrammes) 
et d'exportation au format CSV completent l'application.

Deux modules bonus completent le systeme :
- FORMULAIRE DE DEMANDE D'ACCES : l'etudiant peut soumettre une demande de 
  consultation pour un document sous embargo (RESTREINT) ; le gestionnaire du 
  centre l'accepte ou la refuse. L'acceptation autorise la consultation de la 
  fiche, le telechargement du PDF reste bloque.
- NOTIFICATIONS VISUELLES : l'etudiant est notifie des nouveaux documents 
  publies dans son UFR (panneau de nouveautes de la semaine).


2. ARCHITECTURE TECHNIQUE ET REGLES ACADEMIQUES
-----------------------------------------------
- Langage : Java 8 ou superieur (JDK 17/21 recommande).
- Interface graphique : Java Swing personnalise aux couleurs de l'UGB.
- Base de donnees : MySQL ('centre_doc') avec acces JDBC standard (PreparedStatement).
- Double mode d'execution : L'application fonctionne avec MySQL. Si MySQL n'est 
  pas allume sur votre machine, l'application bascule automatiquement et de 
  maniere transparente en memoire vive (Collections Java) pour garantir une 
  demonstration sans encombre.
- Collections Java obligatoires : Utilisation exclusive de List, ArrayList, 
  Map, HashMap et Set. Aucun tableau brut '[]' n'est utilise pour la manipulation 
  des entites.
- Exceptions metier personnalisees : Heritage direct de java.lang.Exception 
  (AuthentificationException, AccesRefuseException, DocumentIntrouvableException, 
  DoublonException, ChampInvalideException).


3. STRUCTURE DU DOSSIER 'Projet_JAVA'
-------------------------------------
- src/                     : Codes sources Java organises en couches (model, dao, service, ui, exception)
- lib/                     : Pilote JDBC MySQL (mysql-connector-j-9.0.0.jar)
- docs/                    : Echantillons de fichiers PDF pour les tests de telechargement
- bin/                     : Dossier des fichiers .class compiles
- centre_doc.sql           : Script DDL/DML de creation et de population de la BDD
                             (tables ufr, utilisateurs, documents, telechargements, demande_acces)
- centre_doc.jar           : Archive executable generee
- compile.bat              : Script Windows de compilation complete
- run.bat                  : Script Windows de lancement direct de l'application
- run_tests.bat            : Script Windows de validation des 22 regles metier et bonus
- sources.txt              : Liste des classes sources Java
- DIRECTIVES_ET_CONTINUATION.md : Dossier complet de transmission technique et academique
- README.txt               : Le present document


4. COMPTES DE DEMONSTRATION PRECONFIGURES
-----------------------------------------
A. ADMINISTRATEUR :
   - Email        : admin@ugb.edu.sn
   - Mot de passe : admin123

B. GESTIONNAIRES DE CENTRE DE DOCUMENTATION :
   - UFR SAT (Sciences Appliquees) : moussa.diakhame@ugb.edu.sn / pass123
   - UFR SEG (Sciences Eco & Gestion) : aminata.diallo@ugb.edu.sn / pass123
   - UFR SJP (Sciences Juridiques)    : cheikh.fall@ugb.edu.sn / pass123

C. ETUDIANTS (Connexion SANS mot de passe) :
   - Etudiant SAT :
     Prenom : Ibrahima | Nom : SOW | Email : ibrahima.sow@ugb.edu.sn | Code : P28 0145
   - Etudiante SEG :
     Prenom : Fatou | Nom : NDIAYE | Email : fatou.ndiaye@ugb.edu.sn | Code : P29 0089
   - Etudiant SJP :
     Prenom : Mamadou | Nom : BA | Email : mamadou.ba@ugb.edu.sn | Code : P27 0312


5. INSTRUCTIONS DE COMPILATION ET D'EXECUTION
--------------------------------------------
A. Compilation du projet :
   Double-cliquez sur 'compile.bat'
   OU tapez en invite de commande :
   compile.bat

B. Execution de la validation des regles metier :
   Double-cliquez sur 'run_tests.bat'
   OU tapez en invite de commande :
   run_tests.bat
   (Confirme 22 / 22 tests de validation des regles metier et bonus avec succes)

C. Lancement de l'application graphique :
   Double-cliquez sur 'run.bat'
   OU tapez en invite de commande :
   run.bat


6. CONFIGURATION DE LA BASE DE DONNEES MYSQL (OPTIONNELLE MAIS RECOMMANDEE)
---------------------------------------------------------------------------
1. Demarrer votre serveur MySQL local (via XAMPP, WAMP ou MySQL Server).
2. Creer la base et importer les donnees en important le fichier 'centre_doc.sql' 
   depuis phpMyAdmin ou via la ligne de commande :
   mysql -u root -p < centre_doc.sql
3. Dans l'application, l'ecran de connexion propose un bouton "⚙️ Configurer BDD" 
   permettant de verifier l'etat du serveur MySQL, de tester les identifiants ou 
   de reinitialiser la base directement.


7. LIEN VERS LA DEMONSTRATION VIDEO OBLIGATOIRE
-----------------------------------------------
Conformement au sujet officiel (remise du 05 Octobre 2026), une capture video 
d'ecran presentant les interfaces et le fonctionnement global est requise :
- Lien de la video (Google Drive / OneDrive) : [A COMPLETER AVANT L'ENVOI]
- Format recommande : MP4
================================================================================
