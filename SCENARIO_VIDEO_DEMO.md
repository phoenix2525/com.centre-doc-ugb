# SCÉNARIO DE LA VIDÉO DE DÉMONSTRATION — Projet POO2 (L3 UGB)

> **OBJECTIF :** Servir de script de tournage pour la vidéo de démonstration OBLIGATOIRE (remise du 05 Octobre 2026 à 23h00).
> **Format requis :** MP4 recommandé (ou lien Google Drive / OneDrive explicite à insérer dans la section 7 du `README.txt`).
> **Durée cible :** 6 à 9 minutes.
> **Règle d'or :** Montrer à l'écran le démarrage, la navigation inter-interfaces, la gestion des utilisateurs et documents, la recherche, le téléchargement — ET les 2 modules bonus.

---

## 0. Préparation avant l'enregistrement (10 minutes)

1. **Machine de tournage :** Windows avec JDK 17/21 installé. Aucune autre fenêtre ouverte (bureau propre, notifications Windows désactivées : *Assistance Concentration*).
2. **Outil de capture :** OBS Studio (gratuit) ou **Xbox Game Bar** (`Win + G`, capture d'écran intégrée Windows).
   - Résolution : 1920×1080 (Full HD), 30 fps minimum.
   - Enregistrer l'audio micro en même temps (commentaire voix) — recommandé.
3. **Mode d'exécution :** lancer SANS MySQL pour utiliser le **mode mémoire vive** (bascule transparente) : aucune configuration nécessaire, aucune donnée polluée, données de démo garanties identiques à chaque prise.
   - La barre d'état de `LoginFrame` affichera « Mode Mémoire Vive » — l'expliquer à l'oral comme une force de l'architecture (DAOFactory).
4. **Vérifier l'état des lieux :** double-cliquer sur `compile.bat` puis `run_tests.bat` → **22 / 22 tests réussis** (à filmer, voir scène 6).
5. **Texte d'introduction à préparer (30 s) :**
   - Noms du trinôme, filière L3 INFO/MIAGE, UFR SAT — Université Gaston Berger.
   - Sujet : Système d'Information des Centres de Documentation de l'UGB — Gestion des thèses et mémoires.
   - Technologies : Java Swing, JDBC MySQL avec bascule mémoire automatique, collections `java.util`, exceptions métier personnalisées.

---

## 1. Scène 1 — Lancement & Écran d'accueil (≈ 45 s)

- Double-clic sur `run.bat` → la fenêtre « SI Centres de Documentation UGB » apparaît.
- Montrer le **portail d'authentification** : double carte côte à côte.
  - À gauche : **Accès Libre Étudiant — Sans Mot de Passe** (4 champs : Prénom, Nom, Email `@ugb.edu.sn`, Code Étudiant).
  - À droite : **Espace Réservé Personnel & Enseignants** (Email + Mot de passe).
- Montrer la barre d'état (« MySQL Connecté » ou « Mode Mémoire Vive ») et le bouton **⚙ Configurer BDD** (l'ouvrir 2 secondes pour montrer les paramètres, puis Échap).
- **À l'oral :** citer la bascule bi-mode transparente de `DAOFactory` (si MySQL est éteint, l'application bascule sur les collections en mémoire sans planter).

## 2. Scène 2 — Module 1 : Espace Administrateur (≈ 1 min 30)

1. Se connecter : `admin@ugb.edu.sn` / `admin123` (utiliser le raccourci de pré-remplissage si disponible).
2. **Onglet « Gestion des Gestionnaires » :**
   - Montrer la liste des gestionnaires et leur UFR d'affectation.
   - **Créer un nouveau gestionnaire SAT en direct** : bouton « Nouveau » → remplir (Prénom, Nom, Email `@ugb.edu.sn`, UFR SAT, mot de passe) → « + Ajouter ».
   - Montrer la nouvelle ligne dans la table.
3. **Onglet « Annuaire & Recherche » :** recherche instantanée par nom ou email (taper « diallo » par exemple), filtre de rôle.
4. **Onglet « Statistiques & Export » :** 3 cartes KPI + graphiques (barres UFR, anneau disciplines) → cliquer « Actualiser ».
5. **Déconnexion** → retour au portail.

## 3. Scène 3 — Module 2 : Espace Gestionnaire UFR SAT (≈ 2 min 30)

1. Se connecter : `moussa.diakhame@ugb.edu.sn` / `pass123`.
2. Montrer la **bannière UFR personnalisée** : badge institutionnel « Gestionnaire UFR SAT », titre « CENTRE DE DOCUMENTATION — UFR SAT », 4 KPI cards dynamiques.
3. **Onglet « Fonds Documentaire (UFR SAT) » :**
   - Montrer la recherche en direct (taper « blockchain » ou « NDAO »).
   - **Ajouter un mémoire en direct** : « + Déposer pour l'UFR SAT » → remplir les 10 métadonnées (titre, type MÉMOIRE, auteur, encadrant, année 2026, discipline « Informatique », résumé, mots-clés, niveau d'accès **TELECHARGEABLE**, PDF `docs/memoire_ndao_2025.pdf` ou n'importe quel PDF) → « Enregistrer dans mon UFR ».
   - **Message clé à montrer :** « 🔔 Les étudiants de votre UFR seront notifiés de cette nouvelle publication. » (Bonus notifications).
   - Sélectionner le document → volet aperçu → « Modifier ce document » : changer l'UFR de ciblage est impossible (verrouillé) — à l'oral, citer la **Règle 4** (cloisonnement UFR, testée par `AccesRefuseException`).
