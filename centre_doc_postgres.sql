-- ==============================================================================
-- UNIVERSITÉ GASTON BERGER (UGB) - SAINT-LOUIS
-- UFR DE SCIENCES APPLIQUÉES ET DE TECHNOLOGIE (UFR SAT)
-- UE : POO2 (Programmation Orientée Objet 2 - L3 INFO / MIAGE)
-- 
-- Projet : Système d'Information des Centres de Documentation de l'UGB
-- Script d'initialisation pour POSTGRESQL : centre_doc_postgres.sql
-- ==============================================================================

-- 1. Nettoyage des tables existantes
DROP TABLE IF EXISTS telechargements CASCADE;
DROP TABLE IF EXISTS documents CASCADE;
DROP TABLE IF EXISTS utilisateurs CASCADE;
DROP TABLE IF EXISTS ufr CASCADE;

-- 2. Création de la table 'ufr'
CREATE TABLE ufr (
    id_ufr SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    nom VARCHAR(150) NOT NULL
);

-- 3. Création de la table 'utilisateurs'
CREATE TABLE utilisateurs (
    id_utilisateur SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NULL,
    role VARCHAR(30) NOT NULL,
    id_ufr INT NULL,
    code_etudiant VARCHAR(50) NULL UNIQUE,
    CONSTRAINT fk_utilisateur_ufr FOREIGN KEY (id_ufr) REFERENCES ufr(id_ufr) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- 4. Création de la table 'documents'
CREATE TABLE documents (
    id_document SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(150) NOT NULL,
    encadrant VARCHAR(150) NOT NULL,
    annee INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    id_ufr INT NOT NULL,
    discipline VARCHAR(100) NOT NULL,
    resume TEXT NOT NULL,
    mots_cles VARCHAR(255) NOT NULL,
    chemin_pdf VARCHAR(255) NOT NULL,
    niveau_acces VARCHAR(30) NOT NULL DEFAULT 'TELECHARGEABLE',
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_ufr FOREIGN KEY (id_ufr) REFERENCES ufr(id_ufr) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 5. Création de la table 'telechargements'
CREATE TABLE telechargements (
    id_telechargement SERIAL PRIMARY KEY,
    id_utilisateur INT NOT NULL,
    id_document INT NOT NULL,
    date_telechargement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_telechargement_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id_utilisateur) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_telechargement_document FOREIGN KEY (id_document) REFERENCES documents(id_document) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==============================================================================
-- JEU DE DONNÉES OFFICIEL UGB
-- ==============================================================================

-- 1. Insertion des UFR
INSERT INTO ufr (id_ufr, code, nom) VALUES
(1, 'SAT',  'Sciences Appliquées et de Technologie'),
(2, 'SJP',  'Sciences Juridiques et Politiques'),
(3, 'SEG',  'Sciences Économiques et de Gestion'),
(4, 'LSH',  'Lettres et Sciences Humaines'),
(5, 'CRAC', 'Civilisations, Religions, Arts et Communication'),
(6, '2S',   'Sciences de la Santé'),
(7, 'SEFS', 'Sciences de l''Éducation, de la Formation et du Sport'),
(8, 'IPSL', 'Institut Polytechnique de Saint-Louis');

-- Réaligner la séquence UFR
SELECT setval('ufr_id_ufr_seq', (SELECT MAX(id_ufr) FROM ufr));

-- 2. Insertion des utilisateurs
INSERT INTO utilisateurs (id_utilisateur, nom, prenom, email, mot_de_passe, role, id_ufr, code_etudiant) VALUES
(1, 'DIOP', 'Amadou', 'admin@ugb.edu.sn', 'admin123', 'ADMIN', NULL, NULL),
(2, 'DIAKHAME', 'Moussa', 'moussa.diakhame@ugb.edu.sn', 'pass123', 'GESTIONNAIRE', 1, NULL),
(3, 'DIALLO', 'Aminata', 'aminata.diallo@ugb.edu.sn', 'pass123', 'GESTIONNAIRE', 3, NULL),
(4, 'FALL', 'Cheikh', 'cheikh.fall@ugb.edu.sn', 'pass123', 'GESTIONNAIRE', 2, NULL),
(5, 'SOW', 'Ibrahima', 'ibrahima.sow@ugb.edu.sn', NULL, 'ETUDIANT', 1, 'P28 0145'),
(6, 'NDIAYE', 'Fatou', 'fatou.ndiaye@ugb.edu.sn', NULL, 'ETUDIANT', 3, 'P29 0089'),
(7, 'BA', 'Mamadou', 'mamadou.ba@ugb.edu.sn', NULL, 'ETUDIANT', 2, 'P27 0312');

-- Réaligner la séquence Utilisateurs
SELECT setval('utilisateurs_id_utilisateur_seq', (SELECT MAX(id_utilisateur) FROM utilisateurs));

-- 3. Insertion des documents
INSERT INTO documents (id_document, titre, auteur, encadrant, annee, type, id_ufr, discipline, resume, mots_cles, chemin_pdf, niveau_acces) VALUES
(1, 
 'Optimisation des requêtes distribuées dans les bases de données réparties', 
 'Ousmane NDAO', 
 'Dr. Fatou KAMARA-SANGARÉ', 
 2025, 
 'MEMOIRE', 
 1, 
 'Informatique', 
 'Ce mémoire propose de nouvelles heuristiques pour optimiser les plans d''exécution de requêtes SQL sur des nœuds distribués à latence variable.', 
 'BDD, requêtes distribuées, optimisation, SQL', 
 'docs/memoire_ndao_2025.pdf', 
 'TELECHARGEABLE'),

(2, 
 'Application de l''apprentissage profond à la détection de la rétinopathie diabétique', 
 'Mariama SARR', 
 'Pr. Moussa LO', 
 2024, 
 'THESE', 
 1, 
 'Intelligence Artificielle', 
 'Thèse de doctorat portant sur l''utilisation des réseaux de neurones convolutifs (CNN) pour le dépistage automatique des pathologies oculaires en milieu rural.', 
 'deep learning, CNN, santé, IA, vision par ordinateur', 
 'docs/these_sarr_2024.pdf', 
 'TELECHARGEABLE'),

(3, 
 'Impact de la digitalisation des paiements mobiles sur l''inclusion financière au Sénégal', 
 'Aissatou CISSE', 
 'Dr. Abdoulaye SECK', 
 2024, 
 'MEMOIRE', 
 3, 
 'Économie Monétaire', 
 'Analyse empirique de l''adoption des services de mobile money (Wave, Orange Money) et de leur effet sur la bancarisation des populations rurales.', 
 'mobile money, inclusion financière, économie, Fintech', 
 'docs/memoire_cisse_2024.pdf', 
 'CONSULTATION_SEULE'),

(4, 
 'Le statut juridique des contrats intelligents (Smart Contracts) dans l''espace OHADA', 
 'Babacar THIAM', 
 'Pr. Ibrahima DIALLO', 
 2025, 
 'THESE', 
 2, 
 'Droit Privé', 
 'Étude prospective sur l''harmonisation du droit des affaires OHADA face à l''émergence des technologies de registres distribués et smart contracts.', 
 'OHADA, smart contracts, blockchain, droit des affaires', 
 'docs/these_thiam_2025.pdf', 
 'TELECHARGEABLE'),

(5, 
 'Vulnérabilités cryptographiques des protocoles de communication de l''Internet des Objets (IoT)', 
 'Khadim GUEYE', 
 'Dr. Cheikh SARR', 
 2026, 
 'THESE', 
 1, 
 'Cybersécurité', 
 'Travaux sous brevet et embargo : analyse approfondie des vecteurs d''attaque sur les protocoles LoRaWAN et Zigbee en environnement industriel sensible.', 
 'IoT, cryptographie, LoRaWAN, vulnérabilités, cybersécurité', 
 'docs/these_gueye_2026.pdf', 
 'RESTREINT'),

(6, 
 'Gouvernance des données foncières et cadastre numérique dans la vallée du fleuve Sénégal', 
 'Coumba FAYE', 
 'Dr. Oumar SY', 
 2023, 
 'MEMOIRE', 
 4, 
 'Géographie et Aménagement', 
 'Évaluation des conflits d''usage de l''eau et de la terre à travers les systèmes d''information géographique (SIG) communautaires.', 
 'foncier, SIG, vallée du fleuve, gouvernance', 
 'docs/memoire_faye_2023.pdf', 
 'TELECHARGEABLE'),

(7, 
 'Modélisation épidémiologique de la propagation du paludisme dans la région de Saint-Louis', 
 'Moustapha DRAME', 
 'Pr. Alassane SOW', 
 2025, 
 'MEMOIRE', 
 6, 
 'Santé Publique', 
 'Modélisation mathématique et informatique pour prédire les pics saisonniers de paludisme en fonction des précipitations et de la température.', 
 'paludisme, épidémiologie, santé, modèle prédictif', 
 'docs/memoire_drame_2025.pdf', 
 'CONSULTATION_SEULE');

-- Réaligner la séquence Documents
SELECT setval('documents_id_document_seq', (SELECT MAX(id_document) FROM documents));

-- 4. Insertion d'historique de téléchargements
INSERT INTO telechargements (id_telechargement, id_utilisateur, id_document, date_telechargement) VALUES
(1, 5, 1, '2026-09-10 10:14:22'),
(2, 5, 2, '2026-09-12 15:30:00'),
(3, 6, 4, '2026-09-14 09:05:41'),
(4, 7, 4, '2026-09-15 16:48:19');

-- Réaligner la séquence Téléchargements
SELECT setval('telechargements_id_telechargement_seq', (SELECT MAX(id_telechargement) FROM telechargements));
