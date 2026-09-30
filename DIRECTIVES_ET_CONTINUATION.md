# DOSSIER DE TRANSMISSION & DIRECTIVES DE PROJET — POO2 (L3 UGB)

> **DESTINATAIRE :** Tout agent IA ou développeur reprenant ce projet.  
> **OBJECTIF :** Permettre une continuation immédiate, autonome et ultra-efficace sans avoir à refaire l'exploration de l'historique ou du système de fichiers.  
> **STATUT DU DOSSIER :** Emplacement propre, validé et autonome : `C:\Users\ibrah\Desktop\Projet_JAVA`.

---

## 1. Contexte Académique et Cahier des Charges

* **Établissement :** Université Gaston Berger (UGB) de Saint-Louis, Sénégal.
* **UFR :** Sciences Appliquées et de Technologie (UFR SAT).
* **Filière :** Licence 3 Informatique / MIAGE (Année Universitaire 2025-2026).
* **Matière / UE :** Programmation Orientée Objet 2 (POO2).
* **Enseignants référents :** Dr. Fatou Kamara-Sangaré / M. Moussa L. Diakhame.
* **Projet officiel :** *Système d'Information des Centres de Documentation de l'UGB — Gestion des thèses et mémoires*.
* **Date limite de remise :** **05 Octobre 2026 à 23h00**.
* **Modalité :** Trinôme.
* **Modalités de remise officielles :**
  - **Objet du courriel :** `ProjetJAVAL3_INFO` ou `ProjetJAVAL3_MIAGE`.
  - **Archive :** Contenant les sources Java, la base de données SQL, le fichier `README.txt`, et la vidéo.
  - **Vidéo de démonstration OBLIGATOIRE :** Capture vidéo d'écran (format MP4 ou lien Google Drive / OneDrive explicite dans `README.txt`) montrant le démarrage, la navigation inter-interfaces, la gestion des utilisateurs et documents, la recherche et le téléchargement.

---

## 2. Contraintes Académiques & Techniques Non Négociables

### A. Collections Java Obligatoires (`java.util.*`)
* **Interdiction absolue :** Tableaux bruts (`[]` / `Array`) pour manipuler des données d'entités, variables dupliquées pour simuler des listes, structures manuelles non issues de l'API standard.
* **Obligation stricte :** Utilisation exclusive de `List`, `ArrayList`, `Map`, `HashMap`, `LinkedHashMap`, `Set`, `HashSet`.

