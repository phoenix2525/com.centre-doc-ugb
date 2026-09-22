package sn.ugb.centredoc.dao.memory;

import sn.ugb.centredoc.model.*;

import java.sql.Timestamp;
import java.util.*;

/**
 * Entrepôt de données en mémoire utilisant le framework des Collections Java (java.util.*).
 * Conçu pour illustrer le cours de POO2 (ArrayList, HashMap, HashSet) et garantir
 * que l'application fonctionne parfaitement même si le serveur MySQL local n'est pas démarré.
 */
public class DataStore {
    private static DataStore instance;

    private final Map<Integer, Ufr> ufrs = new LinkedHashMap<>();
    private final Map<Integer, Utilisateur> utilisateurs = new LinkedHashMap<>();
    private final Map<Integer, Document> documents = new LinkedHashMap<>();
    private final List<Telechargement> telechargements = new ArrayList<>();

    private int nextUserId = 100;
    private int nextDocId = 100;
    private int nextTelechargementId = 100;

    private DataStore() {
        initialiserDonnees();
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void initialiserDonnees() {
        // 1. UFRs de l'UGB
        ajouterUfr(new Ufr(1, "SAT", "Sciences Appliquées et de Technologie"));
        ajouterUfr(new Ufr(2, "SJP", "Sciences Juridiques et Politiques"));
        ajouterUfr(new Ufr(3, "SEG", "Sciences Économiques et de Gestion"));
        ajouterUfr(new Ufr(4, "LSH", "Lettres et Sciences Humaines"));
        ajouterUfr(new Ufr(5, "CRAC", "Civilisations, Religions, Arts et Communication"));
        ajouterUfr(new Ufr(6, "2S", "Sciences de la Santé"));
        ajouterUfr(new Ufr(7, "SEFS", "Sciences de l'Éducation, de la Formation et du Sport"));
        ajouterUfr(new Ufr(8, "IPSL", "Institut Polytechnique de Saint-Louis"));

        // 2. Utilisateurs
        // Admin
        Administrateur admin = new Administrateur(1, "DIOP", "Amadou", "admin@ugb.edu.sn", "admin123");
        utilisateurs.put(admin.getIdUtilisateur(), admin);

        // Gestionnaires
        Gestionnaire gSAT = new Gestionnaire(2, "DIAKHAME", "Moussa", "moussa.diakhame@ugb.edu.sn", "pass123", 1);
        gSAT.setNomUfr("Sciences Appliquées et de Technologie");
        utilisateurs.put(gSAT.getIdUtilisateur(), gSAT);

        Gestionnaire gSEG = new Gestionnaire(3, "DIALLO", "Aminata", "aminata.diallo@ugb.edu.sn", "pass123", 3);
        gSEG.setNomUfr("Sciences Économiques et de Gestion");
        utilisateurs.put(gSEG.getIdUtilisateur(), gSEG);

        Gestionnaire gSJP = new Gestionnaire(4, "FALL", "Cheikh", "cheikh.fall@ugb.edu.sn", "pass123", 2);
        gSJP.setNomUfr("Sciences Juridiques et Politiques");
        utilisateurs.put(gSJP.getIdUtilisateur(), gSJP);

        // Étudiants (sans mot de passe)
        Etudiant e1 = new Etudiant(5, "SOW", "Ibrahima", "ibrahima.sow@ugb.edu.sn", "P28-2023-0145", 1);
        e1.setNomUfr("Sciences Appliquées et de Technologie");
        utilisateurs.put(e1.getIdUtilisateur(), e1);

        Etudiant e2 = new Etudiant(6, "NDIAYE", "Fatou", "fatou.ndiaye@ugb.edu.sn", "P29-2024-0089", 3);
        e2.setNomUfr("Sciences Économiques et de Gestion");
        utilisateurs.put(e2.getIdUtilisateur(), e2);

        Etudiant e3 = new Etudiant(7, "BA", "Mamadou", "mamadou.ba@ugb.edu.sn", "P27-2022-0312", 2);
        e3.setNomUfr("Sciences Juridiques et Politiques");
        utilisateurs.put(e3.getIdUtilisateur(), e3);

        // 3. Documents
        ajouterDoc(new Document(1, "Optimisation des requêtes distribuées dans les bases de données réparties",
                "Ousmane NDAO", "Dr. Fatou KAMARA-SANGARÉ", 2025, TypeDocument.MEMOIRE, 1,
                "Informatique",
                "Ce mémoire propose de nouvelles heuristiques pour optimiser les plans d'exécution de requêtes SQL sur des nœuds distribués à latence variable.",
                "BDD, requêtes distribuées, optimisation, SQL", "docs/memoire_ndao_2025.pdf", NiveauAcces.TELECHARGEABLE));

        ajouterDoc(new Document(2, "Application de l'apprentissage profond à la détection de la rétinopathie diabétique",
                "Mariama SARR", "Pr. Moussa LO", 2024, TypeDocument.THESE, 1,
                "Intelligence Artificielle",
                "Thèse de doctorat portant sur l'utilisation des réseaux de neurones convolutifs (CNN) pour le dépistage automatique des pathologies oculaires en milieu rural.",
                "deep learning, CNN, santé, IA, vision par ordinateur", "docs/these_sarr_2024.pdf", NiveauAcces.TELECHARGEABLE));

        ajouterDoc(new Document(3, "Impact de la digitalisation des paiements mobiles sur l'inclusion financière au Sénégal",
                "Aissatou CISSE", "Dr. Abdoulaye SECK", 2024, TypeDocument.MEMOIRE, 3,
                "Économie Monétaire",
                "Analyse empirique de l'adoption des services de mobile money (Wave, Orange Money) et de leur effet sur la bancarisation des populations rurales.",
                "mobile money, inclusion financière, économie, Fintech", "docs/memoire_cisse_2024.pdf", NiveauAcces.CONSULTATION_SEULE));

        ajouterDoc(new Document(4, "Le statut juridique des contrats intelligents (Smart Contracts) dans l'espace OHADA",
                "Babacar THIAM", "Pr. Ibrahima DIALLO", 2025, TypeDocument.THESE, 2,
                "Droit Privé",
                "Étude prospective sur l'harmonisation du droit des affaires OHADA face à l'émergence des technologies de registres distribués et smart contracts.",
                "OHADA, smart contracts, blockchain, droit des affaires", "docs/these_thiam_2025.pdf", NiveauAcces.TELECHARGEABLE));

        ajouterDoc(new Document(5, "Vulnérabilités cryptographiques des protocoles de communication de l'Internet des Objets (IoT)",
                "Khadim GUEYE", "Dr. Cheikh SARR", 2026, TypeDocument.THESE, 1,
                "Cybersécurité",
                "Travaux sous brevet et embargo : analyse approfondie des vecteurs d'attaque sur les protocoles LoRaWAN et Zigbee en environnement industriel sensible.",
                "IoT, cryptographie, LoRaWAN, vulnérabilités, cybersécurité", "docs/these_gueye_2026.pdf", NiveauAcces.RESTREINT));

        ajouterDoc(new Document(6, "Gouvernance des données foncières et cadastre numérique dans la vallée du fleuve Sénégal",
                "Coumba FAYE", "Dr. Oumar SY", 2023, TypeDocument.MEMOIRE, 4,
                "Géographie et Aménagement",
                "Évaluation des conflits d'usage de l'eau et de la terre à travers les systèmes d'information géographique (SIG) communautaires.",
                "foncier, SIG, vallée du fleuve, gouvernance", "docs/memoire_faye_2023.pdf", NiveauAcces.TELECHARGEABLE));

        ajouterDoc(new Document(7, "Modélisation épidémiologique de la propagation du paludisme dans la région de Saint-Louis",
                "Moustapha DRAME", "Pr. Alassane SOW", 2025, TypeDocument.MEMOIRE, 6,
                "Santé Publique",
                "Modélisation mathématique et informatique pour prédire les pics saisonniers de paludisme en fonction des précipitations et de la température.",
                "paludisme, épidémiologie, santé, modèle prédictif", "docs/memoire_drame_2025.pdf", NiveauAcces.CONSULTATION_SEULE));

        // 4. Téléchargements
        enregistrerTelechargement(new Telechargement(1, 5, 1, new Timestamp(System.currentTimeMillis() - 86400000L * 5)));
        enregistrerTelechargement(new Telechargement(2, 5, 2, new Timestamp(System.currentTimeMillis() - 86400000L * 3)));
        enregistrerTelechargement(new Telechargement(3, 6, 4, new Timestamp(System.currentTimeMillis() - 86400000L * 2)));
        enregistrerTelechargement(new Telechargement(4, 7, 4, new Timestamp(System.currentTimeMillis() - 86400000L)));
    }

    private void ajouterUfr(Ufr ufr) {
        ufrs.put(ufr.getIdUfr(), ufr);
    }

    private void ajouterDoc(Document doc) {
        Ufr ufr = ufrs.get(doc.getIdUfr());
        if (ufr != null) {
            doc.setNomUfr(ufr.getNom());
        }
        documents.put(doc.getIdDocument(), doc);
    }

    private void enrichirTelechargement(Telechargement t) {
        Utilisateur u = utilisateurs.get(t.getIdUtilisateur());
        if (u != null) {
            t.setNomUtilisateur(u.getNomComplet());
            t.setEmailUtilisateur(u.getEmail());
            t.setCodeEtudiant(u.getCodeEtudiant());
        }
        Document d = documents.get(t.getIdDocument());
        if (d != null) {
            t.setTitreDocument(d.getTitre());
            t.setUfrDocument(d.getNomUfr());
        }
    }

    public void enregistrerTelechargement(Telechargement t) {
        if (t.getIdTelechargement() <= 0) {
            t.setIdTelechargement(nextTelechargementId++);
        }
        enrichirTelechargement(t);
        telechargements.add(t);
    }

    public Map<Integer, Ufr> getUfrs() { return ufrs; }
    public Map<Integer, Utilisateur> getUtilisateurs() { return utilisateurs; }
    public Map<Integer, Document> getDocuments() { return documents; }
    public List<Telechargement> getTelechargements() { return telechargements; }

    public int getNextUserId() { return nextUserId++; }
    public int getNextDocId() { return nextDocId++; }
}
