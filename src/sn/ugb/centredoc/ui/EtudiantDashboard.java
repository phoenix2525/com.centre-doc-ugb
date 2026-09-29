package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.AccesRefuseException;
import sn.ugb.centredoc.exception.DocumentIntrouvableException;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.DocumentService;

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
 * Ergonomie : Interface Master-Detail (Split-Pane) moderne avec prévisualisation et recherche en direct.
 */
public class EtudiantDashboard extends JFrame {
    private final Etudiant etudiant;
    private final DocumentService documentService = new DocumentService();

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

    private List<Ufr> listeUfrs;

    public EtudiantDashboard(Etudiant etudiant) {
        super("Catalogue Académique — Université Gaston Berger (Espace Étudiant)");
        this.etudiant = etudiant;
        initUI();
        chargerUfrs();
        lancerRecherche();
        chargerHistorique();
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

        // TopBar Stitch
        String infoEtu = etudiant.getNomComplet() + " (" + etudiant.getCodeEtudiant() + ")";
        JPanel topBar = UIUtils.creerTopBarStitch(
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

        // Recherche en direct à la frappe (Accessibilité & Ergonomie moderne)
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

            // Règle d'or 1 & 2 : Bouton actif UNIQUEMENT si TELECHARGEABLE
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
