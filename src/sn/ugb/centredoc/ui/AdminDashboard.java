package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.Role;
import sn.ugb.centredoc.model.Ufr;
import sn.ugb.centredoc.model.Utilisateur;
import sn.ugb.centredoc.service.StatistiqueService;
import sn.ugb.centredoc.service.UtilisateurService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Tableau de bord Administrateur (Module 1 : Gestion des Utilisateurs).
 * Permet de :
 * - Créer, modifier, supprimer des comptes gestionnaires
 * - Affecter les gestionnaires à une UFR
 * - Afficher et rechercher les utilisateurs par nom ou email (recherche dynamique en direct)
 * - Différencier les rôles (ADMIN, GESTIONNAIRE, ETUDIANT)
 * - Consulter les statistiques globales et graphiques visuels 2D (Modules 3.2 & 3.3)
 */
public class AdminDashboard extends JFrame {
    private final Utilisateur utilisateurCourant;
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final StatistiqueService statistiqueService = new StatistiqueService();

    // Composants Onglet Gestionnaires
    private JTable tableGestionnaires;
    private DefaultTableModel modelGestionnaires;
    private JTextField txtNomG;
    private JTextField txtPrenomG;
    private JTextField txtEmailG;
    private JPasswordField txtPassG;
    private JComboBox<Ufr> comboUfrG;
    private int idGestionnaireSelectionne = 0;

    // Composants Onglet Annuaire
    private JTable tableUtilisateurs;
    private DefaultTableModel modelUtilisateurs;
    private JTextField txtRechercheUser;
    private JComboBox<String> comboFiltreRole;

    // Composants Onglet Statistiques
    private JLabel lblTotalDocs;
    private JLabel lblTotalUsers;
    private JLabel lblTotalTelechargements;
    private JTable tableStatsUfr;
    private DefaultTableModel modelStatsUfr;
    private SimpleChartPanel chartUfr;
    private SimpleChartPanel chartDisciplines;

    public AdminDashboard(Utilisateur admin) {
        super("Espace Administrateur — SI Centres de Documentation UGB");
        this.utilisateurCourant = admin;
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 740);
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
        JPanel topBar = UIUtils.creerBarreEnTete(
                "Système d'Information Doc UGB",
                "GOUVERNANCE CENTRALE & MONITORING",
                "Supervision Admin",
                utilisateurCourant.getNomComplet(),
                DAOFactory.isUsingJdbc(),
                btnDeconnexion
        );
        add(topBar, BorderLayout.NORTH);

        // Onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        tabbedPane.addTab("Gestion des Gestionnaires", creerOngletGestionnaires());
        tabbedPane.addTab("Annuaire & Recherche", creerOngletAnnuaire());
        tabbedPane.addTab("Statistiques & Export", creerOngletStatistiques());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerOngletGestionnaires() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Tableau des gestionnaires
        String[] colonnes = {"ID", "Nom", "Prénom", "Email (@ugb.edu.sn)", "UFR Affectée"};
        modelGestionnaires = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableGestionnaires = new JTable(modelGestionnaires);
        tableGestionnaires.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.configurerTableModerne(tableGestionnaires);