### B. Hiérarchie d'Exceptions Personnalisées
* **Interdiction absolue :** `return null` ou simple `System.out.println` / message d'erreur silencieux en cas de violation d'une règle métier.
* **Obligation stricte :** Toute règle métier enfreinte doit déclencher une exception dédiée dérivant de `java.lang.Exception` (avec propagation par `throws` et capture par `try-catch` dans l'interface graphique Swing) :
  - `CentreDocException` : Racine de toutes les exceptions applicatives.
  - `AuthentificationException` : Identifiants incorrects ou utilisateur introuvable.
  - `AccesRefuseException` : Tentative de téléchargement d'un document non téléchargeable, consultation d'un document restreint/sous embargo, ou tentative d'un gestionnaire d'altérer un document hors de son UFR.
  - `DocumentIntrouvableException` : Identifiant de document inexistant en base ou mémoire.
  - `DoublonException` : Conflit d'unicité sur email ou code étudiant.
  - `ChampInvalideException` : Format d'email hors `@ugb.edu.sn`, année invalide, champs obligatoires vides.

### C. Base de Données & Accès JDBC
* **SGBD :** MySQL (nom de la base : `centre_doc`).
* **Accès données :** JDBC standard (`PreparedStatement`, `ResultSet`, `try-with-resources`). Pas d'ORM/Hibernate, pas de Spring Boot.
* **Architecture bi-mode transparente :** Grâce à `DAOFactory`, l'application teste automatiquement la présence de MySQL au démarrage. Si MySQL est absent ou éteint, elle bascule de manière transparente sur `DataStore` (mode mémoire basé sur les collections Java) sans faire planter l'application. Une boîte de dialogue de configuration intégrée dans `LoginFrame` permet également d'importer le script SQL ou de changer les paramètres de connexion à chaud.

---

## 3. Règles Métier Strictes du Sujet

1. **Règle 1 (Téléchargement étudiant) :** Un étudiant ne peut télécharger un document que si `niveau_acces == TELECHARGEABLE`.
2. **Règle 2 (Consultation seule) :** Un document `CONSULTATION_SEULE` n'expose que ses métadonnées et son résumé (le téléchargement du PDF est formellement bloqué).
3. **Règle 3 (Embargo / Restreint) :** Un document `RESTREINT` est strictement invisible et inexploitable pour l'étudiant (ni dans le catalogue, ni dans les recherches, ni en accès direct).
4. **Règle 4 (Cloisonnement Gestionnaire) :** Un gestionnaire ne gère (ajout, modification, suppression, consultation d'administration) **exclusivement** que les documents de **son UFR d'affectation**.
5. **Règle 5 (Format Email) :** Tout email doit obligatoirement être au format universitaire `@ugb.edu.sn`.
6. **Règle 6 (Unicité des identifiants) :** L'email est unique pour tous les utilisateurs. Le code étudiant est unique pour tous les étudiants.
7. **Règle 7 (Authentification Étudiant) :** L'étudiant s'authentifie **sans mot de passe** (Prénom, Nom, Email `@ugb.edu.sn` et Code Étudiant). L'administrateur et le gestionnaire s'authentifient par Email et Mot de passe.

---

## 4. Architecture et Organisation du Code Source

Structure complète sous `src/` :
```
src/
└── sn/
    └── ugb/
        └── centredoc/
            ├── Main.java                          # Point d'entrée de l'application Swing
            ├── TestReglesMetier.java              # Suite de validation automatisée (22 tests)
            ├── model/                             # Couche Entités & Enums (POJO conformes POO2)
            │   ├── Utilisateur.java               # Classe abstraite de base
            │   ├── Administrateur.java            # Profil Admin
            │   ├── Gestionnaire.java              # Profil Gestionnaire (avec idUfr)
            │   ├── Etudiant.java                  # Profil Étudiant (avec codeEtudiant)
            │   ├── Document.java                  # Entité Thèse / Mémoire
            │   ├── DemandeAcces.java              # Demande d'accès à un document sous embargo (Bonus)
            │   ├── Ufr.java                       # Entité UFR
            │   ├── Telechargement.java            # Historique d'accès
            │   ├── Role.java                      # Enum (ADMIN, GESTIONNAIRE, ETUDIANT)
            │   ├── TypeDocument.java              # Enum (THESE, MEMOIRE)
            │   ├── NiveauAcces.java               # Enum (TELECHARGEABLE, CONSULTATION_SEULE, RESTREINT)
            │   └── StatutDemande.java             # Enum (EN_ATTENTE, ACCEPTEE, REFUSEE)
            ├── exception/                         # Couche Exceptions métier
            │   ├── CentreDocException.java
            │   ├── AuthentificationException.java
            │   ├── AccesRefuseException.java
            │   ├── DocumentIntrouvableException.java
            │   ├── DoublonException.java
            │   └── ChampInvalideException.java
            ├── dao/                               # Interfaces DAO & Abstraction
            │   ├── DBConnection.java              # Connexion JDBC MySQL & exécuteur SQL
            │   ├── DAOFactory.java                # Bascule JDBC / Mémoire
            │   ├── UfrDAO.java
            │   ├── UtilisateurDAO.java
            │   ├── DocumentDAO.java
            │   ├── TelechargementDAO.java
            │   ├── DemandeAccesDAO.java           # Demandes d'accès aux documents sous embargo (Bonus)
            │   ├── jdbc/                          # Implémentations JDBC MySQL (PreparedStatement)
            │   │   ├── UfrDAOJdbc.java
            │   │   ├── UtilisateurDAOJdbc.java
            │   │   ├── DocumentDAOJdbc.java
            │   │   ├── TelechargementDAOJdbc.java
            │   │   └── DemandeAccesDAOJdbc.java
            │   └── memory/                        # Implémentations Collections en mémoire
            │       ├── DataStore.java             # Magasin de collections synchronisées
            │       ├── UfrDAOMemory.java
            │       ├── UtilisateurDAOMemory.java
            │       ├── DocumentDAOMemory.java
            │       ├── TelechargementDAOMemory.java
            │       └── DemandeAccesDAOMemory.java
            ├── service/                           # Couche Logique Métier & Contrôle des Règles
            │   ├── AuthService.java               # Flux d'authentification et validation email
            │   ├── DocumentService.java           # Gestion documentaire, téléchargement, droits
            │   ├── UtilisateurService.java        # Gestion des utilisateurs et affectations
            │   ├── StatistiqueService.java        # Stats globales, UFR, disciplines, export CSV
            │   ├── DemandeAccesService.java       # Soumission/traitement des demandes d'accès (Bonus)
            │   └── NotificationService.java       # Notifications visuelles des nouveautés UFR (Bonus)
            └── ui/                                # Couche Présentation Java Swing
                ├── UIUtils.java                   # Thème graphique UGB, polices, couleurs, badges
                ├── LoginFrame.java                # Écran d'accueil à onglets (Étudiant / Personnel)
                ├── AdminDashboard.java            # Module 1 : Gestion des gestionnaires & UFR
                ├── GestionnaireDashboard.java     # Module 2 : Gestion documentaire & Statistiques
                ├── EtudiantDashboard.java         # Module 3 : Recherche temps réel & téléchargement
                ├── DialogueEnrolementEtudiant.java       # Dialogue d'enrôlement d'un étudiant
                ├── DialogueAlerteEnrolementHorsUfr.java  # Dialogue de confirmation hors UFR
                ├── LogoUGB.java                   # Affichage du logo institutionnel UGB
                └── SimpleChartPanel.java          # Graphiques Swing vectoriels (barres et camemberts)
```

---

## 5. Comptes de Test Préconfigurés

Tous les comptes ci-dessous sont prêts à l'emploi aussi bien dans la base SQL que dans le magasin en mémoire :

| Rôle | Nom Complet | Identifiant / Email | Mot de passe | Remarques |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | Ibrahima NDIAYE | `admin@ugb.edu.sn` | `admin123` | Gère tous les comptes et UFR |
| **GESTIONNAIRE** | Moussa DIAKHAME | `moussa.diakhame@ugb.edu.sn` | `pass123` | Affecté à l'**UFR SAT** (UFR 1) |
| **GESTIONNAIRE** | Aminata DIALLO | `aminata.diallo@ugb.edu.sn` | `pass123` | Affectée à l'**UFR SEG** (UFR 3) |
| **GESTIONNAIRE** | Cheikh FALL | `cheikh.fall@ugb.edu.sn` | `pass123` | Affecté à l'**UFR SJP** (UFR 2) |
| **ÉTUDIANT** | Ibrahima SOW | `ibrahima.sow@ugb.edu.sn` | *Aucun* | Code : `P28 0145` (UFR SAT) |
| **ÉTUDIANT** | Fatou NDIAYE | `fatou.ndiaye@ugb.edu.sn` | *Aucun* | Code : `P29 0089` (UFR SEG) |
| **ÉTUDIANT** | Mamadou BA | `mamadou.ba@ugb.edu.sn` | *Aucun* | Code : `P27 0312` (UFR SJP) |

---

## 6. Commandes d'Exécution et Scripts

Depuis la racine `C:\Users\ibrah\Desktop\Projet_JAVA` :

### 1. Compilation
Double-cliquer sur `compile.bat` ou exécuter en console :
```cmd
compile.bat
```
*(Compile les 53 classes Java sous `bin/` et assemble le fichier JAR exécutable `centre_doc.jar`)*.

### 2. Validation Automatique des Règles Métier (22/22 Tests)
Double-cliquer sur `run_tests.bat` ou exécuter en console :
```cmd
run_tests.bat
```
Cette suite exécute `TestReglesMetier` et valide formellement :
- Le rejet des emails hors `@ugb.edu.sn` (`ChampInvalideException`).
- L'authentification étudiante sans mot de passe et le rejet d'inconnus (`AuthentificationException`).
- Le blocage d'un gestionnaire tentant d'ajouter un document hors de son UFR (`AccesRefuseException`).
- Le masquage total et l'interdiction de consultation des documents `RESTREINT` pour l'étudiant (`AccesRefuseException`).
- Le blocage du téléchargement pour `CONSULTATION_SEULE` et le succès pour `TELECHARGEABLE`.
- Le contrôle d'unicité avec déclenchement de `DoublonException`.
- L'enrôlement des étudiants par un gestionnaire dans son UFR et hors UFR après confirmation.
- L'interdiction formelle d'enrôlement par un utilisateur non autorisé.
- **[Bonus]** La soumission d'une demande d'accès à un document sous embargo, le doublon de demande (`DoublonException`), le refus de consultation avant acceptation, l'acceptation par le gestionnaire de l'UFR, l'autorisation de consultation après acceptation et le maintien du blocage du téléchargement (Règle 1 préservée).
- **[Bonus]** L'apparition d'un nouveau document publié dans les notifications visuelles de l'UFR.

### 3. Lancement de l'Application Graphique Swing
Double-cliquer sur `run.bat` ou exécuter :
```cmd
run.bat
```

---

## 7. État des Lieux des Fonctionnalités

### Modules Obligatoires (3.1)
- [x] **Module 1 : Gestion des Utilisateurs (Administrateur)**
  - Création, modification et suppression de comptes gestionnaires.
  - Affectation d'un gestionnaire à une UFR universitaire officielle.
  - Liste dynamique et recherche instantanée par nom ou adresse email.
  - Gestion des rôles différenciés.
- [x] **Module 2 : Gestion du Fonds Documentaire & Enrôlement Étudiant (Gestionnaire)**
  - **Personnalisation UFR éclatante de la bannière :** Dès la connexion, le tableau de bord met en évidence l'UFR du gestionnaire (ex: UFR SAT) via un badge institutionnel proéminent (fond sombre, bordure or UGB `OR_UGB`, 18pt), un titre explicite (`CENTRE DE DOCUMENTATION — UFR SAT`), et un sous-titre officiel.
  - **Module d'Enrôlement des Étudiants :** Possibilité directe pour un gestionnaire d'enrôler de nouveaux étudiants (Prénom, Nom, Email `@ugb.edu.sn`, Code Étudiant, UFR).
  - **Règle d'Avertissement UFR (Validation Contrôlée) :** Si le gestionnaire sélectionne pour l'étudiant une UFR différente de celle qu'il gère, une boîte de dialogue d'avertissement (`JOptionPane.WARNING_MESSAGE`) le notifie immédiatement et sollicite sa confirmation expresse avant tout enregistrement afin de prévenir les erreurs.
  - Ajout, modification et suppression de thèses et mémoires.
  - Saisie complète des métadonnées (titre, auteur, encadrant, année, type, UFR, discipline, résumé, mots-clés, sélection du fichier PDF).
  - Définition du niveau d'accès (`TELECHARGEABLE`, `CONSULTATION_SEULE`, `RESTREINT`).
  - Cloisonnement strict à l'UFR du gestionnaire connecté.
  - Consultation de la disponibilité et de l'historique complet des téléchargements de l'UFR.
- [x] **Module 3 : Recherche et Consultation (Étudiant)**
  - Authentification dédiée sans mot de passe.
  - Moteur de recherche multicritère en direct (titre, auteur, mot-clé, année, UFR, discipline).
  - Volet de prévisualisation détaillée (Master-Detail).
  - Téléchargement du PDF conditionné aux droits d'accès.
  - Historique personnel des téléchargements de l'étudiant.

### Modules Optionnels & Bonus (3.2 & 3.3)
- [x] **Module 3.2 : Statistiques et Rapports**
  - Statistiques globales (total documents, utilisateurs, téléchargements).
  - Top des documents les plus téléchargés.
  - Activité par UFR et répartition par discipline/année.
  - Liste des documents placés sous embargo restreint.
- [x] **Module 3.3 (Bonus) : Formulaire de Demande d'Accès aux Documents Restreints**
  - **Côté étudiant (`EtudiantDashboard`) :** nouvel onglet "Demandes d'Accès (Embargo)" avec formulaire de soumission (référence exacte du document + motif obligatoire) et registre personnel des demandes avec leur statut de traitement (`EN_ATTENTE`, `ACCEPTEE`, `REFUSEE`).
  - Le catalogue étudiant reste strictement purgé des documents `RESTREINT` (Règle 3 préservée) : la demande est résolue par le service sans jamais exposer le fonds sous embargo.
  - **Côté gestionnaire (`GestionnaireDashboard`) :** nouvel onglet "Demandes d'Accès (UFR x)" listant uniquement les demandes portant sur les documents de son UFR (Règle 4), avec boutons d'acceptation/refus après confirmation expresse.
  - **Effet de l'acceptation :** l'étudiant peut consulter la fiche détaillée (métadonnées + résumé) du document sous embargo ; **le téléchargement du PDF reste formellement bloqué** (Règle 1 préservée et testée).
  - Contrôles métier : doublon de demande active (`DoublonException`), demande sur un document non restreint (`ChampInvalideException`), traitement hors UFR (`AccesRefuseException`).
  - **Persistance bi-mode :** table SQL `demande_acces` (clés étrangères + statut ENUM) côté MySQL, `Map<Integer, DemandeAcces>` côté mémoire via `DataStore` et `DAOFactory`.
- [x] **Module 3.3 (Bonus) : Notifications Visuelles des Nouveautés de la Filière**
  - `NotificationService` (singleton) combinant deux sources dédupliquées par `HashSet` : les dépôts récents persistés (date d'ajout ≤ 7 jours) et les publications enregistrées pendant la session.
  - `DocumentService.ajouterDocument` publie automatiquement une notification vers l'UFR du document déposé.
  - **Panneau de notification dans `EtudiantDashboard` :** bannière émeraude affichant "🔔 X nouveau(x) document(s) publié(s) dans votre UFR" avec le détail des titres, masquée automatiquement lorsqu'il n'y a pas de nouveautés.
  - Message de confirmation enrichi côté gestionnaire ("Les étudiants de votre UFR seront notifiés de cette nouvelle publication.").
- [x] **Refonte Graphique & Ergonomique Complète (charte graphique du projet)**
  - **Charte graphique officielle :** Couleurs saphir `#1D4ED8`, fond `#F8F9FF`, ardoise `#0F172A`, émeraude `#059669`, ambre `#D97706` (centralisées dans `UIUtils.java`).
  - **Portail d'authentification :** Double carte côte à côte (`LoginFrame.java`) avec accès étudiant sans mot de passe à gauche et personnel universitaire à droite.
  - **Espace Gestionnaire :** Barre d'en-tête, bandeau d'accueil UFR, 4 cartes d'indicateurs à mise à jour dynamique, boutons d'action rapide.
  - **Dialogue de Contrôle Hors UFR :** `DialogueAlerteEnrolementHorsUfr.java` avec cartes comparatives, bouton d'auto-rectification vers l'UFR de tutelle et bouton de confirmation d'affectation ciblée.
  - **Espaces Étudiant & Admin :** Intégration du TopBar institutionnel et synchronisation bi-mode.
  - **Validation Automatique :** 22 / 22 tests de validation des règles métier et bonus passés avec succès (100%).

---

## 8. Travaux Restants et Recommandations pour l'Agent Suivant

1. **Vidéo de démonstration obligatoire pour la remise (SEUL LIVRABLE RESTANT) :**
   - Le sujet exige une capture vidéo d'écran (format MP4 recommandé ou lien Cloud inséré dans `README.txt`).
   - **Scénario détaillé minute par minute :** voir le fichier `SCENARIO_VIDEO_DEMO.md` à la racine du projet.
   - Scénario recommandé pour la démo vidéo :
     1. Lancement via `run.bat`.
     2. Connexion Administrateur (`admin@ugb.edu.sn` / `admin123`) : montrer la liste des utilisateurs, création d'un gestionnaire SAT.
     3. Connexion Gestionnaire SAT (`moussa.diakhame@ugb.edu.sn` / `pass123`) : montrer le fonds SAT, ajout d'un mémoire, tentative de modification hors UFR bloquée, traitement d'une demande d'accès, consultation des stats et export CSV.
     4. Connexion Étudiant (`Ibrahima SOW` / `P28 0145`) : notifications de nouveautés, recherche multicritère, prévisualisation, téléchargement réussi d'un document `TELECHARGEABLE`, blocage expliqué sur `CONSULTATION_SEULE`, invisibilité des documents `RESTREINT`, soumission d'une demande d'accès.
2. **Inscription du lien de la vidéo :**
   - Compléter la section 7 du `README.txt` (`[A COMPLETER AVANT L'ENVOI]`) avec le lien Google Drive / OneDrive ou joindre le MP4 à l'archive.
3. **Conservation du chemin propre :**
   - Travailler toujours dans `C:\Users\ibrah\Desktop\Projet_JAVA` pour éviter les erreurs d'encodage de caractères spéciaux (`&`, accents, emojis) sous les outils en ligne de commande Windows.
4. **Nettoyage optionnel avant remise :**
   - Le dossier `com.centre-doc-ugb/` est une ancienne variante du projet (avec PostgreSQL) : ne pas l'inclure dans l'archive de remise pour éviter toute confusion.
   - Les mots de passe sont stockés en clair (simplification académique assumée) : à mentionner comme perspective d'amélioration lors de la soutenance.
