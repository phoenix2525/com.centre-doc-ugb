package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.AccesRefuseException;
import sn.ugb.centredoc.exception.DocumentIntrouvableException;
import sn.ugb.centredoc.exception.DoublonException;
import sn.ugb.centredoc.exception.ChampInvalideException;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.DemandeAccesService;
import sn.ugb.centredoc.service.DocumentService;
import sn.ugb.centredoc.service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Tableau de bord Étudiant (Module 3 : Recherche, Consultation & Téléchargement).
 * Règles strictes respectées :
 * 1. Téléchargement autorisé uniquement si niveau d'accès == TELECHARGEABLE.
 * 2. CONSULTATION_SEULE expose uniquement les métadonnées et le résumé (téléchargement bloqué).
 * 3. RESTREINT (embargo) strictement invisible et inaccessible pour l'étudiant.
 * 4. Historique personnel des téléchargements.
 * Interface de type maître-détail : liste des résultats à gauche, fiche détaillée à droite,
 * avec recherche actualisée à chaque frappe.
 */
public class EtudiantDashboard extends JFrame {
    private final Etudiant etudiant;
    private final DocumentService documentService = new DocumentService();
    private final DemandeAccesService demandeAccesService = new DemandeAccesService();

    // Critères de recherche multicritère
    private JTextField txtTitre;
    private JTextField txtAuteur;
    private JTextField txtMotCle;
    private JTextField txtAnnee;
    private JComboBox<String> comboUfr;
    private JTextField txtDiscipline;
    private JLabel lblNbResultats;

    // Tableau de résultats (Côté gauche)
    private JTable tableResultats;
    private DefaultTableModel modelResultats;

    // Volet de prévisualisation latérale (Côté droit)
    private JPanel panelApercu;
    private JLabel lblApercuTitre;
    private JLabel lblApercuType;
    private JLabel lblApercuAuteur;
    private JLabel lblApercuEncadrant;
    private JLabel lblApercuAnneeDiscipline;
    private JLabel lblApercuUfr;
    private JLabel lblApercuAcces;
    private JTextArea txtApercuResume;
    private JLabel lblApercuMotsCles;
    private JButton btnApercuTelecharger;
    private JLabel lblApercuAvertissement;
    private Document documentSelectionne = null;

    // Tableau historique (Onglet 2)
    private JTable tableHistorique;
    private DefaultTableModel modelHistorique;

    // Formulaire & registre des demandes d'accès aux documents sous embargo (Bonus 3.3)
    private JTextField txtDemandeTitre;
    private JTextArea txtDemandeMotif;
    private JTable tableDemandes;
    private DefaultTableModel modelDemandes;

    // Panneau de notifications visuelles (Bonus 3.3 : nouveautés de l'UFR de l'étudiant)
    private JPanel panelNouveautes;
    private JLabel lblNouveautes;

    private List<Ufr> listeUfrs;

