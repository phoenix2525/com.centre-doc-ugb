package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.AuthService;
import sn.ugb.centredoc.service.DemandeAccesService;
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
 * Règle 4 du sujet : un gestionnaire ne gère QUE les documents de son UFR.
 * Personnalisation forte selon l'UFR d'affectation (ex: UFR SAT).
 * Module d'enrôlement des étudiants avec alerte en cas de choix d'une autre UFR.
 */
public class GestionnaireDashboard extends JFrame {
    private final Utilisateur gestionnaire;
    private final DocumentService documentService = new DocumentService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final DemandeAccesService demandeAccesService = new DemandeAccesService();
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
    private JButton btnEnrolerEtudiant;
    private JButton btnModifierEtudiant;
    private JButton btnSupprimerEtudiant;
    private JLabel lblEtudiantSelectionne;
    private int idEtudiantSelectionne = 0;

    // Composants Onglet Historique
    private JTable tableHistorique;
    private DefaultTableModel modelHistorique;

    // Composants Onglet Demandes d'Accès (Bonus 3.3)
    private JTable tableDemandesAcces;
    private DefaultTableModel modelDemandesAcces;

    // Composants des indicateurs (KPI)
    private JLabel lblKpiFondsVal;
    private JLabel lblKpiEtudiantsVal;
    private JLabel lblKpiTelechargementsVal;
    private JLabel lblKpiDisponibiliteVal;
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
        chargerDemandesAcces();
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

