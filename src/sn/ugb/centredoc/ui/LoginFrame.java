package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.dao.DBConnection;
import sn.ugb.centredoc.exception.AuthentificationException;
import sn.ugb.centredoc.exception.ChampInvalideException;
import sn.ugb.centredoc.model.Etudiant;
import sn.ugb.centredoc.model.Role;
import sn.ugb.centredoc.model.Utilisateur;
import sn.ugb.centredoc.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Écran d'authentification du Système d'Information des Centres de Documentation (UGB).
 * Distingue clairement les flux :
 * - Étudiant : sans mot de passe (Prénom, Nom, Email UGB, Code Étudiant)
 * - Personnel (Admin / Gestionnaire) : Email UGB et Mot de passe
 */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    // Champs onglet Étudiant
    private JTextField txtEtuPrenom;
    private JTextField txtEtuNom;
    private JTextField txtEtuEmail;
    private JTextField txtEtuCode;

    // Champs onglet Personnel
    private JTextField txtPersoEmail;
    private JPasswordField txtPersoPass;

    private JLabel lblStatutBD;

    public LoginFrame() {
        super("UGB — Système d'Information des Centres de Documentation");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.FOND_CLAIR);

        // Bandeau supérieur
        JPanel bandeau = UIUtils.creerBandeauHaut(
                "Centres de Documentation de l'UGB",
                "Gestion informatisée des thèses et mémoires par UFR (L3 Informatique / MIAGE)",
                null
        );
        add(bandeau, BorderLayout.NORTH);

        // Panneau central avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SOUS_TITRE);

        tabbedPane.addTab("🎓 Espace Étudiant (Sans mot de passe)", creerOngletEtudiant());
        tabbedPane.addTab("🔑 Personnel & Administration", creerOngletPersonnel());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(new EmptyBorder(16, 20, 10, 20));
        centerWrapper.setOpaque(false);
        centerWrapper.add(tabbedPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // Bandeau inférieur : état de la base de données
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(UIUtils.BLANC);
        footer.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblStatutBD = new JLabel(getTexteStatutBD());
        lblStatutBD.setFont(UIUtils.FONT_PETIT);
        lblStatutBD.setForeground(UIUtils.TEXTE_FONCE);
        footer.add(lblStatutBD, BorderLayout.WEST);

        JPanel boutonsFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boutonsFooter.setOpaque(false);

        JButton btnConfigDB = UIUtils.creerBoutonSecondaire("⚙️ Configurer BDD");
        btnConfigDB.setFont(UIUtils.FONT_PETIT);
        btnConfigDB.addActionListener(e -> ouvrirDialogueConfigBDD());
        boutonsFooter.add(btnConfigDB);

        JButton btnTester = UIUtils.creerBoutonSecondaire("🔄 Tester MySQL");
        btnTester.setFont(UIUtils.FONT_PETIT);
        btnTester.addActionListener(e -> testerConnexionMySQL(true));
        boutonsFooter.add(btnTester);

        footer.add(boutonsFooter, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private String getTexteStatutBD() {
        if (DAOFactory.isUsingJdbc()) {
            return "🟢 Source active : MySQL JDBC ('" + DBConnection.getDatabase() + "' sur " + DBConnection.getHost() + ":" + DBConnection.getPort() + ")";
        } else {
            return "🔵 Source active : Mode Mémoire Java Collections (Démonstration autonome)";
        }
    }

    private void testerConnexionMySQL(boolean afficherDialogue) {
        boolean dispo = DBConnection.isMySQLAvailable();
        DAOFactory.setUseJdbc(dispo);
        lblStatutBD.setText(getTexteStatutBD());
        if (afficherDialogue) {
            if (dispo) {
                JOptionPane.showMessageDialog(this,
                        "Connexion réussie au serveur MySQL local sur le port " + DBConnection.getPort() + ".\n" +
                        "L'application est maintenant connectée via JDBC.",
                        "MySQL Détecté et Opérationnel", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Le serveur MySQL n'a pas répondu sur " + DBConnection.getHost() + ":" + DBConnection.getPort() + ".\n" +
                        "L'application continue de fonctionner en Mode Mémoire (Collections Java du cours).",
                        "Serveur MySQL Hors-ligne", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void ouvrirDialogueConfigBDD() {
        JDialog dialog = new JDialog(this, "Configuration de la Base de Données MySQL", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        UIUtils.associerToucheEchap(dialog);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        form.setBackground(UIUtils.BLANC);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtHost = UIUtils.creerChampTexte(15);
        txtHost.setText(DBConnection.getHost());
        JTextField txtPort = UIUtils.creerChampTexte(15);
        txtPort.setText(String.valueOf(DBConnection.getPort()));
        JTextField txtDatabase = UIUtils.creerChampTexte(15);
        txtDatabase.setText(DBConnection.getDatabase());
        JTextField txtUser = UIUtils.creerChampTexte(15);
        txtUser.setText(DBConnection.getUser());
        JPasswordField txtPass = UIUtils.creerChampMotDePasse(15);
        txtPass.setText(DBConnection.getPassword());

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Hôte (Host) :"), g);
        g.gridx = 1; form.add(txtHost, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Port :"), g);
        g.gridx = 1; form.add(txtPort, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Nom de la base :"), g);
        g.gridx = 1; form.add(txtDatabase, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Utilisateur :"), g);
        g.gridx = 1; form.add(txtUser, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Mot de passe :"), g);
        g.gridx = 1; form.add(txtPass, g);

        dialog.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        actions.setBackground(UIUtils.FOND_CLAIR);

        JButton btnInitSql = UIUtils.creerBoutonAccent("⚡ Initialiser BDD (centre_doc.sql)");
        btnInitSql.addActionListener(e -> {
            try {
                int p = Integer.parseInt(txtPort.getText().trim());
                DBConnection.setCredentials(txtHost.getText().trim(), p, txtDatabase.getText().trim(),
                        txtUser.getText().trim(), new String(txtPass.getPassword()));
                File sqlFile = new File("centre_doc.sql");
                if (!sqlFile.exists()) {
                    sqlFile = new File("../centre_doc.sql");
                }
                DBConnection.initialiserBaseDepuisScript(sqlFile);
                DAOFactory.setUseJdbc(true);
                lblStatutBD.setText(getTexteStatutBD());
                JOptionPane.showMessageDialog(dialog, "Base de données 'centre_doc' initialisée et peuplée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Échec d'initialisation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(btnInitSql);

        JButton btnSauvegarder = UIUtils.creerBoutonPrimaire("Appliquer & Tester");
        btnSauvegarder.addActionListener(e -> {
            try {
                int p = Integer.parseInt(txtPort.getText().trim());
                DBConnection.setCredentials(txtHost.getText().trim(), p, txtDatabase.getText().trim(),
                        txtUser.getText().trim(), new String(txtPass.getPassword()));
                testerConnexionMySQL(true);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Port invalide : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(btnSauvegarder);

        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel creerOngletEtudiant() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BLANC);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEtuPrenom = UIUtils.creerChampTexte(20);
        txtEtuNom = UIUtils.creerChampTexte(20);
        txtEtuEmail = UIUtils.creerChampTexte(20);
        txtEtuCode = UIUtils.creerChampTexte(20);

        // Validation par touche Entrée sur tous les champs
        Runnable actionEntree = this::tenterConnexionEtudiant;
        UIUtils.associerToucheEntree(txtEtuPrenom, actionEntree);
        UIUtils.associerToucheEntree(txtEtuNom, actionEntree);
        UIUtils.associerToucheEntree(txtEtuEmail, actionEntree);
        UIUtils.associerToucheEntree(txtEtuCode, actionEntree);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtEtuPrenom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtEtuNom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Email institutionnel :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtEtuEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Code Étudiant :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtEtuCode, gbc);

        panel.add(form, BorderLayout.CENTER);

        // Bouton de connexion et raccourcis démo
        JPanel actions = new JPanel(new GridLayout(2, 1, 10, 10));
        actions.setOpaque(false);

        JButton btnConnexion = UIUtils.creerBoutonPrimaire("Accéder au catalogue des thèses et mémoires");
        btnConnexion.addActionListener(e -> tenterConnexionEtudiant());
        actions.add(btnConnexion);

        JPanel raccourcis = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        raccourcis.setOpaque(false);
        JLabel lblDemoEtu = new JLabel("Raccourcis Démo : ");
        lblDemoEtu.setFont(UIUtils.FONT_PETIT);
        lblDemoEtu.setForeground(UIUtils.TEXTE_SECONDAIRE);
        raccourcis.add(lblDemoEtu);

        JButton btnDemo1 = UIUtils.creerBoutonSecondaire("Ibrahima SOW (SAT)");
        btnDemo1.setFont(UIUtils.FONT_PETIT);
        btnDemo1.addActionListener(e -> {
            txtEtuPrenom.setText("Ibrahima");
            txtEtuNom.setText("SOW");
            txtEtuEmail.setText("ibrahima.sow@ugb.edu.sn");
            txtEtuCode.setText("P28 0145");
        });
        raccourcis.add(btnDemo1);

        JButton btnDemo2 = UIUtils.creerBoutonSecondaire("Fatou NDIAYE (SEG)");
        btnDemo2.setFont(UIUtils.FONT_PETIT);
        btnDemo2.addActionListener(e -> {
            txtEtuPrenom.setText("Fatou");
            txtEtuNom.setText("NDIAYE");
            txtEtuEmail.setText("fatou.ndiaye@ugb.edu.sn");
            txtEtuCode.setText("P29 0089");
        });
        raccourcis.add(btnDemo2);

        actions.add(raccourcis);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerOngletPersonnel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BLANC);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPersoEmail = UIUtils.creerChampTexte(20);
        txtPersoPass = UIUtils.creerChampMotDePasse(20);

        Runnable actionEntree = this::tenterConnexionPersonnel;
        UIUtils.associerToucheEntree(txtPersoEmail, actionEntree);
        UIUtils.associerToucheEntree(txtPersoPass, actionEntree);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Email (@ugb.edu.sn) :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtPersoEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(txtPersoPass, gbc);

        panel.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 1, 10, 10));
        actions.setOpaque(false);

        JButton btnConnexion = UIUtils.creerBoutonPrimaire("Se connecter à l'espace de gestion");
        btnConnexion.addActionListener(e -> tenterConnexionPersonnel());
        actions.add(btnConnexion);

        JPanel raccourcis = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        raccourcis.setOpaque(false);
        JLabel lblDemoPerso = new JLabel("Raccourcis Démo : ");
        lblDemoPerso.setFont(UIUtils.FONT_PETIT);
        lblDemoPerso.setForeground(UIUtils.TEXTE_SECONDAIRE);
        raccourcis.add(lblDemoPerso);

        JButton btnAdmin = UIUtils.creerBoutonSecondaire("Admin");
        btnAdmin.setFont(UIUtils.FONT_PETIT);
        btnAdmin.addActionListener(e -> {
            txtPersoEmail.setText("admin@ugb.edu.sn");
            txtPersoPass.setText("admin123");
        });
        raccourcis.add(btnAdmin);

        JButton btnGestSAT = UIUtils.creerBoutonSecondaire("Gest. SAT");
        btnGestSAT.setFont(UIUtils.FONT_PETIT);
        btnGestSAT.addActionListener(e -> {
            txtPersoEmail.setText("moussa.diakhame@ugb.edu.sn");
            txtPersoPass.setText("pass123");
        });
        raccourcis.add(btnGestSAT);

        JButton btnGestSEG = UIUtils.creerBoutonSecondaire("Gest. SEG");
        btnGestSEG.setFont(UIUtils.FONT_PETIT);
        btnGestSEG.addActionListener(e -> {
            txtPersoEmail.setText("aminata.diallo@ugb.edu.sn");
            txtPersoPass.setText("pass123");
        });
        raccourcis.add(btnGestSEG);

        actions.add(raccourcis);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void tenterConnexionEtudiant() {
        String prenom = txtEtuPrenom.getText();
        String nom = txtEtuNom.getText();
        String email = txtEtuEmail.getText();
        String code = txtEtuCode.getText();

        try {
            Etudiant etu = authService.authentifierEtudiant(prenom, nom, email, code);
            JOptionPane.showMessageDialog(this,
                    "Bienvenue, " + etu.getNomComplet() + " !\nAccès au catalogue accordé.",
                    "Connexion réussie", JOptionPane.INFORMATION_MESSAGE);

            new EtudiantDashboard(etu).setVisible(true);
            this.dispose();

        } catch (ChampInvalideException | AuthentificationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur d'authentification", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur inattendue : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tenterConnexionPersonnel() {
        String email = txtPersoEmail.getText();
        String pass = new String(txtPersoPass.getPassword());

        try {
            Utilisateur user = authService.authentifierPersonnel(email, pass);
            JOptionPane.showMessageDialog(this,
                    "Bienvenue, " + user.getNomComplet() + " (" + user.getRole() + ")",
                    "Connexion réussie", JOptionPane.INFORMATION_MESSAGE);

            if (user.getRole() == Role.ADMIN) {
                new AdminDashboard(user).setVisible(true);
            } else if (user.getRole() == Role.GESTIONNAIRE) {
                new GestionnaireDashboard(user).setVisible(true);
            }
            this.dispose();

        } catch (ChampInvalideException | AuthentificationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur d'authentification", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur inattendue : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
