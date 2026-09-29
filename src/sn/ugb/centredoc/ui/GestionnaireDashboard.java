package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.AuthService;
import sn.ugb.centredoc.service.DocumentService;
import sn.ugb.centredoc.service.UtilisateurService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Tableau de bord Gestionnaire de Centre de Documentation (Module 2 : Gestion du Fonds Documentaire & Enrôlement Étudiant).
 * Règle d'or 4 : Un gestionnaire ne gère QUE les documents de son UFR.
 * Personnalisation forte selon l'UFR d'affectation (ex: UFR SAT).
 * Module d'enrôlement des étudiants avec alerte en cas de choix d'une autre UFR.
 */
public class GestionnaireDashboard extends JFrame {
    private final Utilisateur gestionnaire;
    private final DocumentService documentService = new DocumentService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Ufr ufrGestionnaire;

    // Composants Onglet Fonds Documentaire
    private JTable tableDocuments;
    private DefaultTableModel modelDocuments;
    private JTextField txtFiltreTitre;
    private JLabel lblNbDocs;

    // Volet de prévisualisation latérale
    private JPanel panelApercu;
    private JLabel lblApercuTitre;
    private JLabel lblApercuType;
    private JLabel lblApercuAuteur;
    private JLabel lblApercuEncadrant;
    private JLabel lblApercuAnneeDiscipline;
    private JLabel lblApercuAcces;
    private JTextArea txtApercuResume;
    private JLabel lblApercuMotsCles;
    private JButton btnApercuModifier;
    private JButton btnApercuSupprimer;
    private Document documentSelectionne = null;

    // Composants Onglet Enrôlement des Étudiants
    private JTable tableEtudiants;
    private DefaultTableModel modelEtudiants;
    private JTextField txtFiltreEtudiant;
    private JLabel lblNbEtudiants;
    private JTextField txtEtuNom;
    private JTextField txtEtuPrenom;
    private JTextField txtEtuEmail;
    private JTextField txtEtuCode;
    private JComboBox<Ufr> comboEtuUfr;
    private JButton btnValiderEnrolement;
    private JButton btnModifierEtudiant;
    private JButton btnSupprimerEtudiant;
    private int idEtudiantSelectionne = 0;

    // Composants Onglet Historique
    private JTable tableHistorique;
    private DefaultTableModel modelHistorique;

    // Composants KPIs & Navigation Stitch
    private JLabel lblKpiFondsVal;
    private JLabel lblKpiEtudiantsVal;
    private JLabel lblKpiTelechargementsVal;
    private JTabbedPane tabbedPane;

    public GestionnaireDashboard(Utilisateur gestionnaire) {
        this.gestionnaire = gestionnaire;
        chargerUfrGestionnaire();

        String codeUfr = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "UFR";
        String nomUfr = (ufrGestionnaire != null && ufrGestionnaire.getNom() != null) ? ufrGestionnaire.getNom() : "";
        setTitle("SI Centres de Documentation UGB — Espace Gestionnaire : UFR " + codeUfr + " (" + nomUfr + ")");

        initUI();
        chargerFonds();
        chargerEtudiants();
        chargerHistorique();
    }

    private void chargerUfrGestionnaire() {
        try {
            if (gestionnaire.getIdUfr() != null && gestionnaire.getIdUfr() > 0) {
                this.ufrGestionnaire = DAOFactory.getUfrDAO().trouverParId(gestionnaire.getIdUfr());
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger l'UFR du gestionnaire : " + e.getMessage());
        }
        if (this.ufrGestionnaire == null) {
            String nom = (gestionnaire.getNomUfr() != null && !gestionnaire.getNomUfr().isEmpty()) 
                    ? gestionnaire.getNomUfr() : "UFR " + (gestionnaire.getIdUfr() != null ? gestionnaire.getIdUfr() : 1);
            this.ufrGestionnaire = new Ufr(gestionnaire.getIdUfr() != null ? gestionnaire.getIdUfr() : 1, "UFR", nom);
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 820);
        setMinimumSize(new Dimension(1080, 740));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.FOND_CLAIR);

        String codeUfr = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "UFR";
        String nomUfr = (ufrGestionnaire != null && ufrGestionnaire.getNom() != null) ? ufrGestionnaire.getNom() : "";

        JButton btnDeconnexion = UIUtils.creerBoutonDanger("⏻ Déconnexion");
        btnDeconnexion.setFont(UIUtils.FONT_PETIT);
        btnDeconnexion.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        // 1. En-tête composé Stitch : TopBar + Hero Banner UFR + 4 KPI Cards
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);