4. **Onglet « 📩 Demandes d'Accès (UFR SAT) » (Bonus) :**
   - Montrer la demande en attente de l'étudiante **Fatou NDIAYE** (donnée de démo) sur le mémoire sous embargo.
   - **Accepter la demande** : sélectionner la ligne → « ✔ Accepter la demande sélectionnée » → confirmer → message « l'étudiant peut désormais consulter la fiche détaillée (téléchargement toujours bloqué) ».
5. **Onglet « 🎓 Enrôlement des Étudiants » :** montrer le registre et la recherche dynamique.
6. **Onglet « 📊 Historique des Téléchargements (UFR SAT) » :** montrer les téléchargements de l'UFR.
7. **Déconnexion.**

## 4. Scène 4 — Module 3 : Espace Étudiant (≈ 2 min 30)

1. Se connecter **sans mot de passe** : Prénom `Ibrahima`, Nom `SOW`, Email `ibrahima.sow@ugb.edu.sn`, Code `P28 0145` (raccourci de pré-remplissage disponible).
2. **Panneau de notifications (Bonus)** — à montrer immédiatement : la bannière émeraude « 🔔 1 nouveau(x) document(s) publié(s) dans votre UFR (Sciences Appliquées et de Technologie) » avec le titre du mémoire ajouté en scène 3. À l'oral : « le système notifie visuellement l'étudiant des nouveautés de sa filière ».
3. **Onglet « Recherche & Consultation » :**
   - **Recherche multicritère en direct** : taper un mot-clé, changer l'UFR, tester l'année.
   - **Règle 3 à l'oral + à l'écran :** le mémoire sous embargo « Vulnérabilités cryptographiques... IoT » n'apparaît NULLE PART dans le catalogue ni dans les recherches.
   - Sélectionner un document `TELECHARGEABLE` → prévisualisation → **« ⬇️ Télécharger le document PDF »** → choisir le dossier Bureau → message « Téléchargement réussi » + le fichier apparaît sur le Bureau (montrer-le).
   - Sélectionner un document `CONSULTATION_SEULE` (ex : « Impact de la digitalisation des paiements mobiles ») → bouton remplacé par « 🚫 Téléchargement bloqué (Consultation Seule) » + avertissement explicite.
4. **Onglet « 📩 Demandes d'Accès (Embargo) » (Bonus) :**
   - Montrer le registre : la demande déjà acceptée (si gérée en scène 3) ou soumettre une nouvelle demande : référence exacte du document sous embargo + motif → « 📤 Soumettre la demande d'accès » → message « en attente de validation ».
   - Sélectionner une demande **ACCEPTEE** → « 📖 Consulter la fiche autorisée » → la fiche détaillée s'affiche avec l'avertissement « téléchargement du PDF reste bloqué (document sous embargo) ».
   - Montrer le contrôle de doublon : soumettre à nouveau la même référence → message « Vous avez déjà soumis une demande d'accès pour ce document ».
5. **Onglet « Historique des telechargements » :** l'historique personnel contient le téléchargement effectué.
6. **Déconnexion.**

## 5. Scène 5 — Architecture & Base de données (≈ 45 s, optionnel mais valorisant)

- Ouvrir rapidement l'explorateur : arborescence `src/sn/ugb/centredoc` (model, exception, dao jdbc/memory, service, ui).
- Ouvrir `centre_doc.sql` : montrer les 5 tables (`ufr`, `utilisateurs`, `documents`, `telechargements`, `demande_acces`) et les clés étrangères.
- À l'oral : architecture en couches, `DAOFactory` (Factory Pattern), exceptions personnalisées dérivant de `CentreDocException`, collections exclusives `java.util` (aucun tableau brut).

## 6. Scène 6 — Validation automatique : 22 / 22 tests (≈ 30 s)

- Fermer l'application puis lancer `run_tests.bat` en console.
- Filmer le défilement des sections 1 → 9 et le résultat final **« RÉSULTAT : 22 / 22 tests réussis »**.
- À l'oral : citer 2-3 exemples (embargo masqué, cloisonnement UFR, demande d'accès).

## 7. Scène 7 — Conclusion (≈ 20 s)

- Rappeler : les 3 modules obligatoires + stats + 2 bonus (demandes d'accès, notifications).
- Rappeler les modalités de remise : sources + SQL + `README.txt` + vidéo.

---

## Checklist avant l'envoi du courriel

- [ ] Vidéo MP4 enregistrée (6-9 min) et visionnée intégralement.
- [ ] Lien Google Drive / OneDrive (accès « toute personne avec le lien ») inséré dans la **section 7 du `README.txt`** (remplacer `[A COMPLETER AVANT L'ENVOI]`).
- [ ] Archive contenant : `src/`, `centre_doc.sql`, `centre_doc.jar`, `README.txt`, `compile.bat`, `run.bat`, `run_tests.bat`, `sources.txt`, `lib/`, `docs/`.
- [ ] **Ne pas inclure** le dossier `com.centre-doc-ugb/` (ancienne variante) ni le dossier `Agent/` (documents internes).
- [ ] Objet du courriel : `ProjetJAVAL3_INFO` ou `ProjetJAVAL3_MIAGE`.
- [ ] Envoi avant le **05 Octobre 2026 à 23h00**.