        // 1. En-tête : barre supérieure + bandeau UFR + 4 indicateurs
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);

        JPanel topBar = UIUtils.creerBarreEnTete(
                "Système d'Information Doc UGB",
                "PORTAIL DES THÈSES & MÉMOIRES",
                "Gestionnaire UFR " + codeUfr,
                gestionnaire.getNomComplet(),
                DAOFactory.isUsingJdbc(),
                btnDeconnexion
        );
        headerContainer.add(topBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        // Actions rapides du bandeau
        java.util.List<JButton> actionsRapides = new java.util.ArrayList<>();

        JButton btnHeroEnroler = UIUtils.creerBoutonEmeraude("👤+ Enrôler un étudiant");
        btnHeroEnroler.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHeroEnroler.addActionListener(e -> ouvrirDialogueEnrolement(0));
        actionsRapides.add(btnHeroEnroler);

        JPanel heroBanner = UIUtils.creerBandeauAccueil(
                "CENTRE DE DOCUMENTATION — UFR " + codeUfr + " (" + nomUfr.toUpperCase() + ")",
                "Pôle d'Archivage & Thèses d'Ingénierie",
                "Pôle de gestion documentaire des Thèses et Mémoires — Université Gaston Berger de Saint-Louis.",
                actionsRapides
        );

        // Panneau des indicateurs (KPI)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblKpiFondsVal = new JLabel("0");
        lblKpiEtudiantsVal = new JLabel("0");
        lblKpiTelechargementsVal = new JLabel("0");
        lblKpiDisponibiliteVal = new JLabel("—");

        JPanel card1 = UIUtils.creerCarteIndicateur("FONDS ARCHIVÉ", lblKpiFondsVal, "Thèses & mémoires UFR", "📚", UIUtils.BLEU_UGB);
        JPanel card2 = UIUtils.creerCarteIndicateur("ÉTUDIANTS ENRÔLÉS", lblKpiEtudiantsVal, "Promotion " + codeUfr + " active", "🎓", UIUtils.VERT_SUCCES);
        JPanel card3 = UIUtils.creerCarteIndicateur("TÉLÉCHARGEMENTS", lblKpiTelechargementsVal, "Ce semestre académique", "⬇", UIUtils.OR_UGB);
        JPanel card4 = UIUtils.creerCarteIndicateur("TAUX DE DISPONIBILITÉ", lblKpiDisponibiliteVal, "Part de documents téléchargeables", "⚡", UIUtils.ACCENT_EMERAUDE);

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
        tabbedPane.addTab("📩 Demandes d'Accès (UFR " + codeUfr + ")", creerOngletDemandesAcces());

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

    /**
     * Onglet Demandes d'Accès (Bonus 3.3) : traitement des demandes des étudiants portant
     * sur les documents sous embargo de l'UFR du gestionnaire (Règle 4).
     */
    private JPanel creerOngletDemandesAcces() {
        String codeUfr = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "UFR";

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        String[] colonnes = {"ID", "Date de la demande", "Étudiant", "Email universitaire", "Code Étudiant",
                "Document demandé", "Motif", "Statut"};
        modelDemandesAcces = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDemandesAcces = new JTable(modelDemandesAcces);
        tableDemandesAcces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.configurerTableModerne(tableDemandesAcces);

        JScrollPane scroll = new JScrollPane(tableDemandesAcces);
        scroll.setBorder(UIUtils.creerBordureCarte(
                "Demandes de consultation des documents sous embargo de l'UFR " + codeUfr + " (acceptation = consultation des métadonnées et du résumé uniquement)"));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);

        JButton btnAccepter = UIUtils.creerBoutonEmeraude("✔ Accepter la demande sélectionnée");
        btnAccepter.addActionListener(e -> traiterDemandeSelectionnee(true));
        south.add(btnAccepter);

        JButton btnRefuser = UIUtils.creerBoutonDanger("✖ Refuser la demande sélectionnée");
        btnRefuser.addActionListener(e -> traiterDemandeSelectionnee(false));
        south.add(btnRefuser);

        JButton btnRafraichir = UIUtils.creerBoutonSecondaire("🔄 Actualiser les demandes");
        btnRafraichir.addActionListener(e -> chargerDemandesAcces());
        south.add(btnRafraichir);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Charge les demandes d'accès portant sur les documents de l'UFR du gestionnaire.
     */
    private void chargerDemandesAcces() {
        if (modelDemandesAcces == null) {
            return;
        }
        modelDemandesAcces.setRowCount(0);
        try {
            List<DemandeAcces> liste = demandeAccesService.listerPourGestionnaire(gestionnaire);
            for (DemandeAcces da : liste) {
                modelDemandesAcces.addRow(new Object[]{
                        da.getIdDemande(),
                        da.getDateDemande(),
                        da.getNomEtudiant(),
                        da.getEmailEtudiant(),
                        (da.getCodeEtudiant() != null ? da.getCodeEtudiant() : "—"),
                        da.getTitreDocument(),
                        da.getMotif(),
                        da.getStatut().getLibelle()
                });
            }
        } catch (AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Accès refusé", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Erreur chargement demandes d'accès : " + ex.getMessage());
        }
    }

    /**
     * Acceptation ou refus de la demande sélectionnée, après confirmation expresse.
     */
    private void traiterDemandeSelectionnee(boolean accepter) {
        int row = tableDemandesAcces.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner une demande d'accès dans le tableau.",
                    "Aucune Sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tableDemandesAcces.convertRowIndexToModel(row);
        int idDemande = (int) modelDemandesAcces.getValueAt(modelRow, 0);
        String etudiant = String.valueOf(modelDemandesAcces.getValueAt(modelRow, 2));

        int confirm = JOptionPane.showConfirmDialog(this,
                (accepter
                        ? "Accorder l'accès en consultation (métadonnées et résumé) du document sous embargo à :\n\"" + etudiant + "\" ?"
                        : "Refuser la demande d'accès de :\n\"" + etudiant + "\" ?"),
                accepter ? "Confirmation d'acceptation" : "Confirmation de refus",
                JOptionPane.YES_NO_OPTION,
                accepter ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                demandeAccesService.traiterDemande(idDemande, accepter, gestionnaire);
                JOptionPane.showMessageDialog(this,
                        accepter
                                ? "Demande acceptée : l'étudiant peut désormais consulter la fiche détaillée du document (téléchargement toujours bloqué)."
                                : "Demande refusée : l'étudiant en sera informé dans son registre de demandes.",
                        "Traitement Effectué", JOptionPane.INFORMATION_MESSAGE);
                chargerDemandesAcces();
            } catch (AccesRefuseException | DocumentIntrouvableException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
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
                selectionnerEtudiant(modelRow);
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

        // Partie Droite : Panneau d'actions (l'enrôlement s'ouvre dans une mini-fenêtre modale au centre)
        JPanel actionWrapper = new JPanel(new BorderLayout());
        actionWrapper.setPreferredSize(new Dimension(340, 0));
        actionWrapper.setBorder(UIUtils.creerBordureCarte("Gestion des Étudiants"));
        actionWrapper.setBackground(UIUtils.BLANC);

        // Encart de rappel UFR gestionnaire
        JPanel encartUfrInfo = new JPanel(new BorderLayout());
        encartUfrInfo.setBackground(UIUtils.BADGE_BLEU_FOND);
        encartUfrInfo.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblInfoUfrGest = new JLabel("<html><b>UFR gérée :</b> UFR " + codeUfr + " (" + nomUfr + ")</html>");
        lblInfoUfrGest.setFont(UIUtils.FONT_PETIT);
        lblInfoUfrGest.setForeground(UIUtils.BADGE_BLEU_TEXT);
        encartUfrInfo.add(lblInfoUfrGest, BorderLayout.CENTER);

        actionWrapper.add(encartUfrInfo, BorderLayout.NORTH);

        // Bloc central : bouton d'enrôlement + info + actions sur l'étudiant sélectionné
        JPanel blocCentral = new JPanel();
        blocCentral.setLayout(new BoxLayout(blocCentral, BoxLayout.Y_AXIS));
        blocCentral.setOpaque(false);
        blocCentral.setBorder(new EmptyBorder(16, 16, 12, 16));

        btnEnrolerEtudiant = UIUtils.creerBoutonEmeraude("👤+ Enrôler un étudiant");
        btnEnrolerEtudiant.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnrolerEtudiant.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEnrolerEtudiant.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnEnrolerEtudiant.addActionListener(e -> ouvrirDialogueEnrolement(0));
        blocCentral.add(btnEnrolerEtudiant);

        blocCentral.add(Box.createVerticalStrut(16));
        blocCentral.add(UIUtils.creerSeparateur());
        blocCentral.add(Box.createVerticalStrut(10));

        lblEtudiantSelectionne = new JLabel("<html><i>Aucun étudiant sélectionné.<br>Sélectionnez une ligne du registre pour modifier ou supprimer son compte.</i></html>");
        lblEtudiantSelectionne.setFont(UIUtils.FONT_PETIT);
        lblEtudiantSelectionne.setForeground(UIUtils.TEXTE_SECONDAIRE);
        lblEtudiantSelectionne.setAlignmentX(Component.CENTER_ALIGNMENT);
        blocCentral.add(lblEtudiantSelectionne);

        blocCentral.add(Box.createVerticalStrut(14));

        btnModifierEtudiant = UIUtils.creerBoutonAccent("✏ Modifier l'étudiant sélectionné");
        btnModifierEtudiant.setEnabled(false);
        btnModifierEtudiant.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnModifierEtudiant.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnModifierEtudiant.addActionListener(e -> modifierEtudiantSelectionne());
        blocCentral.add(btnModifierEtudiant);

        blocCentral.add(Box.createVerticalStrut(8));

        btnSupprimerEtudiant = UIUtils.creerBoutonDanger("🗑 Supprimer l'étudiant sélectionné");
        btnSupprimerEtudiant.setEnabled(false);
        btnSupprimerEtudiant.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSupprimerEtudiant.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSupprimerEtudiant.addActionListener(e -> supprimerEtudiantSelectionne());
        blocCentral.add(btnSupprimerEtudiant);

        JScrollPane scrollBloc = new JScrollPane(blocCentral, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBloc.setBorder(null);
        scrollBloc.setOpaque(false);
        scrollBloc.getViewport().setOpaque(false);
        actionWrapper.add(scrollBloc, BorderLayout.CENTER);

        panel.add(actionWrapper, BorderLayout.EAST);
        return panel;
    }

    /**
     * Ouvre la mini-fenêtre modale d'enrôlement / de modification d'un étudiant.
     * idEtudiantAModifier = 0 : mode création ; > 0 : mode édition.
     */
    private void ouvrirDialogueEnrolement(int idEtudiantAModifier) {
        boolean modeEdition = (idEtudiantAModifier > 0);
        String nom = null, prenom = null, email = null, code = null;

        if (modeEdition) {
            int row = tableEtudiants.getSelectedRow();
            if (row == -1) return;
            int modelRow = tableEtudiants.convertRowIndexToModel(row);
            code = (String) modelEtudiants.getValueAt(modelRow, 1);
            nom = (String) modelEtudiants.getValueAt(modelRow, 2);
            prenom = (String) modelEtudiants.getValueAt(modelRow, 3);
            email = (String) modelEtudiants.getValueAt(modelRow, 4);
        }

        DialogueEnrolementEtudiant dialogue = new DialogueEnrolementEtudiant(
                this, gestionnaire, utilisateurService, modeEdition, idEtudiantAModifier,
                nom, prenom, email, code
        );
        dialogue.setVisible(true);

        if (dialogue.isSucces()) {
            chargerEtudiants();
            reinitialiserSelectionEtudiant();
        }
    }

    private void selectionnerEtudiant(int modelRow) {
        idEtudiantSelectionne = (int) modelEtudiants.getValueAt(modelRow, 0);
        String code = (String) modelEtudiants.getValueAt(modelRow, 1);
        String nom = (String) modelEtudiants.getValueAt(modelRow, 2);
        String prenom = (String) modelEtudiants.getValueAt(modelRow, 3);
        String email = (String) modelEtudiants.getValueAt(modelRow, 4);

        lblEtudiantSelectionne.setText("<html><b>" + prenom + " " + nom + "</b><br>" + email
                + "<br>Code : " + code + "</html>");

        btnModifierEtudiant.setEnabled(true);
        btnSupprimerEtudiant.setEnabled(true);
    }

    private void reinitialiserSelectionEtudiant() {
        idEtudiantSelectionne = 0;
        tableEtudiants.clearSelection();
        lblEtudiantSelectionne.setText("<html><i>Aucun étudiant sélectionné.<br>Sélectionnez une ligne du registre pour modifier ou supprimer son compte.</i></html>");
        btnModifierEtudiant.setEnabled(false);
        btnSupprimerEtudiant.setEnabled(false);
    }

    private void modifierEtudiantSelectionne() {
        if (idEtudiantSelectionne <= 0) return;
        ouvrirDialogueEnrolement(idEtudiantSelectionne);
    }

    private void supprimerEtudiantSelectionne() {
        if (idEtudiantSelectionne <= 0) return;

        int row = tableEtudiants.getSelectedRow();
        String nomComplet = "cet étudiant";
        String code = "";
        if (row != -1) {
            int modelRow = tableEtudiants.convertRowIndexToModel(row);
            nomComplet = modelEtudiants.getValueAt(modelRow, 3) + " " + modelEtudiants.getValueAt(modelRow, 2);
            code = (String) modelEtudiants.getValueAt(modelRow, 1);
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous certain de vouloir supprimer le compte de l'étudiant :\n\"" + nomComplet + "\" (Code: " + code + ") ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                utilisateurService.supprimerEtudiant(idEtudiantSelectionne, gestionnaire);
                JOptionPane.showMessageDialog(this, "Étudiant supprimé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                reinitialiserSelectionEtudiant();
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
            if (lblKpiDisponibiliteVal != null) {
                int nbTelechargeables = 0;
                for (Document d : liste) {
                    if (d.isTelechargeable()) {
                        nbTelechargeables++;
                    }
                }
                int taux = liste.isEmpty() ? 0 : (int) Math.round(nbTelechargeables * 100.0 / liste.size());
                lblKpiDisponibiliteVal.setText(taux + "%");
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

        JScrollPane scrollDialog = new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDialog.setBorder(null);
        dialog.add(scrollDialog, BorderLayout.CENTER);

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
                doc.setIdUfr(gestionnaire.getIdUfr()); // Règle 4 : verrouillé à l'UFR du gestionnaire
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
                    JOptionPane.showMessageDialog(dialog,
                            "Document enregistré avec succès dans le fonds de votre UFR !\n"
                            + "🔔 Les étudiants de votre UFR seront notifiés de cette nouvelle publication.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
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