        JPanel topBar = UIUtils.creerTopBarStitch(
                "Système d'Information Doc UGB",
                "PORTAIL DES THÈSES & MÉMOIRES",
                "Gestionnaire UFR " + codeUfr,
                gestionnaire.getNomComplet(),
                DBConnection.isMySQLAvailable(),
                btnDeconnexion
        );
        headerContainer.add(topBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        // Actions rapides du Hero Banner
        java.util.List<JButton> actionsRapides = new java.util.ArrayList<>();

        JButton btnHeroEnroler = UIUtils.creerBoutonEmeraude("👤+ Enrôler un étudiant");
        btnHeroEnroler.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHeroEnroler.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        actionsRapides.add(btnHeroEnroler);

        JButton btnHeroDeposer = UIUtils.creerBoutonPrimaire("📄+ Déposer une thèse ou mémoire");
        btnHeroDeposer.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHeroDeposer.addActionListener(e -> ouvrirDialogueDocument(null));
        actionsRapides.add(btnHeroDeposer);

        JPanel heroBanner = UIUtils.creerHeroBannerStitch(
                "CENTRE DE DOCUMENTATION — UFR " + codeUfr + " (" + nomUfr.toUpperCase() + ")",
                "Pôle d'Archivage & Thèses d'Ingénierie",
                "Pôle de gestion documentaire des Thèses et Mémoires — Université Gaston Berger de Saint-Louis.",
                actionsRapides
        );

        // Panneau KPI Stitch
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblKpiFondsVal = new JLabel("0");
        lblKpiEtudiantsVal = new JLabel("0");
        lblKpiTelechargementsVal = new JLabel("0");

        JPanel card1 = UIUtils.creerKPICardStitch("FONDS ARCHIVÉ", lblKpiFondsVal, "Thèses & mémoires UFR", "📚", UIUtils.BLEU_UGB);
        JPanel card2 = UIUtils.creerKPICardStitch("ÉTUDIANTS ENRÔLÉS", lblKpiEtudiantsVal, "Promotion " + codeUfr + " active", "🎓", UIUtils.VERT_SUCCES);
        JPanel card3 = UIUtils.creerKPICardStitch("TÉLÉCHARGEMENTS", lblKpiTelechargementsVal, "Ce semestre académique", "⬇", UIUtils.OR_UGB);
        JPanel card4 = UIUtils.creerKPICardStitch("DISPONIBILITÉ DU DÉPÔT", "100%", "Indexation synchronisée", "⚡", UIUtils.ACCENT_EMERAUDE);

        kpiPanel.add(card1);
        kpiPanel.add(card2);
        kpiPanel.add(card3);
        kpiPanel.add(card4);

        JPanel bannerWithKpi = new JPanel(new BorderLayout());
        bannerWithKpi.setOpaque(false);
        bannerWithKpi.add(heroBanner, BorderLayout.NORTH);
        bannerWithKpi.add(kpiPanel, BorderLayout.SOUTH);

        headerContainer.add(bannerWithKpi, BorderLayout.CENTER);
        add(headerContainer, BorderLayout.NORTH);

        tabbedPane.addTab("📚 Fonds Documentaire (UFR " + codeUfr + ")", creerOngletFonds());
        tabbedPane.addTab("🎓 Enrôlement des Étudiants", creerOngletEnrolement());
        tabbedPane.addTab("📊 Historique des Téléchargements (UFR " + codeUfr + ")", creerOngletHistorique());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerOngletFonds() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Barre d'outils supérieure et recherche dynamique
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(UIUtils.BLANC);
        toolbar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchLeft.setOpaque(false);

        searchLeft.add(new JLabel("Rechercher en direct (Titre / Auteur) :"));
        txtFiltreTitre = UIUtils.creerChampTexte(20);
        txtFiltreTitre.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrerTableDocuments(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrerTableDocuments(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrerTableDocuments(); }
        });
        searchLeft.add(txtFiltreTitre);

        JButton btnReset = UIUtils.creerBoutonSecondaire("Effacer");
        btnReset.addActionListener(e -> {
            txtFiltreTitre.setText("");
            chargerFonds();
        });
        searchLeft.add(btnReset);

        toolbar.add(searchLeft, BorderLayout.WEST);

        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsRight.setOpaque(false);

        String codeUfr = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "UFR";
        String nomUfr = (ufrGestionnaire != null && ufrGestionnaire.getNom() != null) ? ufrGestionnaire.getNom() : "";

        JButton btnAjouter = UIUtils.creerBoutonAccent("+ Déposer pour l'UFR " + codeUfr);
        btnAjouter.addActionListener(e -> ouvrirDialogueDocument(null));
        actionsRight.add(btnAjouter);

        toolbar.add(actionsRight, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table des documents (Côté gauche du SplitPane)
        String[] colonnes = {"ID", "Type", "Titre", "Auteur", "Encadrant", "Année", "Discipline", "Niveau d'accès"};
        modelDocuments = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDocuments = new JTable(modelDocuments);
        tableDocuments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.configurerTableModerne(tableDocuments);

        tableDocuments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualiserVoletApercu();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableDocuments);
        scrollTable.setBorder(UIUtils.creerBordureCarte("Documents de l'UFR " + codeUfr + " (" + nomUfr + ")"));

