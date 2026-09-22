package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.exception.*;
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
 * Tableau de bord Gestionnaire de Centre de Documentation (Module 2 : Gestion du Fonds Documentaire).
 * Règle d'or 4 : Un gestionnaire ne gère QUE les documents de son UFR.
 * Interface moderne Master-Detail (Split-Pane) avec prévisualisation latérale en direct.
 */
public class GestionnaireDashboard extends JFrame {
    private final Utilisateur gestionnaire;
    private final DocumentService documentService = new DocumentService();

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

    // Composants Onglet Historique
    private JTable tableHistorique;
    private DefaultTableModel modelHistorique;

    public GestionnaireDashboard(Utilisateur gestionnaire) {
        super("Espace Gestionnaire — Centre de Documentation UGB (" + 
              (gestionnaire.getNomUfr() != null ? gestionnaire.getNomUfr() : "UFR " + gestionnaire.getIdUfr()) + ")");
        this.gestionnaire = gestionnaire;
        initUI();
        chargerFonds();
        chargerHistorique();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.FOND_CLAIR);

        // Bandeau supérieur
        String sousTitre = "Centre de documentation — UFR : " + 
                (gestionnaire.getNomUfr() != null ? gestionnaire.getNomUfr() : "UFR " + gestionnaire.getIdUfr());
        JPanel bandeau = UIUtils.creerBandeauHaut(
                "Fonds Documentaire — Thèses et Mémoires",
                sousTitre,
                gestionnaire.getNomComplet() + " (Gestionnaire)"
        );

        JButton btnDeconnexion = UIUtils.creerBoutonSecondaire("Déconnexion");
        btnDeconnexion.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        east.setOpaque(false);
        east.add(btnDeconnexion);
        bandeau.add(east, BorderLayout.EAST);

        add(bandeau, BorderLayout.NORTH);

        // Onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        tabbedPane.addTab("📚 Thèses et Mémoires de l'UFR", creerOngletFonds());
        tabbedPane.addTab("📥 Historique des Téléchargements UFR", creerOngletHistorique());

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

        JButton btnAjouter = UIUtils.creerBoutonAccent("➕ Déposer une Thèse / un Mémoire");
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
        scrollTable.setBorder(UIUtils.creerBordureCarte("Documents de votre UFR"));

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
        content.setBorder(new EmptyBorder(12, 14, 12, 14));

        lblApercuTitre = new JLabel("Sélectionnez un document");
        lblApercuTitre.setFont(UIUtils.FONT_SOUS_TITRE);
        lblApercuTitre.setForeground(UIUtils.BLEU_UGB);
        content.add(lblApercuTitre);
        content.add(Box.createVerticalStrut(8));

        lblApercuType = new JLabel("—");
        lblApercuType.setFont(UIUtils.FONT_NORMAL);
        content.add(lblApercuType);
        content.add(Box.createVerticalStrut(4));

        lblApercuAuteur = new JLabel("Auteur : —");
        lblApercuAuteur.setFont(UIUtils.FONT_NORMAL);
        content.add(lblApercuAuteur);
        content.add(Box.createVerticalStrut(4));

        lblApercuEncadrant = new JLabel("Directeur : —");
        lblApercuEncadrant.setFont(UIUtils.FONT_NORMAL);
        content.add(lblApercuEncadrant);
        content.add(Box.createVerticalStrut(4));

        lblApercuAnneeDiscipline = new JLabel("Année / Discipline : —");
        lblApercuAnneeDiscipline.setFont(UIUtils.FONT_NORMAL);
        content.add(lblApercuAnneeDiscipline);
        content.add(Box.createVerticalStrut(4));

        lblApercuAcces = new JLabel("Niveau d'accès : —");
        lblApercuAcces.setFont(UIUtils.FONT_NORMAL);
        content.add(lblApercuAcces);
        content.add(Box.createVerticalStrut(10));

        JLabel lblResTitre = new JLabel("Résumé : ");
        lblResTitre.setFont(UIUtils.FONT_GRAS);
        content.add(lblResTitre);
        content.add(Box.createVerticalStrut(4));

        txtApercuResume = new JTextArea("Aucun document sélectionné.", 7, 24);
        txtApercuResume.setFont(UIUtils.FONT_NORMAL);
        txtApercuResume.setForeground(UIUtils.TEXTE_FONCE);
        txtApercuResume.setDisabledTextColor(UIUtils.TEXTE_FONCE);
        txtApercuResume.setLineWrap(true);
        txtApercuResume.setWrapStyleWord(true);
        txtApercuResume.setEditable(false);
        txtApercuResume.setBackground(new Color(248, 250, 252));
        txtApercuResume.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane scrollRes = new JScrollPane(txtApercuResume);
        content.add(scrollRes);
        content.add(Box.createVerticalStrut(8));

        lblApercuMotsCles = new JLabel("Mots-clés : —");
        lblApercuMotsCles.setFont(UIUtils.FONT_PETIT);
        lblApercuMotsCles.setForeground(UIUtils.TEXTE_SECONDAIRE);
        content.add(lblApercuMotsCles);

        wrapper.add(content, BorderLayout.CENTER);

        // Boutons d'action sur le document sélectionné
        JPanel bottomActions = new JPanel(new GridLayout(2, 1, 6, 6));
        bottomActions.setBackground(UIUtils.BLANC);
        bottomActions.setBorder(new EmptyBorder(8, 12, 12, 12));

        btnApercuModifier = UIUtils.creerBoutonAccent("✏️ Modifier ce document");
        btnApercuModifier.setEnabled(false);
        btnApercuModifier.addActionListener(e -> {
            if (documentSelectionne != null) ouvrirDialogueDocument(documentSelectionne);
        });
        bottomActions.add(btnApercuModifier);

        btnApercuSupprimer = UIUtils.creerBoutonDanger("🗑️ Supprimer du fonds");
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