        tableGestionnaires.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableGestionnaires.getSelectedRow() != -1) {
                int modelRow = tableGestionnaires.convertRowIndexToModel(tableGestionnaires.getSelectedRow());
                remplirFormulaireGestionnaire(modelRow);
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableGestionnaires);
        scrollTable.setBorder(UIUtils.creerBordureCarte("Liste des Gestionnaires de Centre"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Formulaire latéral d'édition / création
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setPreferredSize(new Dimension(400, 0));
        formWrapper.setBorder(UIUtils.creerBordureCarte("Fiche Gestionnaire"));
        formWrapper.setBackground(UIUtils.BLANC);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNomG = UIUtils.creerChampTexte(15);
        txtPrenomG = UIUtils.creerChampTexte(15);
        txtEmailG = UIUtils.creerChampTexte(15);
        txtPassG = UIUtils.creerChampMotDePasse(15);
        comboUfrG = new JComboBox<>();
        comboUfrG.setFont(UIUtils.FONT_NORMAL);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; form.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; form.add(txtNomG, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; form.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; form.add(txtPrenomG, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; form.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; form.add(txtEmailG, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; form.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; form.add(txtPassG, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; form.add(new JLabel("UFR :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; form.add(comboUfrG, gbc);

        JScrollPane scrollForm = new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        formWrapper.add(scrollForm, BorderLayout.CENTER);

        // Boutons d'actions
        JPanel actions = new JPanel(new GridLayout(4, 1, 6, 6));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnNouveau = UIUtils.creerBoutonSecondaire("Réinitialiser / Nouveau");
        btnNouveau.addActionListener(e -> viderFormulaireGestionnaire());
        actions.add(btnNouveau);

        JButton btnAjouter = UIUtils.creerBoutonPrimaire("+ Ajouter ce gestionnaire");
        btnAjouter.addActionListener(e -> ajouterGestionnaire());
        actions.add(btnAjouter);

        JButton btnModifier = UIUtils.creerBoutonAccent("Enregistrer les modifications");
        btnModifier.addActionListener(e -> modifierGestionnaire());
        actions.add(btnModifier);

        JButton btnSupprimer = UIUtils.creerBoutonDanger("Supprimer ce compte");
        btnSupprimer.addActionListener(e -> supprimerGestionnaire());
        actions.add(btnSupprimer);

        formWrapper.add(actions, BorderLayout.SOUTH);
        panel.add(formWrapper, BorderLayout.EAST);

        return panel;
    }

    private JPanel creerOngletAnnuaire() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Barre de recherche en direct et filtrage par rôle
        JPanel filtrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filtrePanel.setBackground(UIUtils.BLANC);
        filtrePanel.setBorder(new EmptyBorder(6, 12, 6, 12));

        filtrePanel.add(new JLabel("Rechercher en direct (Nom/Email) :"));
        txtRechercheUser = UIUtils.creerChampTexte(18);
        filtrePanel.add(txtRechercheUser);

        // Recherche dynamique à la frappe
        txtRechercheUser.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { chargerUtilisateurs(); }
            @Override public void removeUpdate(DocumentEvent e) { chargerUtilisateurs(); }
            @Override public void changedUpdate(DocumentEvent e) { chargerUtilisateurs(); }
        });

        filtrePanel.add(new JLabel("Rôle :"));
        comboFiltreRole = new JComboBox<>(new String[]{"TOUS", "ADMIN", "GESTIONNAIRE", "ETUDIANT"});
        comboFiltreRole.setFont(UIUtils.FONT_NORMAL);
        comboFiltreRole.addActionListener(e -> chargerUtilisateurs());
        filtrePanel.add(comboFiltreRole);

        JButton btnReset = UIUtils.creerBoutonSecondaire("Réinitialiser");
        btnReset.addActionListener(e -> {
            txtRechercheUser.setText("");
            comboFiltreRole.setSelectedIndex(0);
            chargerUtilisateurs();
        });
        filtrePanel.add(btnReset);

        JButton btnNouvelEtudiant = UIUtils.creerBoutonAccent("+ Inscrire un Etudiant");
        btnNouvelEtudiant.addActionListener(e -> ouvrirDialogueAjoutEtudiant());
        filtrePanel.add(btnNouvelEtudiant);

        panel.add(filtrePanel, BorderLayout.NORTH);

        // Table des utilisateurs
        String[] colonnes = {"ID", "Nom Complet", "Email (@ugb.edu.sn)", "Rôle", "UFR", "Code Étudiant"};
        modelUtilisateurs = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableUtilisateurs = new JTable(modelUtilisateurs);
        UIUtils.configurerTableModerne(tableUtilisateurs);

        JScrollPane scroll = new JScrollPane(tableUtilisateurs);
        scroll.setBorder(UIUtils.creerBordureCarte("Utilisateurs enregistrés dans le système"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerOngletStatistiques() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIUtils.FOND_CLAIR);

        // Cartes résumé en haut
        JPanel cartesPanel = new JPanel(new GridLayout(1, 3, 16, 16));
        cartesPanel.setOpaque(false);

        lblTotalDocs = new JLabel("0", SwingConstants.CENTER);
        lblTotalUsers = new JLabel("0", SwingConstants.CENTER);
        lblTotalTelechargements = new JLabel("0", SwingConstants.CENTER);

        cartesPanel.add(UIUtils.creerCarteStatAvecAccent("📚 Fonds Documentaire", lblTotalDocs, UIUtils.BLEU_UGB));
        cartesPanel.add(UIUtils.creerCarteStatAvecAccent("👥 Utilisateurs Inscrits", lblTotalUsers, UIUtils.ACCENT_EMERAUDE));
        cartesPanel.add(UIUtils.creerCarteStatAvecAccent("⬇️ Téléchargements Totaux", lblTotalTelechargements, UIUtils.OR_UGB));

        panel.add(cartesPanel, BorderLayout.NORTH);

        // Panneau central : Graphiques 2D & Tableaux
        JPanel centreStats = new JPanel(new GridLayout(1, 2, 14, 14));
        centreStats.setOpaque(false);

        // Graphique 1 : Activité par UFR (barres horizontales)
        chartUfr = new SimpleChartPanel("📈 Téléchargements par UFR", SimpleChartPanel.TypeGraphique.BARRES_HORIZONTALES);
        centreStats.add(chartUfr);

        // Graphique 2 : Répartition par Discipline (donut chart)
        chartDisciplines = new SimpleChartPanel("🎯 Répartition du Fonds par Discipline", SimpleChartPanel.TypeGraphique.ANNEAU_CAMEMBERT);
        centreStats.add(chartDisciplines);

        panel.add(centreStats, BorderLayout.CENTER);

        // Barre d'export et actualisation
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setOpaque(false);

        JButton btnRefresh = UIUtils.creerBoutonSecondaire("🔄 Actualiser les statistiques");
        btnRefresh.addActionListener(e -> chargerStatistiques());
        south.add(btnRefresh);

        JButton btnExport = UIUtils.creerBoutonPrimaire("📄 Exporter le Rapport Complet en CSV");
        btnExport.addActionListener(e -> exporterRapportCSV());
        south.add(btnExport);

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerDonnees() {
        chargerUfrs();
        chargerGestionnaires();
        chargerUtilisateurs();
        chargerStatistiques();
    }

    private void chargerUfrs() {
        comboUfrG.removeAllItems();
        try {
            List<Ufr> liste = utilisateurService.listerUfrs();
            for (Ufr u : liste) {
                comboUfrG.addItem(u);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement UFR : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerGestionnaires() {
        modelGestionnaires.setRowCount(0);
        try {
            List<Utilisateur> liste = utilisateurService.listerTous(utilisateurCourant);
            for (Utilisateur u : liste) {
                if (u.getRole() == Role.GESTIONNAIRE) {
                    modelGestionnaires.addRow(new Object[]{
                            u.getIdUtilisateur(),
                            u.getNom(),
                            u.getPrenom(),
                            u.getEmail(),
                            (u.getNomUfr() != null ? u.getNomUfr() : "Non affecté")
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerUtilisateurs() {
        modelUtilisateurs.setRowCount(0);
        String filtre = (txtRechercheUser != null) ? txtRechercheUser.getText() : "";
        String roleChoisi = (comboFiltreRole != null) ? (String) comboFiltreRole.getSelectedItem() : "TOUS";

        try {
            List<Utilisateur> liste = (filtre != null && !filtre.trim().isEmpty())
                    ? utilisateurService.rechercher(filtre, utilisateurCourant)
                    : utilisateurService.listerTous(utilisateurCourant);

            for (Utilisateur u : liste) {
                if ("TOUS".equals(roleChoisi) || u.getRole().name().equals(roleChoisi)) {
                    modelUtilisateurs.addRow(new Object[]{
                            u.getIdUtilisateur(),
                            u.getNomComplet(),
                            u.getEmail(),
                            u.getRole().name(),
                            (u.getNomUfr() != null ? u.getNomUfr() : "—"),
                            (u.getCodeEtudiant() != null ? u.getCodeEtudiant() : "—")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur annuaire : " + e.getMessage());
        }
    }

    private void chargerStatistiques() {
        try {
            Map<String, Integer> global = statistiqueService.getStatistiquesGlobales();
            lblTotalDocs.setText(String.valueOf(global.getOrDefault("Total Documents", 0)));
            lblTotalUsers.setText(String.valueOf(global.getOrDefault("Total Utilisateurs", 0)));
            lblTotalTelechargements.setText(String.valueOf(global.getOrDefault("Total Téléchargements", 0)));

            Map<String, Integer> parUfr = statistiqueService.getActiviteParUfr();
            if (chartUfr != null) {
                chartUfr.setDonnees(parUfr);
            }

            Map<String, Integer> parDiscipline = statistiqueService.getRepartitionParDiscipline();
            if (chartDisciplines != null) {
                chartDisciplines.setDonnees(parDiscipline);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement stats : " + e.getMessage());
        }
    }

    private void remplirFormulaireGestionnaire(int row) {
        if (row < 0 || row >= modelGestionnaires.getRowCount()) return;
        idGestionnaireSelectionne = (int) modelGestionnaires.getValueAt(row, 0);
        txtNomG.setText((String) modelGestionnaires.getValueAt(row, 1));
        txtPrenomG.setText((String) modelGestionnaires.getValueAt(row, 2));
        txtEmailG.setText((String) modelGestionnaires.getValueAt(row, 3));
        txtPassG.setText("");

        String nomUfr = (String) modelGestionnaires.getValueAt(row, 4);
        for (int i = 0; i < comboUfrG.getItemCount(); i++) {
            Ufr u = comboUfrG.getItemAt(i);
            if (u.getNom().equalsIgnoreCase(nomUfr)) {
                comboUfrG.setSelectedIndex(i);
                break;
            }
        }
    }

    private void viderFormulaireGestionnaire() {
        idGestionnaireSelectionne = 0;
        tableGestionnaires.clearSelection();
        txtNomG.setText("");
        txtPrenomG.setText("");
        txtEmailG.setText("");
        txtPassG.setText("");
        if (comboUfrG.getItemCount() > 0) comboUfrG.setSelectedIndex(0);
    }

    private void ajouterGestionnaire() {
        Ufr ufr = (Ufr) comboUfrG.getSelectedItem();
        if (ufr == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une UFR.", "Champ Invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            utilisateurService.creerGestionnaire(
                    txtNomG.getText(),
                    txtPrenomG.getText(),
                    txtEmailG.getText(),
                    new String(txtPassG.getPassword()),
                    ufr.getIdUfr(),
                    utilisateurCourant
            );

            JOptionPane.showMessageDialog(this, "Gestionnaire ajouté avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            viderFormulaireGestionnaire();
            chargerGestionnaires();
            chargerUtilisateurs();
            chargerStatistiques();
        } catch (ChampInvalideException | DoublonException | AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle Métier Non Respectée", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur inattendue : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierGestionnaire() {
        if (idGestionnaireSelectionne <= 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un gestionnaire dans le tableau.", "Sélection Requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Ufr ufr = (Ufr) comboUfrG.getSelectedItem();
        if (ufr == null) return;

        try {
            utilisateurService.modifierGestionnaire(
                    idGestionnaireSelectionne,
                    txtNomG.getText(),
                    txtPrenomG.getText(),
                    txtEmailG.getText(),
                    new String(txtPassG.getPassword()),
                    ufr.getIdUfr(),
                    utilisateurCourant
            );

            JOptionPane.showMessageDialog(this, "Gestionnaire modifié avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            viderFormulaireGestionnaire();
            chargerGestionnaires();
            chargerUtilisateurs();
        } catch (ChampInvalideException | DoublonException | DocumentIntrouvableException | AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur inattendue : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerGestionnaire() {
        if (idGestionnaireSelectionne <= 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un gestionnaire à supprimer.", "Sélection Requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choix = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce compte gestionnaire ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choix == JOptionPane.YES_OPTION) {
            try {
                utilisateurService.supprimerGestionnaire(idGestionnaireSelectionne, utilisateurCourant);
                JOptionPane.showMessageDialog(this, "Gestionnaire supprimé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                viderFormulaireGestionnaire();
                chargerGestionnaires();
                chargerUtilisateurs();
                chargerStatistiques();
            } catch (DocumentIntrouvableException | AccesRefuseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur Métier", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ouvrirDialogueAjoutEtudiant() {
        JDialog dialog = new JDialog(this, "Inscrire un nouvel étudiant", true);
        dialog.setSize(440, 390);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        UIUtils.associerToucheEchap(dialog);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        form.setBackground(UIUtils.BLANC);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNom = UIUtils.creerChampTexte(15);
        JTextField txtPrenom = UIUtils.creerChampTexte(15);
        JTextField txtEmail = UIUtils.creerChampTexte(15);
        JTextField txtCode = UIUtils.creerChampTexte(15);
        JComboBox<Ufr> comboUfr = new JComboBox<>();
        comboUfr.setFont(UIUtils.FONT_NORMAL);

        for (int i = 0; i < comboUfrG.getItemCount(); i++) comboUfr.addItem(comboUfrG.getItemAt(i));

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Nom :"), g);
        g.gridx = 1; form.add(txtNom, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Prénom :"), g);
        g.gridx = 1; form.add(txtPrenom, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Email (@ugb.edu.sn) :"), g);
        g.gridx = 1; form.add(txtEmail, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Code Étudiant :"), g);
        g.gridx = 1; form.add(txtCode, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("UFR :"), g);
        g.gridx = 1; form.add(comboUfr, g);

        dialog.add(form, BorderLayout.CENTER);

        JButton btnValider = UIUtils.creerBoutonPrimaire("Enregistrer l'étudiant");
        btnValider.addActionListener(e -> {
            try {
                Ufr u = (Ufr) comboUfr.getSelectedItem();
                utilisateurService.creerEtudiant(
                        txtNom.getText(),
                        txtPrenom.getText(),
                        txtEmail.getText(),
                        txtCode.getText(),
                        (u != null ? u.getIdUfr() : null),
                        utilisateurCourant
                );
                JOptionPane.showMessageDialog(dialog, "Étudiant inscrit avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                chargerUtilisateurs();
                chargerStatistiques();
            } catch (ChampInvalideException | DoublonException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Erreur de validation", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UIUtils.FOND_CLAIR);
        btnPanel.add(btnValider);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void exporterRapportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Rapport_Centres_Doc_UGB.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File fichier = chooser.getSelectedFile();
                statistiqueService.exporterStatistiquesCSV(fichier);
                JOptionPane.showMessageDialog(this,
                        "Le rapport statistique a été exporté avec succès vers :\n" + fichier.getAbsolutePath(),
                        "Export Réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'export : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