    public EtudiantDashboard(Etudiant etudiant) {
        super("Catalogue Académique — Université Gaston Berger (Espace Étudiant)");
        this.etudiant = etudiant;
        initUI();
        chargerUfrs();
        lancerRecherche();
        chargerHistorique();
        chargerDemandes();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 800);
        setMinimumSize(new Dimension(1040, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.FOND_CLAIR);

        JButton btnDeconnexion = UIUtils.creerBoutonDanger("⏻ Déconnexion");
        btnDeconnexion.setFont(UIUtils.FONT_PETIT);
        btnDeconnexion.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        // Barre d'en-tête
        String infoEtu = etudiant.getNomComplet() + " (" + etudiant.getCodeEtudiant() + ")";
        JPanel topBar = UIUtils.creerBarreEnTete(
                "Système d'Information Doc UGB",
                "CATALOGUE DES THÈSES & MÉMOIRES",
                "Catalogue Étudiant",
                infoEtu,
                DAOFactory.isUsingJdbc(),
                btnDeconnexion
        );
        add(topBar, BorderLayout.NORTH);

        // Onglets principaux
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        tabbedPane.addTab("Recherche & Consultation", creerOngletRecherche());
        tabbedPane.addTab("Demandes d'Accès (Embargo)", creerOngletDemandes());
        tabbedPane.addTab("Historique des telechargements", creerOngletHistorique());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerOngletRecherche() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Panneau supérieur : Moteur de recherche multicritère
        JPanel searchBox = new JPanel(new GridBagLayout());
        searchBox.setBackground(UIUtils.BLANC);
        searchBox.setBorder(UIUtils.creerBordureCarte("Moteur de Recherche Multicritère en Direct"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtTitre = UIUtils.creerChampTexte(13);
        txtAuteur = UIUtils.creerChampTexte(13);
        txtMotCle = UIUtils.creerChampTexte(13);
        txtAnnee = UIUtils.creerChampTexte(6);
        comboUfr = new JComboBox<>();
        comboUfr.setFont(UIUtils.FONT_NORMAL);
        txtDiscipline = UIUtils.creerChampTexte(13);

        // Recherche en direct à la frappe
        DocumentListener liveSearch = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { lancerRecherche(); }
            @Override public void removeUpdate(DocumentEvent e) { lancerRecherche(); }
            @Override public void changedUpdate(DocumentEvent e) { lancerRecherche(); }
        };

        txtTitre.getDocument().addDocumentListener(liveSearch);
        txtAuteur.getDocument().addDocumentListener(liveSearch);
        txtMotCle.getDocument().addDocumentListener(liveSearch);
        txtAnnee.getDocument().addDocumentListener(liveSearch);
        txtDiscipline.getDocument().addDocumentListener(liveSearch);
        comboUfr.addActionListener(e -> lancerRecherche());

        // Ligne 1
        g.gridx = 0; g.gridy = 0; searchBox.add(new JLabel("Titre :"), g);
        g.gridx = 1; searchBox.add(txtTitre, g);
        g.gridx = 2; searchBox.add(new JLabel("Auteur :"), g);
        g.gridx = 3; searchBox.add(txtAuteur, g);
        g.gridx = 4; searchBox.add(new JLabel("Mot-clé :"), g);
        g.gridx = 5; searchBox.add(txtMotCle, g);

        // Ligne 2
        g.gridx = 0; g.gridy = 1; searchBox.add(new JLabel("Année :"), g);
        g.gridx = 1; searchBox.add(txtAnnee, g);
        g.gridx = 2; searchBox.add(new JLabel("UFR :"), g);
        g.gridx = 3; searchBox.add(comboUfr, g);
        g.gridx = 4; searchBox.add(new JLabel("Discipline :"), g);
        g.gridx = 5; searchBox.add(txtDiscipline, g);

        // Boutons
        JPanel btnSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnSearchPanel.setOpaque(false);

        JButton btnReset = UIUtils.creerBoutonSecondaire("Réinitialiser les filtres");
        btnReset.addActionListener(e -> resetFiltres());
        btnSearchPanel.add(btnReset);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 6;
        searchBox.add(btnSearchPanel, g);

        panel.add(searchBox, BorderLayout.NORTH);

        // Panneau de notifications visuelles (Bonus 3.3) : nouveautés publiées dans l'UFR de l'étudiant
        panelNouveautes = new JPanel(new BorderLayout());
        panelNouveautes.setBackground(new Color(236, 253, 245));
        panelNouveautes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(5, 150, 105)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        lblNouveautes = new JLabel("");
        lblNouveautes.setFont(UIUtils.FONT_NORMAL);
        lblNouveautes.setForeground(UIUtils.TEXTE_FONCE);
        lblNouveautes.setVerticalAlignment(SwingConstants.TOP);
        panelNouveautes.add(lblNouveautes, BorderLayout.CENTER);
        panelNouveautes.setVisible(false);

        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(panelNouveautes);
        northStack.add(searchBox);
        panel.add(northStack, BorderLayout.NORTH);

        // Tableau des résultats (Côté gauche du SplitPane)
        String[] colonnes = {"ID", "Type", "Titre du document", "Auteur", "Encadrant", "Année", "UFR", "Discipline", "Accès"};
        modelResultats = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableResultats = new JTable(modelResultats);
        tableResultats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.configurerTableModerne(tableResultats);

        tableResultats.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualiserVoletApercu();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableResultats);
        scrollTable.setBorder(UIUtils.creerBordureCarte("Résultats du catalogue (Cliquez pour afficher la fiche)"));

        // Volet de prévisualisation latérale (Côté droit du SplitPane)
        panelApercu = creerVoletApercu();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTable, panelApercu);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);

        // Barre d'état
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        lblNbResultats = new JLabel("Recherche en cours...");
        lblNbResultats.setFont(UIUtils.FONT_PETIT);
        lblNbResultats.setForeground(UIUtils.TEXTE_SECONDAIRE);
        statusBar.add(lblNbResultats, BorderLayout.WEST);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerVoletApercu() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIUtils.BLANC);
        wrapper.setBorder(UIUtils.creerBordureCarte("Fiche Descriptive Detaillee"));
        wrapper.setPreferredSize(new Dimension(400, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BLANC);
        content.setBorder(new EmptyBorder(14, 16, 12, 16));

        lblApercuTitre = new JLabel("Selectionnez un document");
        lblApercuTitre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblApercuTitre.setForeground(UIUtils.BLEU_UGB);
        lblApercuTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuTitre);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.creerSeparateur());
        content.add(Box.createVerticalStrut(8));

        content.add(UIUtils.creerLabelSection("Type & Auteur"));
        lblApercuType = new JLabel("Type : --");
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

        content.add(UIUtils.creerLabelSection("Details"));
        lblApercuAnneeDiscipline = new JLabel("-- | --");
        lblApercuAnneeDiscipline.setFont(UIUtils.FONT_NORMAL);
        lblApercuAnneeDiscipline.setForeground(UIUtils.TEXTE_FONCE);
        lblApercuAnneeDiscipline.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuAnneeDiscipline);
        content.add(Box.createVerticalStrut(3));

        lblApercuUfr = new JLabel("UFR : --");
        lblApercuUfr.setFont(UIUtils.FONT_NORMAL);
        lblApercuUfr.setForeground(UIUtils.TEXTE_FONCE);
        lblApercuUfr.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblApercuUfr);
        content.add(Box.createVerticalStrut(8));
        content.add(UIUtils.creerSeparateur());
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

        content.add(UIUtils.creerLabelSection("Resume du document"));
        txtApercuResume = new JTextArea("Veuillez selectionner un document dans le tableau de gauche.", 7, 26);
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

        // Zone de telechargement
        JPanel bottomActions = new JPanel(new BorderLayout(0, 6));
        bottomActions.setBackground(UIUtils.BLANC);
        bottomActions.setBorder(new EmptyBorder(8, 14, 14, 14));

        lblApercuAvertissement = new JLabel("", SwingConstants.CENTER);
        lblApercuAvertissement.setFont(UIUtils.FONT_PETIT);
        bottomActions.add(lblApercuAvertissement, BorderLayout.NORTH);

        btnApercuTelecharger = UIUtils.creerBoutonPrimaire("Telecharger le document PDF");
        btnApercuTelecharger.setEnabled(false);
        btnApercuTelecharger.addActionListener(e -> {
            if (documentSelectionne != null) {
                telechargerDocumentParId(documentSelectionne.getIdDocument());
            }
        });
        bottomActions.add(btnApercuTelecharger, BorderLayout.CENTER);

        wrapper.add(bottomActions, BorderLayout.SOUTH);
        return wrapper;
    }

    private void actualiserVoletApercu() {
        int row = tableResultats.getSelectedRow();
        if (row == -1) {
            documentSelectionne = null;
            lblApercuTitre.setText("Sélectionnez un document");
            lblApercuType.setText("Type : —");
            lblApercuAuteur.setText("Auteur : —");
            lblApercuEncadrant.setText("Directeur : —");
            lblApercuAnneeDiscipline.setText("Année / Discipline : —");
            lblApercuUfr.setText("UFR : —");
            lblApercuAcces.setText("Niveau d'accès : —");
            txtApercuResume.setText("Veuillez sélectionner un document dans le tableau de gauche pour consulter sa fiche.");
            lblApercuMotsCles.setText("Mots-clés : —");
            lblApercuAvertissement.setText("");
            btnApercuTelecharger.setEnabled(false);
            btnApercuTelecharger.setText("Telecharger le document PDF");
            return;
        }

        int modelRow = tableResultats.convertRowIndexToModel(row);
        int idDoc = (int) modelResultats.getValueAt(modelRow, 0);

        try {
            documentSelectionne = documentService.consulterFicheDetaillee(idDoc, etudiant);
            lblApercuTitre.setText("<html><b>" + documentSelectionne.getTitre() + "</b></html>");
            lblApercuType.setText("Type : " + documentSelectionne.getType());
            lblApercuAuteur.setText("Auteur : " + documentSelectionne.getAuteur());
            lblApercuEncadrant.setText("Directeur : " + documentSelectionne.getEncadrant());
            lblApercuAnneeDiscipline.setText(documentSelectionne.getAnnee() + " | " + documentSelectionne.getDiscipline());
            lblApercuUfr.setText("UFR : " + documentSelectionne.getNomUfr());
            lblApercuAcces.setText("Niveau : " + documentSelectionne.getNiveauAcces().getLibelle());
            txtApercuResume.setText(documentSelectionne.getResume());
            txtApercuResume.setCaretPosition(0);
            lblApercuMotsCles.setText("Mots-clés : " + documentSelectionne.getMotsCles());

            // Règles 1 et 2 : bouton actif UNIQUEMENT si TELECHARGEABLE
            if (documentSelectionne.isTelechargeable()) {
                btnApercuTelecharger.setEnabled(true);
                btnApercuTelecharger.setText("⬇️ Télécharger le document PDF (" + documentSelectionne.getType() + ")");
                lblApercuAvertissement.setText("<html><b style='color:#0f6832;'>✔ Document disponible au téléchargement immédiat</b></html>");
            } else {
                btnApercuTelecharger.setEnabled(false);
                btnApercuTelecharger.setText("🚫 Téléchargement bloqué (Consultation Seule)");
                lblApercuAvertissement.setText("<html><b style='color:#b05000;'>⚠️ Règle métier : Seules les métadonnées et le résumé sont consultables</b></html>");
            }
        } catch (Exception ex) {
            System.err.println("Erreur aperçu étudiant : " + ex.getMessage());
        }
    }

    private JPanel creerOngletHistorique() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        String[] cols = {"ID", "Date & Heure de téléchargement", "Titre du document", "UFR"};
        modelHistorique = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHistorique = new JTable(modelHistorique);
        UIUtils.configurerTableModerne(tableHistorique);

        JScrollPane scroll = new JScrollPane(tableHistorique);
        scroll.setBorder(UIUtils.creerBordureCarte("Vos téléchargements enregistrés"));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        JButton btnRefresh = UIUtils.creerBoutonSecondaire("🔄 Rafraîchir mon historique");
        btnRefresh.addActionListener(e -> chargerHistorique());
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerUfrs() {
        comboUfr.removeAllItems();
        comboUfr.addItem("Toutes les UFR");
        try {
            listeUfrs = DAOFactory.getUfrDAO().listerTous();
            for (Ufr u : listeUfrs) {
                comboUfr.addItem(u.getCode() + " - " + u.getNom());
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement UFR : " + e.getMessage());
        }
    }

    private void resetFiltres() {
        txtTitre.setText("");
        txtAuteur.setText("");
        txtMotCle.setText("");
        txtAnnee.setText("");
        comboUfr.setSelectedIndex(0);
        txtDiscipline.setText("");
        lancerRecherche();
    }

    private void lancerRecherche() {
        actualiserNotifications();
        modelResultats.setRowCount(0);
        String titre = (txtTitre != null) ? txtTitre.getText() : "";
        String auteur = (txtAuteur != null) ? txtAuteur.getText() : "";
        String motCle = (txtMotCle != null) ? txtMotCle.getText() : "";
        String discipline = (txtDiscipline != null) ? txtDiscipline.getText() : "";

        Integer annee = null;
        if (txtAnnee != null && !txtAnnee.getText().trim().isEmpty()) {
            try {
                annee = Integer.parseInt(txtAnnee.getText().trim());
            } catch (NumberFormatException ignored) {}
        }

        Integer idUfr = null;
        if (comboUfr != null && comboUfr.getSelectedIndex() > 0 && listeUfrs != null) {
            idUfr = listeUfrs.get(comboUfr.getSelectedIndex() - 1).getIdUfr();
        }

        try {
            // Règle 3 : le service filtre et exclut les documents RESTREINT pour les étudiants
            List<Document> docs = documentService.rechercherPourEtudiant(titre, auteur, motCle, annee, idUfr, discipline);
            for (Document d : docs) {
                modelResultats.addRow(new Object[]{
                        d.getIdDocument(),
                        d.getType().name(),
                        d.getTitre(),
                        d.getAuteur(),
                        d.getEncadrant(),
                        d.getAnnee(),
                        d.getNomUfr(),
                        d.getDiscipline(),
                        d.getNiveauAcces().name()
                });
            }
            if (lblNbResultats != null) {
                lblNbResultats.setText(docs.size() + " document(s) accessible(s) trouvé(s).");
            }
        } catch (Exception e) {
            System.err.println("Erreur recherche étudiant : " + e.getMessage());
        }
    }

    private void chargerHistorique() {
        modelHistorique.setRowCount(0);
        try {
            List<Telechargement> liste = documentService.consulterHistoriqueEtudiant(etudiant);
            for (Telechargement t : liste) {
                modelHistorique.addRow(new Object[]{
                        t.getIdTelechargement(),
                        t.getDateTelechargement(),
                        t.getTitreDocument(),
                        t.getUfrDocument()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur historique étudiant : " + e.getMessage());
        }
    }

    /**
     * Actualise le panneau de notifications visuelles (Bonus 3.3) :
     * nouveaux documents publiés dans l'UFR de l'étudiant au cours des 7 derniers jours.
     */
    private void actualiserNotifications() {
        if (panelNouveautes == null || lblNouveautes == null) {
            return;
        }
        Integer idUfrEtu = etudiant.getIdUfr();
        if (idUfrEtu == null || idUfrEtu <= 0) {
            panelNouveautes.setVisible(false);
            return;
        }
        try {
            List<Document> nouveautes = NotificationService.getInstance().listerNouveautes(idUfrEtu);
            if (nouveautes.isEmpty()) {
                panelNouveautes.setVisible(false);
                return;
            }
            StringBuilder sb = new StringBuilder("<html><b>🔔 " + nouveautes.size()
                    + " nouveau(x) document(s) publié(s) dans votre UFR (" + etudiant.getNomUfr() + ") :</b><br>");
            int affiches = 0;
            for (Document d : nouveautes) {
                if (affiches >= 3) {
                    sb.append("&nbsp;&nbsp;… et ").append(nouveautes.size() - affiches).append(" autre(s) dans le catalogue.<br>");
                    break;
                }
                sb.append("&nbsp;&nbsp;• ").append(d.getTitre())
                        .append(" (").append(d.getType()).append(", ").append(d.getAnnee()).append(")<br>");
                affiches++;
            }
            sb.append("<span style='color:#0f6832;'>Consultez le catalogue ci-dessous pour les explorer et les télécharger.</span></html>");
            lblNouveautes.setText(sb.toString());
            panelNouveautes.setVisible(true);
        } catch (Exception e) {
            panelNouveautes.setVisible(false);
            System.err.println("Erreur notifications étudiant : " + e.getMessage());
        }
    }

    /**
     * Onglet Demandes d'Accès (Bonus 3.3) : formulaire de demande pour un document
     * sous embargo + registre personnel des demandes soumises et de leur statut.
     */
    private JPanel creerOngletDemandes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Partie haute : Formulaire de demande d'accès
        JPanel formBox = new JPanel(new BorderLayout(0, 8));
        formBox.setBackground(UIUtils.BLANC);
        formBox.setBorder(UIUtils.creerBordureCarte("Formulaire de Demande d'Accès — Document sous Embargo (RESTREINT)"));

        JPanel formInner = new JPanel(new BorderLayout(10, 10));
        formInner.setOpaque(false);
        formInner.setBorder(new EmptyBorder(4, 12, 10, 12));

        JLabel lblInfo = new JLabel("<html>Les documents sous embargo sont invisibles dans le catalogue (Règle métier 3).<br>"
                + "Saisissez la référence exacte (titre) du document ainsi que le motif de votre demande :<br>"
                + "le gestionnaire du centre concerné validera ou refusera votre requête.</html>");
        lblInfo.setFont(UIUtils.FONT_PETIT);
        lblInfo.setForeground(UIUtils.TEXTE_SECONDAIRE);
        formInner.add(lblInfo, BorderLayout.NORTH);

        JPanel champs = new JPanel(new GridBagLayout());
        champs.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtDemandeTitre = UIUtils.creerChampTexte(26);
        txtDemandeMotif = new JTextArea(3, 26);
        txtDemandeMotif.setFont(UIUtils.FONT_NORMAL);
        txtDemandeMotif.setLineWrap(true);
        txtDemandeMotif.setWrapStyleWord(true);
        txtDemandeMotif.setBackground(new Color(248, 250, 252));
        txtDemandeMotif.setBorder(new EmptyBorder(6, 8, 6, 8));

        g.gridx = 0; g.gridy = 0; g.weightx = 0.25; champs.add(new JLabel("Référence (titre) :"), g);
        g.gridx = 1; g.weightx = 0.75; champs.add(txtDemandeTitre, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0.25; champs.add(new JLabel("Motif de la demande :"), g);
        g.gridx = 1; g.weightx = 0.75; champs.add(new JScrollPane(txtDemandeMotif), g);

        formInner.add(champs, BorderLayout.CENTER);

        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        formActions.setOpaque(false);
        JButton btnSoumettre = UIUtils.creerBoutonAccent("📤 Soumettre la demande d'accès");
        btnSoumettre.addActionListener(e -> soumettreDemande());
        formActions.add(btnSoumettre);
        formInner.add(formActions, BorderLayout.SOUTH);

        formBox.add(formInner, BorderLayout.CENTER);
        panel.add(formBox, BorderLayout.NORTH);

        // Partie centrale : Registre des demandes soumises
        String[] cols = {"ID", "Date de la demande", "Document demandé", "UFR", "Statut", "Motif"};
        modelDemandes = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDemandes = new JTable(modelDemandes);
        UIUtils.configurerTableModerne(tableDemandes);

        JScrollPane scroll = new JScrollPane(tableDemandes);
        scroll.setBorder(UIUtils.creerBordureCarte("Mes demandes d'accès et leur statut de traitement"));
        panel.add(scroll, BorderLayout.CENTER);

        // Partie basse : Actions
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);

        JButton btnConsulter = UIUtils.creerBoutonPrimaire("📖 Consulter la fiche autorisée");
        btnConsulter.setEnabled(false);
        btnConsulter.addActionListener(e -> consulterFicheAccordee());
        tableDemandes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableDemandes.getSelectedRow();
                if (row == -1) {
                    btnConsulter.setEnabled(false);
                } else {
                    int modelRow = tableDemandes.convertRowIndexToModel(row);
                    Object statut = modelDemandes.getValueAt(modelRow, 4);
                    btnConsulter.setEnabled(StatutDemande.ACCEPTEE.getLibelle().equals(statut));
                }
            }
        });
        south.add(btnConsulter);

        JButton btnRefresh = UIUtils.creerBoutonSecondaire("🔄 Actualiser mes demandes");
        btnRefresh.addActionListener(e -> chargerDemandes());
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Soumission du formulaire de demande d'accès (avec rattrapage des exceptions métier).
     */
    private void soumettreDemande() {
        String titre = txtDemandeTitre.getText();
        String motif = txtDemandeMotif.getText();
        try {
            demandeAccesService.creerDemande(etudiant, titre, motif);
            JOptionPane.showMessageDialog(this,
                    "Demande d'accès soumise avec succès !\nElle est maintenant en attente de validation par le gestionnaire du centre.",
                    "Demande Enregistrée", JOptionPane.INFORMATION_MESSAGE);
            txtDemandeTitre.setText("");
            txtDemandeMotif.setText("");
            chargerDemandes();
        } catch (ChampInvalideException | DoublonException | DocumentIntrouvableException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle Métier Non Respectée", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Consultation de la fiche d'un document sous embargo dont la demande a été ACCEPTEE.
     * Règles 1 et 3 : le téléchargement du PDF reste formellement bloqué.
     */
    private void consulterFicheAccordee() {
        int row = tableDemandes.getSelectedRow();
        if (row == -1) {
            return;
        }
        int modelRow = tableDemandes.convertRowIndexToModel(row);
        int idDemande = (int) modelDemandes.getValueAt(modelRow, 0);

        try {
            DemandeAcces cible = null;
            for (DemandeAcces da : demandeAccesService.listerPourEtudiant(etudiant)) {
                if (da.getIdDemande() == idDemande) {
                    cible = da;
                    break;
                }
            }
            if (cible == null || !cible.isAcceptee()) {
                JOptionPane.showMessageDialog(this,
                        "L'accès n'a pas encore été accordé pour cette demande.",
                        "Accès Non Accordé", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Document doc = documentService.consulterFicheDetaillee(cible.getIdDocument(), etudiant);
            String fiche = "<html><body style='width:440px;'>"
                    + "<h2 style='color:#1D4ED8;'>" + doc.getTitre() + "</h2>"
                    + "<p><b>Auteur :</b> " + doc.getAuteur() + "<br>"
                    + "<b>Encadrant :</b> " + doc.getEncadrant() + "<br>"
                    + "<b>Année / Discipline :</b> " + doc.getAnnee() + " | " + doc.getDiscipline() + "<br>"
                    + "<b>UFR :</b> " + doc.getNomUfr() + "<br>"
                    + "<b>Niveau :</b> " + doc.getNiveauAcces().getLibelle() + "</p>"
                    + "<p><b>Résumé :</b><br>" + doc.getResume() + "</p>"
                    + "<p style='color:#b05000;'><b>⚠️ Accès accordé en consultation uniquement : "
                    + "le téléchargement du PDF reste bloqué (document sous embargo).</b></p>"
                    + "</body></html>";
            JOptionPane.showMessageDialog(this, fiche, "Fiche du document (Accès accordé)", JOptionPane.INFORMATION_MESSAGE);
        } catch (AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Règle Métier : Accès Refusé", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Charge le registre personnel des demandes d'accès de l'étudiant.
     */
    private void chargerDemandes() {
        if (modelDemandes == null) {
            return;
        }
        modelDemandes.setRowCount(0);
        try {
            List<DemandeAcces> liste = demandeAccesService.listerPourEtudiant(etudiant);
            for (DemandeAcces da : liste) {
                modelDemandes.addRow(new Object[]{
                        da.getIdDemande(),
                        da.getDateDemande(),
                        da.getTitreDocument(),
                        da.getUfrDocument(),
                        da.getStatut().getLibelle(),
                        da.getMotif()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement demandes : " + e.getMessage());
        }
    }

    private void telechargerDocumentParId(int idDoc) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir le dossier d'enregistrement du PDF");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dossier = chooser.getSelectedFile();
            try {
                // Application stricte de la Règle 1 : lève AccesRefuseException si CONSULTATION_SEULE ou RESTREINT
                File pdfTelecharge = documentService.telechargerDocument(idDoc, etudiant, dossier);

                JOptionPane.showMessageDialog(this,
                        "Téléchargement réussi avec succès !\nFichier enregistré sous :\n" + pdfTelecharge.getAbsolutePath(),
                        "Téléchargement Terminé", JOptionPane.INFORMATION_MESSAGE);

                chargerHistorique();

            } catch (AccesRefuseException ex) {
                // Rattrapage obligatoire de l'exception métier selon la charte du cours
                JOptionPane.showMessageDialog(this,
                        "❌ " + ex.getMessage(),
                        "Règle Métier : Accès Refusé", JOptionPane.WARNING_MESSAGE);

            } catch (DocumentIntrouvableException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Document Introuvable", JOptionPane.ERROR_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du téléchargement : " + ex.getMessage(),
                        "Erreur Système", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