        // Volet de prévisualisation latérale (Côté droit du SplitPane)
        panelApercu = creerVoletApercu();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTable, panelApercu);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);

        // Barre d'état inférieure
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        lblNbDocs = new JLabel("Chargement des documents...");
        lblNbDocs.setFont(UIUtils.FONT_PETIT);
        lblNbDocs.setForeground(UIUtils.TEXTE_SECONDAIRE);
        statusBar.add(lblNbDocs, BorderLayout.WEST);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerVoletApercu() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIUtils.BLANC);
        wrapper.setBorder(UIUtils.creerBordureCarte("Fiche Descriptive & Actions"));
        wrapper.setPreferredSize(new Dimension(380, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BLANC);
        content.setBorder(new EmptyBorder(14, 16, 12, 16));

        // Titre du document
        lblApercuTitre = new JLabel("Selectionnez un document");
        lblApercuTitre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblApercuTitre.setForeground(UIUtils.BLEU_UGB);
        lblApercuTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuTitre);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.creerSeparateur());
        content.add(Box.createVerticalStrut(8));

        // Metadonnees
        content.add(UIUtils.creerLabelSection("Type & Auteur"));
        lblApercuType = new JLabel("--");
        lblApercuType.setFont(UIUtils.FONT_NORMAL);
        lblApercuType.setForeground(UIUtils.TEXTE_FONCE);
        lblApercuType.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuType);
        content.add(Box.createVerticalStrut(3));

        lblApercuAuteur = new JLabel("Auteur : --");
        lblApercuAuteur.setFont(UIUtils.FONT_NORMAL);
        lblApercuAuteur.setForeground(UIUtils.TEXTE_FONCE);
        lblApercuAuteur.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuAuteur);
        content.add(Box.createVerticalStrut(3));

        lblApercuEncadrant = new JLabel("Directeur : --");
        lblApercuEncadrant.setFont(UIUtils.FONT_NORMAL);
        lblApercuEncadrant.setForeground(UIUtils.TEXTE_SECONDAIRE);
        lblApercuEncadrant.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuEncadrant);
        content.add(Box.createVerticalStrut(8));
        content.add(UIUtils.creerSeparateur());
        content.add(Box.createVerticalStrut(6));

        content.add(UIUtils.creerLabelSection("Annee & Discipline"));
        lblApercuAnneeDiscipline = new JLabel("-- | --");
        lblApercuAnneeDiscipline.setFont(UIUtils.FONT_NORMAL);
        lblApercuAnneeDiscipline.setForeground(UIUtils.TEXTE_FONCE);
        lblApercuAnneeDiscipline.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuAnneeDiscipline);
        content.add(Box.createVerticalStrut(6));

        content.add(UIUtils.creerLabelSection("Niveau d'acces"));
        lblApercuAcces = new JLabel("--");
        lblApercuAcces.setFont(UIUtils.FONT_GRAS);
        lblApercuAcces.setForeground(UIUtils.BLEU_UGB);
        lblApercuAcces.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuAcces);
        content.add(Box.createVerticalStrut(8));
        content.add(UIUtils.creerSeparateur());
        content.add(Box.createVerticalStrut(6));

        content.add(UIUtils.creerLabelSection("Resume analytique"));
        txtApercuResume = new JTextArea("Aucun document selectionne.", 6, 24);
        txtApercuResume.setFont(UIUtils.FONT_NORMAL);
        txtApercuResume.setForeground(UIUtils.TEXTE_FONCE);
        txtApercuResume.setDisabledTextColor(UIUtils.TEXTE_FONCE);
        txtApercuResume.setLineWrap(true);
        txtApercuResume.setWrapStyleWord(true);
        txtApercuResume.setEditable(false);
        txtApercuResume.setBackground(new Color(248, 250, 252));
        txtApercuResume.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scrollRes = new JScrollPane(txtApercuResume);
        scrollRes.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollRes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        content.add(scrollRes);
        content.add(Box.createVerticalStrut(6));

        lblApercuMotsCles = new JLabel("Mots-cles : --");
        lblApercuMotsCles.setFont(UIUtils.FONT_PETIT);
        lblApercuMotsCles.setForeground(UIUtils.TEXTE_SECONDAIRE);
        lblApercuMotsCles.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuMotsCles);

        wrapper.add(new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // Boutons d'action
        JPanel bottomActions = new JPanel(new GridLayout(2, 1, 6, 6));
        bottomActions.setBackground(UIUtils.BLANC);
        bottomActions.setBorder(new EmptyBorder(8, 12, 12, 12));

        btnApercuModifier = UIUtils.creerBoutonAccent("Modifier ce document");
        btnApercuModifier.setEnabled(false);
        btnApercuModifier.addActionListener(e -> {
            if (documentSelectionne != null) ouvrirDialogueDocument(documentSelectionne);
        });
        bottomActions.add(btnApercuModifier);

        btnApercuSupprimer = UIUtils.creerBoutonDanger("Supprimer du fonds");
        btnApercuSupprimer.setEnabled(false);
        btnApercuSupprimer.addActionListener(e -> supprimerDocumentSelectionne());
        bottomActions.add(btnApercuSupprimer);

        wrapper.add(bottomActions, BorderLayout.SOUTH);
        return wrapper;
    }

    private void actualiserVoletApercu() {
        int row = tableDocuments.getSelectedRow();
        if (row == -1) {
            documentSelectionne = null;
            lblApercuTitre.setText("Sélectionnez un document");
            lblApercuType.setText("—");
            lblApercuAuteur.setText("Auteur : —");
            lblApercuEncadrant.setText("Directeur : —");
            lblApercuAnneeDiscipline.setText("Année / Discipline : —");
            lblApercuAcces.setText("Niveau d'accès : —");
            txtApercuResume.setText("Aucun document sélectionné.");
            lblApercuMotsCles.setText("Mots-clés : —");
            btnApercuModifier.setEnabled(false);
            btnApercuSupprimer.setEnabled(false);
            return;
        }

        int modelRow = tableDocuments.convertRowIndexToModel(row);
        int idDoc = (int) modelDocuments.getValueAt(modelRow, 0);

        try {
            documentSelectionne = documentService.consulterFicheDetaillee(idDoc, gestionnaire);
            lblApercuTitre.setText("<html><b>" + documentSelectionne.getTitre() + "</b></html>");
            lblApercuType.setText("Type : " + documentSelectionne.getType());
            lblApercuAuteur.setText("Auteur : " + documentSelectionne.getAuteur());
            lblApercuEncadrant.setText("Directeur : " + documentSelectionne.getEncadrant());
            lblApercuAnneeDiscipline.setText(documentSelectionne.getAnnee() + " | " + documentSelectionne.getDiscipline());
            lblApercuAcces.setText("Niveau : " + documentSelectionne.getNiveauAcces().getLibelle());
            txtApercuResume.setText(documentSelectionne.getResume());
            txtApercuResume.setCaretPosition(0);
            lblApercuMotsCles.setText("Mots-clés : " + documentSelectionne.getMotsCles());
            btnApercuModifier.setEnabled(true);
            btnApercuSupprimer.setEnabled(true);
        } catch (Exception ex) {
            System.err.println("Erreur consultation aperçu : " + ex.getMessage());
        }
    }

    private JPanel creerOngletHistorique() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        String[] colonnes = {"ID", "Date & Heure", "Titre du Document", "Utilisateur / Étudiant", "Code Étudiant", "Email"};
        modelHistorique = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHistorique = new JTable(modelHistorique);
        UIUtils.configurerTableModerne(tableHistorique);

        JScrollPane scroll = new JScrollPane(tableHistorique);
        scroll.setBorder(UIUtils.creerBordureCarte("Journal des téléchargements des documents de votre UFR"));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        JButton btnRafraichir = UIUtils.creerBoutonSecondaire("🔄 Actualiser l'historique");
        btnRafraichir.addActionListener(e -> chargerHistorique());
        south.add(btnRafraichir);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerOngletEnrolement() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        String codeUfr = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "UFR";
        String nomUfr = (ufrGestionnaire != null && ufrGestionnaire.getNom() != null) ? ufrGestionnaire.getNom() : "";

        // Partie Gauche : Table des étudiants + Barre de recherche instantanée
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setOpaque(false);

        // Barre de recherche
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(UIUtils.BLANC);
        toolbar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(new JLabel("Recherche dynamique (Nom / Prénom / Code / Email) :"));

        txtFiltreEtudiant = UIUtils.creerChampTexte(18);
        txtFiltreEtudiant.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrerTableEtudiants(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrerTableEtudiants(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrerTableEtudiants(); }
        });
        searchLeft.add(txtFiltreEtudiant);

        JButton btnResetSearch = UIUtils.creerBoutonSecondaire("Effacer");
        btnResetSearch.addActionListener(e -> {
            txtFiltreEtudiant.setText("");
            chargerEtudiants();
        });
        searchLeft.add(btnResetSearch);

        toolbar.add(searchLeft, BorderLayout.WEST);

        JButton btnRafraichir = UIUtils.creerBoutonSecondaire("🔄 Actualiser");
        btnRafraichir.addActionListener(e -> chargerEtudiants());
        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchRight.setOpaque(false);
        searchRight.add(btnRafraichir);
        toolbar.add(searchRight, BorderLayout.EAST);

        tablePanel.add(toolbar, BorderLayout.NORTH);

        // Tableau des étudiants
        String[] colonnes = {"ID", "Code Étudiant", "Nom", "Prénom", "Email universitaire", "UFR de rattachement"};
        modelEtudiants = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableEtudiants = new JTable(modelEtudiants);
        tableEtudiants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.configurerTableModerne(tableEtudiants);

        tableEtudiants.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableEtudiants.getSelectedRow() != -1) {
                int modelRow = tableEtudiants.convertRowIndexToModel(tableEtudiants.getSelectedRow());
                remplirFormulaireEtudiant(modelRow);
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableEtudiants);
        scrollTable.setBorder(UIUtils.creerBordureCarte("Registre des Étudiants Inscrits au Système"));
        tablePanel.add(scrollTable, BorderLayout.CENTER);

        // Barre d'état inférieure
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        lblNbEtudiants = new JLabel("Chargement des étudiants...");
        lblNbEtudiants.setFont(UIUtils.FONT_PETIT);
        lblNbEtudiants.setForeground(UIUtils.TEXTE_SECONDAIRE);
        statusBar.add(lblNbEtudiants, BorderLayout.WEST);
        tablePanel.add(statusBar, BorderLayout.SOUTH);

        panel.add(tablePanel, BorderLayout.CENTER);

        // Partie Droite : Formulaire latéral d'enrôlement et de gestion
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setPreferredSize(new Dimension(380, 0));
        formWrapper.setBorder(UIUtils.creerBordureCarte("Enrôlement d'un Nouvel Étudiant"));
        formWrapper.setBackground(UIUtils.BLANC);

        // Encart de rappel UFR gestionnaire
        JPanel encartUfrInfo = new JPanel(new BorderLayout());
        encartUfrInfo.setBackground(UIUtils.BADGE_BLEU_FOND);
        encartUfrInfo.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblInfoUfrGest = new JLabel("<html><b>UFR gérée :</b> UFR " + codeUfr + " (" + nomUfr + ")</html>");
        lblInfoUfrGest.setFont(UIUtils.FONT_PETIT);
        lblInfoUfrGest.setForeground(UIUtils.BADGE_BLEU_TEXT);
        encartUfrInfo.add(lblInfoUfrGest, BorderLayout.CENTER);

        formWrapper.add(encartUfrInfo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 12, 10, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEtuNom = UIUtils.creerChampTexte(16);
        txtEtuPrenom = UIUtils.creerChampTexte(16);
        txtEtuEmail = UIUtils.creerChampModerne(16, "prenom.nom@ugb.edu.sn");
        txtEtuCode = UIUtils.creerChampTexte(16);
        comboEtuUfr = new JComboBox<>();
        comboEtuUfr.setFont(UIUtils.FONT_NORMAL);

        chargerListeUfrsDansCombo();

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtEtuNom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtEtuPrenom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Email (@ugb.edu.sn) :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtEtuEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Code Étudiant :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtEtuCode, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("UFR de l'étudiant :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(comboEtuUfr, gbc);

        formWrapper.add(form, BorderLayout.CENTER);

        // Boutons d'actions
        JPanel actions = new JPanel(new GridLayout(4, 1, 6, 6));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(10, 12, 12, 12));

        btnValiderEnrolement = UIUtils.creerBoutonPrimaire("✓ Valider l'Enrôlement");
        btnValiderEnrolement.addActionListener(e -> validerEnrolementEtudiant());
        actions.add(btnValiderEnrolement);

        btnModifierEtudiant = UIUtils.creerBoutonAccent("Enregistrer les Modifications");
        btnModifierEtudiant.setEnabled(false);
        btnModifierEtudiant.addActionListener(e -> modifierEtudiantSelectionne());
        actions.add(btnModifierEtudiant);

        btnSupprimerEtudiant = UIUtils.creerBoutonDanger("Supprimer cet Étudiant");
        btnSupprimerEtudiant.setEnabled(false);
        btnSupprimerEtudiant.addActionListener(e -> supprimerEtudiantSelectionne());
        actions.add(btnSupprimerEtudiant);

        JButton btnNouveau = UIUtils.creerBoutonSecondaire("Réinitialiser / Nouvel Enrôlement");
        btnNouveau.addActionListener(e -> viderFormulaireEtudiant());
        actions.add(btnNouveau);

        formWrapper.add(actions, BorderLayout.SOUTH);

        panel.add(formWrapper, BorderLayout.EAST);
        return panel;
    }

    private void chargerListeUfrsDansCombo() {
        comboEtuUfr.removeAllItems();
        try {
            List<Ufr> ufrs = DAOFactory.getUfrDAO().listerTous();
            Ufr ufrSelection = null;
            for (Ufr u : ufrs) {
                comboEtuUfr.addItem(u);
                if (gestionnaire.getIdUfr() != null && u.getIdUfr() == gestionnaire.getIdUfr()) {
                    ufrSelection = u;
                }
            }
            if (ufrSelection != null) {
                comboEtuUfr.setSelectedItem(ufrSelection);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement UFRs : " + e.getMessage());
        }
    }

    private void remplirFormulaireEtudiant(int modelRow) {
        idEtudiantSelectionne = (int) modelEtudiants.getValueAt(modelRow, 0);
        String code = (String) modelEtudiants.getValueAt(modelRow, 1);
        String nom = (String) modelEtudiants.getValueAt(modelRow, 2);
        String prenom = (String) modelEtudiants.getValueAt(modelRow, 3);
        String email = (String) modelEtudiants.getValueAt(modelRow, 4);
        String nomUfr = (String) modelEtudiants.getValueAt(modelRow, 5);

        txtEtuNom.setText(nom);
        txtEtuPrenom.setText(prenom);
        txtEtuEmail.setText(email);
        txtEtuCode.setText(code);

        for (int i = 0; i < comboEtuUfr.getItemCount(); i++) {
            Ufr u = comboEtuUfr.getItemAt(i);
            if (nomUfr != null && (nomUfr.contains(u.getCode()) || nomUfr.contains(u.getNom()))) {
                comboEtuUfr.setSelectedIndex(i);
                break;
            }
        }

        btnModifierEtudiant.setEnabled(true);
        btnSupprimerEtudiant.setEnabled(true);
        btnValiderEnrolement.setEnabled(false);
    }

    private void viderFormulaireEtudiant() {
        idEtudiantSelectionne = 0;
        txtEtuNom.setText("");
        txtEtuPrenom.setText("");
        txtEtuEmail.setText("");
        txtEtuCode.setText("");

        // Rétablir la sélection par défaut sur l'UFR du gestionnaire
        if (gestionnaire.getIdUfr() != null) {
            for (int i = 0; i < comboEtuUfr.getItemCount(); i++) {
                Ufr u = comboEtuUfr.getItemAt(i);
                if (u.getIdUfr() == gestionnaire.getIdUfr()) {
                    comboEtuUfr.setSelectedIndex(i);
                    break;
                }
            }
        }

        tableEtudiants.clearSelection();
        btnModifierEtudiant.setEnabled(false);
        btnSupprimerEtudiant.setEnabled(false);
        btnValiderEnrolement.setEnabled(true);
    }

    private void validerEnrolementEtudiant() {
        String nom = txtEtuNom.getText().trim();
        String prenom = txtEtuPrenom.getText().trim();
        String email = txtEtuEmail.getText().trim();
        String code = txtEtuCode.getText().trim();
        Ufr ufrChoisie = (Ufr) comboEtuUfr.getSelectedItem();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom de l'étudiant est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtEtuNom.requestFocus();
            return;
        }
        if (prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom de l'étudiant est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtEtuPrenom.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "L'adresse email est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtEtuEmail.requestFocus();
            return;
        }
        try {
            AuthService.validerEmailUGB(email);
        } catch (ChampInvalideException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Format Email Non Conforme", JOptionPane.ERROR_MESSAGE);
            txtEtuEmail.requestFocus();
            return;
        }
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le code étudiant est obligatoire (ex: P28 0145).", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtEtuCode.requestFocus();
            return;
        }
        if (ufrChoisie == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner l'UFR de l'étudiant.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // RÈGLE MÉTIER CLIENT : Avertissement si le gestionnaire choisit une UFR différente de la sienne
        int idUfrGestionnaire = (gestionnaire.getIdUfr() != null ? gestionnaire.getIdUfr() : 0);
        if (ufrChoisie.getIdUfr() != idUfrGestionnaire) {
            DialogueAlerteEnrolementHorsUfr dialogAlerte = new DialogueAlerteEnrolementHorsUfr(
                    this, nom, prenom, code, ufrGestionnaire, ufrChoisie
            );
            dialogAlerte.setVisible(true);

            if (dialogAlerte.getResultat() == DialogueAlerteEnrolementHorsUfr.Resultat.CORRIGER) {
                // Auto-rectification : réinitialiser sur l'UFR du gestionnaire
                for (int i = 0; i < comboEtuUfr.getItemCount(); i++) {
                    Ufr u = comboEtuUfr.getItemAt(i);
                    if (u != null && u.getIdUfr() == idUfrGestionnaire) {
                        comboEtuUfr.setSelectedIndex(i);
                        break;
                    }
                }
                comboEtuUfr.requestFocus();
                return;
            } else if (dialogAlerte.getResultat() != DialogueAlerteEnrolementHorsUfr.Resultat.CONFIRMER) {
                comboEtuUfr.requestFocus();
                return;
            }
        }

        try {
            Etudiant nouvelEtu = utilisateurService.creerEtudiant(
                    nom, prenom, email, code, ufrChoisie.getIdUfr(), gestionnaire
            );

            JOptionPane.showMessageDialog(this,
                    "Étudiant enrôlé avec succès !\n\n"
                    + "• Nom & Prénom : " + nouvelEtu.getNomComplet() + "\n"
                    + "• Code Étudiant : " + nouvelEtu.getCodeEtudiant() + "\n"
                    + "• Email : " + nouvelEtu.getEmail() + "\n"
                    + "• UFR de rattachement : " + ufrChoisie.getCode() + " - " + ufrChoisie.getNom(),
                    "Enrôlement Réussi",
                    JOptionPane.INFORMATION_MESSAGE);

            viderFormulaireEtudiant();
            chargerEtudiants();
        } catch (ChampInvalideException | DoublonException | AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle Métier Non Respectée", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système lors de l'enrôlement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierEtudiantSelectionne() {
        if (idEtudiantSelectionne <= 0) return;

        String nom = txtEtuNom.getText().trim();
        String prenom = txtEtuPrenom.getText().trim();
        String email = txtEtuEmail.getText().trim();
        String code = txtEtuCode.getText().trim();
        Ufr ufrChoisie = (Ufr) comboEtuUfr.getSelectedItem();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || code.isEmpty() || ufrChoisie == null) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.", "Champs manquants", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AuthService.validerEmailUGB(email);
        } catch (ChampInvalideException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Format Email Invalide", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idUfrGestionnaire = (gestionnaire.getIdUfr() != null ? gestionnaire.getIdUfr() : 0);
        if (ufrChoisie.getIdUfr() != idUfrGestionnaire) {
            DialogueAlerteEnrolementHorsUfr dialogAlerte = new DialogueAlerteEnrolementHorsUfr(
                    this, nom, prenom, code, ufrGestionnaire, ufrChoisie
            );
            dialogAlerte.setVisible(true);

            if (dialogAlerte.getResultat() == DialogueAlerteEnrolementHorsUfr.Resultat.CORRIGER) {
                for (int i = 0; i < comboEtuUfr.getItemCount(); i++) {
                    Ufr u = comboEtuUfr.getItemAt(i);
                    if (u != null && u.getIdUfr() == idUfrGestionnaire) {
                        comboEtuUfr.setSelectedIndex(i);
                        break;
                    }
                }
                return;
            } else if (dialogAlerte.getResultat() != DialogueAlerteEnrolementHorsUfr.Resultat.CONFIRMER) {
                return;
            }
        }

        try {
            utilisateurService.modifierEtudiant(idEtudiantSelectionne, nom, prenom, email, code, ufrChoisie.getIdUfr(), gestionnaire);
            JOptionPane.showMessageDialog(this, "Informations de l'étudiant mises à jour avec succès !", "Mise à jour Réussie", JOptionPane.INFORMATION_MESSAGE);
            viderFormulaireEtudiant();
            chargerEtudiants();
        } catch (ChampInvalideException | DoublonException | AccesRefuseException | DocumentIntrouvableException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerEtudiantSelectionne() {
        if (idEtudiantSelectionne <= 0) return;
        String nomComplet = txtEtuPrenom.getText() + " " + txtEtuNom.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous certain de vouloir supprimer le compte de l'étudiant :\n\"" + nomComplet + "\" (Code: " + txtEtuCode.getText() + ") ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                utilisateurService.supprimerEtudiant(idEtudiantSelectionne, gestionnaire);
                JOptionPane.showMessageDialog(this, "Étudiant supprimé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                viderFormulaireEtudiant();
                chargerEtudiants();
            } catch (AccesRefuseException | DocumentIntrouvableException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerEtudiants() {
        if (modelEtudiants == null) return;
        modelEtudiants.setRowCount(0);
        try {
            List<Utilisateur> liste = utilisateurService.listerEtudiants(gestionnaire);
            for (Utilisateur u : liste) {
                modelEtudiants.addRow(new Object[]{
                        u.getIdUtilisateur(),
                        (u.getCodeEtudiant() != null ? u.getCodeEtudiant() : "—"),
                        u.getNom(),
                        u.getPrenom(),
                        u.getEmail(),
                        (u.getNomUfr() != null ? u.getNomUfr() : "—")
                });
            }
            lblNbEtudiants.setText(liste.size() + " étudiant(s) enregistré(s) dans le système.");
            if (lblKpiEtudiantsVal != null) {
                lblKpiEtudiantsVal.setText(String.valueOf(liste.size()));
            }
        } catch (Exception ex) {
            System.err.println("Erreur chargement étudiants : " + ex.getMessage());
        }
    }

    private void filtrerTableEtudiants() {
        if (modelEtudiants == null || txtFiltreEtudiant == null) return;
        String mot = txtFiltreEtudiant.getText().trim().toLowerCase();
        if (mot.isEmpty()) {
            chargerEtudiants();
            return;
        }
        modelEtudiants.setRowCount(0);
        try {
            List<Utilisateur> liste = utilisateurService.rechercherEtudiants(mot, gestionnaire);
            for (Utilisateur u : liste) {
                modelEtudiants.addRow(new Object[]{
                        u.getIdUtilisateur(),
                        (u.getCodeEtudiant() != null ? u.getCodeEtudiant() : "—"),
                        u.getNom(),
                        u.getPrenom(),
                        u.getEmail(),
                        (u.getNomUfr() != null ? u.getNomUfr() : "—")
                });
            }
            lblNbEtudiants.setText(liste.size() + " étudiant(s) correspondant(s) au filtre.");
        } catch (Exception ex) {
            System.err.println("Erreur filtrage étudiants : " + ex.getMessage());
        }
    }

    private void chargerFonds() {
        modelDocuments.setRowCount(0);
        try {
            List<Document> liste = documentService.listerPourGestionnaire(gestionnaire);
            for (Document d : liste) {
                modelDocuments.addRow(new Object[]{
                        d.getIdDocument(),
                        d.getType().name(),
                        d.getTitre(),
                        d.getAuteur(),
                        d.getEncadrant(),
                        d.getAnnee(),
                        d.getDiscipline(),
                        d.getNiveauAcces().name()
                });
            }
            lblNbDocs.setText(liste.size() + " document(s) enregistré(s) pour votre UFR.");
            if (lblKpiFondsVal != null) {
                lblKpiFondsVal.setText(String.valueOf(liste.size()));
            }
        } catch (AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Accès refusé", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrerTableDocuments() {
        String mot = txtFiltreTitre.getText().trim().toLowerCase();
        if (mot.isEmpty()) {
            chargerFonds();
            return;
        }
        modelDocuments.setRowCount(0);
        try {
            List<Document> liste = documentService.listerPourGestionnaire(gestionnaire);
            int count = 0;
            for (Document d : liste) {
                if (d.getTitre().toLowerCase().contains(mot) || d.getAuteur().toLowerCase().contains(mot)
                        || d.getMotsCles().toLowerCase().contains(mot)) {
                    modelDocuments.addRow(new Object[]{
                            d.getIdDocument(),
                            d.getType().name(),
                            d.getTitre(),
                            d.getAuteur(),
                            d.getEncadrant(),
                            d.getAnnee(),
                            d.getDiscipline(),
                            d.getNiveauAcces().name()
                    });
                    count++;
                }
            }
            lblNbDocs.setText(count + " document(s) trouvé(s) pour la recherche.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur filtrage : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerHistorique() {
        modelHistorique.setRowCount(0);
        try {
            if (gestionnaire instanceof Gestionnaire) {
                List<Telechargement> liste = documentService.consulterHistoriqueGestionnaire((Gestionnaire) gestionnaire);
                for (Telechargement t : liste) {
                    modelHistorique.addRow(new Object[]{
                            t.getIdTelechargement(),
                            t.getDateTelechargement(),
                            t.getTitreDocument(),
                            t.getNomUtilisateur(),
                            (t.getCodeEtudiant() != null ? t.getCodeEtudiant() : "—"),
                            t.getEmailUtilisateur()
                    });
                }
                if (lblKpiTelechargementsVal != null) {
                    lblKpiTelechargementsVal.setText(String.valueOf(liste.size()));
                }
            }
        } catch (Exception ex) {
            System.err.println("Erreur historique : " + ex.getMessage());
        }
    }

    private void modifierDocumentSelectionne() {
        if (documentSelectionne != null) {
            ouvrirDialogueDocument(documentSelectionne);
        }
    }

    private void supprimerDocumentSelectionne() {
        if (documentSelectionne == null) return;
        int idDoc = documentSelectionne.getIdDocument();
        String titre = documentSelectionne.getTitre();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous certain de vouloir supprimer définitivement le document :\n\"" + titre + "\" ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                documentService.supprimerDocument(idDoc, gestionnaire);
                JOptionPane.showMessageDialog(this, "Document supprimé du fonds avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                actualiserVoletApercu();
                chargerFonds();
            } catch (AccesRefuseException | DocumentIntrouvableException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ouvrirDialogueDocument(Document docExistant) {
        boolean modeEdition = (docExistant != null);
        String titreFenetre = modeEdition ? "Modifier le document #" + docExistant.getIdDocument() : "Déposer une Thèse / un Mémoire";

        JDialog dialog = new JDialog(this, titreFenetre, true);
        dialog.setSize(600, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        UIUtils.associerToucheEchap(dialog);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(UIUtils.BLANC);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtTitre = UIUtils.creerChampTexte(24);
        JTextField txtAuteur = UIUtils.creerChampTexte(24);
        JTextField txtEncadrant = UIUtils.creerChampTexte(24);
        JTextField txtAnnee = UIUtils.creerChampTexte(24);
        JComboBox<TypeDocument> comboType = new JComboBox<>(TypeDocument.values());
        comboType.setFont(UIUtils.FONT_NORMAL);
        JTextField txtDiscipline = UIUtils.creerChampTexte(24);
        JTextArea txtResume = new JTextArea(4, 24);
        txtResume.setFont(UIUtils.FONT_NORMAL);
        txtResume.setLineWrap(true);
        txtResume.setWrapStyleWord(true);
        JTextField txtMotsCles = UIUtils.creerChampTexte(24);
        JComboBox<NiveauAcces> comboAcces = new JComboBox<>(NiveauAcces.values());
        comboAcces.setFont(UIUtils.FONT_NORMAL);
        JTextField txtCheminPdf = UIUtils.creerChampTexte(18);
        JButton btnBrowse = UIUtils.creerBoutonSecondaire("Parcourir...");

        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                txtCheminPdf.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        // Pré-remplissage en mode édition
        if (modeEdition) {
            txtTitre.setText(docExistant.getTitre());
            txtAuteur.setText(docExistant.getAuteur());
            txtEncadrant.setText(docExistant.getEncadrant());
            txtAnnee.setText(String.valueOf(docExistant.getAnnee()));
            comboType.setSelectedItem(docExistant.getType());
            txtDiscipline.setText(docExistant.getDiscipline());
            txtResume.setText(docExistant.getResume());
            txtMotsCles.setText(docExistant.getMotsCles());
            comboAcces.setSelectedItem(docExistant.getNiveauAcces());
            txtCheminPdf.setText(docExistant.getCheminPdf());
        } else {
            txtAnnee.setText("2025");
            txtCheminPdf.setText("docs/nouveau_document.pdf");
        }

        int r = 0;
        g.gridx = 0; g.gridy = r; g.weightx = 0.3; form.add(new JLabel("Titre du document :"), g);
        g.gridx = 1; g.weightx = 0.7; form.add(txtTitre, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Type de document :"), g);
        g.gridx = 1; form.add(comboType, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Auteur (étudiant rédacteur) :"), g);
        g.gridx = 1; form.add(txtAuteur, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Encadrant / Directeur :"), g);
        g.gridx = 1; form.add(txtEncadrant, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Année de soutenance :"), g);
        g.gridx = 1; form.add(txtAnnee, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Discipline :"), g);
        g.gridx = 1; form.add(txtDiscipline, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Niveau d'accès :"), g);
        g.gridx = 1; form.add(comboAcces, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Résumé analytique :"), g);
        g.gridx = 1; form.add(new JScrollPane(txtResume), g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Mots-clés (séparés par virgule) :"), g);
        g.gridx = 1; form.add(txtMotsCles, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Fichier PDF :"), g);
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setOpaque(false);
        filePanel.add(txtCheminPdf, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);
        g.gridx = 1; form.add(filePanel, g);

        dialog.add(form, BorderLayout.CENTER);

        JButton btnEnregistrer = UIUtils.creerBoutonPrimaire(modeEdition ? "Mettre à jour le document" : "Enregistrer dans mon UFR");
        btnEnregistrer.addActionListener(e -> {
            try {
                int annee = Integer.parseInt(txtAnnee.getText().trim());
                Document doc = modeEdition ? docExistant : new Document();
                doc.setTitre(txtTitre.getText());
                doc.setType((TypeDocument) comboType.getSelectedItem());
                doc.setAuteur(txtAuteur.getText());
                doc.setEncadrant(txtEncadrant.getText());
                doc.setAnnee(annee);
                doc.setIdUfr(gestionnaire.getIdUfr()); // Règle d'or 4 : verrouillé à l'UFR du gestionnaire
                doc.setNomUfr(gestionnaire.getNomUfr());
                doc.setDiscipline(txtDiscipline.getText());
                doc.setResume(txtResume.getText());
                doc.setMotsCles(txtMotsCles.getText());
                doc.setNiveauAcces((NiveauAcces) comboAcces.getSelectedItem());
                doc.setCheminPdf(txtCheminPdf.getText());

                if (modeEdition) {
                    documentService.modifierDocument(doc, gestionnaire);
                    JOptionPane.showMessageDialog(dialog, "Document mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    documentService.ajouterDocument(doc, gestionnaire);
                    JOptionPane.showMessageDialog(dialog, "Document enregistré avec succès dans le fonds de votre UFR !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                chargerFonds();
                actualiserVoletApercu();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "L'année doit être un entier valide (ex: 2025).", "Format Invalide", JOptionPane.ERROR_MESSAGE);
            } catch (ChampInvalideException | AccesRefuseException | DocumentIntrouvableException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Règle Métier Non Respectée", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UIUtils.FOND_CLAIR);
        btnPanel.add(btnEnregistrer);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
