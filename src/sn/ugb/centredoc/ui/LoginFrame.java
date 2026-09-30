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

/**
 * Fenêtre de connexion de l'application (écran d'accueil).
 * Présente deux cartes côte à côte : l'accès étudiant sans mot de passe
 * et l'espace réservé au personnel universitaire et à l'administration.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    // Champs Carte Étudiant
    private JTextField txtEtuPrenom;
    private JTextField txtEtuNom;
    private JTextField txtEtuEmail;
    private JTextField txtEtuCode;

    // Champs Carte Personnel
    private JTextField txtPersoEmail;
    private JPasswordField txtPersoPass;

    private JLabel lblStatutBD;

    public LoginFrame() {
        super("UGB Docs — Portail Numérique des Thèses & Mémoires");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 790);
        setMinimumSize(new Dimension(980, 720));
        setLocationRelativeTo(null);

        // Fond de la fenêtre : dégradé subtil du bleu très clair vers le blanc
        JPanel backgroundPanel = new UIUtils.PanelDegrade(
                new Color(248, 249, 255),
                new Color(238, 244, 255),
                true
        );
        backgroundPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(backgroundPanel);

        // =====================================================================
        // 1. Barre institutionnelle supérieure
        // =====================================================================
        JPanel topUtilityBar = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIUtils.BORDURE_DOUCE);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        topUtilityBar.setBackground(Color.WHITE);
        topUtilityBar.setBorder(new EmptyBorder(8, 24, 8, 24));
        topUtilityBar.setPreferredSize(new Dimension(0, 52));

        // Gauche : Logo + Titre app
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBrand.setOpaque(false);

        LogoUGB logoMini = new LogoUGB(34, 34);
        leftBrand.add(logoMini);

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 1));
        brandText.setOpaque(false);

        JLabel lblBrandTitle = new JLabel("UGB Docs  v2.4");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrandTitle.setForeground(UIUtils.TEXTE_FONCE);

        JLabel lblBrandSub = new JLabel("UNIVERSITÉ GASTON BERGER DE SAINT-LOUIS");
        lblBrandSub.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBrandSub.setForeground(UIUtils.TEXTE_GRIS);

        brandText.add(lblBrandTitle);
        brandText.add(lblBrandSub);
        leftBrand.add(brandText);
        topUtilityBar.add(leftBrand, BorderLayout.WEST);

        // Droite : Mention officielle ministérielle
        JPanel rightMinistry = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightMinistry.setOpaque(false);

        JLabel lblMinistry = new JLabel("🇸🇳 RÉPUBLIQUE DU SÉNÉGAL • MESRI");
        lblMinistry.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMinistry.setForeground(UIUtils.TEXTE_SECONDAIRE);
        rightMinistry.add(lblMinistry);

        topUtilityBar.add(rightMinistry, BorderLayout.EAST);
        backgroundPanel.add(topUtilityBar, BorderLayout.NORTH);

        // =====================================================================
        // 2. Zone centrale : bandeau d'accueil + les deux cartes de connexion
        // =====================================================================
        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(14, 28, 12, 28));

        // Badge + titre principal
        JPanel heroHeader = new JPanel();
        heroHeader.setLayout(new BoxLayout(heroHeader, BoxLayout.Y_AXIS));
        heroHeader.setOpaque(false);
        heroHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Badge "Fonds Documentaire Numérique Institutionnel"
        JPanel tagPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(224, 238, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(186, 214, 255));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tagPill.setOpaque(false);
        tagPill.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblTag = new JLabel("🛡 Fonds Documentaire Numérique Institutionnel");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTag.setForeground(UIUtils.BLEU_UGB);
        tagPill.add(lblTag);
        heroHeader.add(tagPill);
        heroHeader.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel lblGrandTitre = new JLabel("Portail Numérique des Thèses & Mémoires Universitaires");
        lblGrandTitre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGrandTitre.setForeground(UIUtils.TEXTE_FONCE);
        lblGrandTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroHeader.add(lblGrandTitre);
        heroHeader.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblSousTitre = new JLabel("Consultation académique ouverte et gestion décentralisée des productions de recherche des 8 UFR de l'UGB.");
        lblSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSousTitre.setForeground(UIUtils.TEXTE_SECONDAIRE);
        lblSousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroHeader.add(lblSousTitre);
        heroHeader.add(Box.createRigidArea(new Dimension(0, 16)));

        centerWrapper.add(heroHeader);

        // Les deux cartes côte à côte
        JPanel cardsContainer = new JPanel(new GridLayout(1, 2, 22, 0));
        cardsContainer.setOpaque(false);
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Carte 1 : Consultation Étudiante (Sans mot de passe)
        JPanel cardEtudiant = creerCarteEtudiant();
        cardsContainer.add(cardEtudiant);

        // Carte 2 : Administration & Enseignants (Identifiant + Mot de passe)
        JPanel cardPersonnel = creerCartePersonnel();
        cardsContainer.add(cardPersonnel);

        centerWrapper.add(cardsContainer);
        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        // =====================================================================
        // 3. Pied de page : raccourcis de démonstration + état de la base
        // =====================================================================
        JPanel footerWrapper = new JPanel(new BorderLayout(0, 6));
        footerWrapper.setOpaque(false);
        footerWrapper.setBorder(new EmptyBorder(6, 28, 12, 28));

        // Raccourcis rapides de test
        JPanel barRaccourcis = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(UIUtils.BORDURE_DOUCE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        barRaccourcis.setOpaque(false);
        barRaccourcis.setBorder(new EmptyBorder(4, 12, 4, 12));

        JLabel lblRaccourcis = new JLabel("⚡ Accès rapide de démonstration :");
        lblRaccourcis.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRaccourcis.setForeground(UIUtils.TEXTE_SECONDAIRE);
        barRaccourcis.add(lblRaccourcis);

        JButton btnDemoEtu1 = UIUtils.creerBoutonSecondaire("🎓 Étudiant : Ibrahima SOW (SAT)");
        btnDemoEtu1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoEtu1.addActionListener(e -> {
            txtEtuPrenom.setText("Ibrahima");
            txtEtuNom.setText("SOW");
            txtEtuEmail.setText("ibrahima.sow@ugb.edu.sn");
            txtEtuCode.setText("P28 0145");
        });
        barRaccourcis.add(btnDemoEtu1);

        JButton btnDemoEtu2 = UIUtils.creerBoutonSecondaire("🎓 Étudiante : Fatou NDIAYE (SEG)");
        btnDemoEtu2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoEtu2.addActionListener(e -> {
            txtEtuPrenom.setText("Fatou");
            txtEtuNom.setText("NDIAYE");
            txtEtuEmail.setText("fatou.ndiaye@ugb.edu.sn");
            txtEtuCode.setText("P29 0089");
        });
        barRaccourcis.add(btnDemoEtu2);

        JButton btnDemoGest = UIUtils.creerBoutonSecondaire("👨‍🏫 Gestionnaire : M. Diakhame (SAT)");
        btnDemoGest.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoGest.addActionListener(e -> {
            txtPersoEmail.setText("moussa.diakhame@ugb.edu.sn");
            txtPersoPass.setText("pass123");
        });
        barRaccourcis.add(btnDemoGest);

        JButton btnDemoAdmin = UIUtils.creerBoutonSecondaire("🛡 Admin Central : Ibrahima NDIAYE");
        btnDemoAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoAdmin.addActionListener(e -> {
            txtPersoEmail.setText("admin@ugb.edu.sn");
            txtPersoPass.setText("admin123");
        });
        barRaccourcis.add(btnDemoAdmin);

        JButton btnConfigDB = UIUtils.creerBoutonSecondaire("⚙️ Config BDD");
        btnConfigDB.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnConfigDB.addActionListener(e -> ouvrirDialogueConfigBDD());
        barRaccourcis.add(btnConfigDB);

        footerWrapper.add(barRaccourcis, BorderLayout.NORTH);

        // Barre d'état inférieure
        JPanel bottomStatusBar = new JPanel(new BorderLayout());
        bottomStatusBar.setOpaque(false);

        lblStatutBD = new JLabel(getTexteStatutBD());
        lblStatutBD.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatutBD.setForeground(UIUtils.TEXTE_SECONDAIRE);
        bottomStatusBar.add(lblStatutBD, BorderLayout.WEST);

        JLabel lblStandards = new JLabel("Archives Ouvertes HAL/CAMES Conformes • Support : dsi@ugb.edu.sn");
        lblStandards.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStandards.setForeground(UIUtils.TEXTE_GRIS);
        bottomStatusBar.add(lblStandards, BorderLayout.EAST);

        footerWrapper.add(bottomStatusBar, BorderLayout.SOUTH);
        backgroundPanel.add(footerWrapper, BorderLayout.SOUTH);
    }

    private JPanel creerCarteEtudiant() {
        JPanel carte = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(UIUtils.BORDURE_DOUCE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        carte.setOpaque(false);
        carte.setBorder(new EmptyBorder(18, 20, 16, 20));

        // Haut : Badge d'accès + Titre
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel pillAcces = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pillAcces.setOpaque(false);
        JLabel lblPill = new JLabel("🎓 Accès Libre Étudiant — Sans Mot de Passe");
        lblPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPill.setForeground(UIUtils.BLEU_UGB);
        pillAcces.add(lblPill);
        top.add(pillAcces, BorderLayout.WEST);

        carte.add(top, BorderLayout.NORTH);

        // Centre : Description + Formulaire
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel lblTitre = new JLabel("Consultation Étudiante");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitre.setForeground(UIUtils.TEXTE_FONCE);
        body.add(lblTitre);
        body.add(Box.createRigidArea(new Dimension(0, 3)));

        JLabel lblDesc = new JLabel("Accédez immédiatement au catalogue et téléchargez selon vos droits.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(UIUtils.TEXTE_SECONDAIRE);
        body.add(lblDesc);
        body.add(Box.createRigidArea(new Dimension(0, 12)));

        // Grille de saisie
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 6));
        form.setOpaque(false);

        // Prénom & Nom (deux champs sur une même ligne)
        JPanel rowNomPrenom = new JPanel(new GridLayout(1, 2, 8, 0));
        rowNomPrenom.setOpaque(false);

        JPanel pnlPrenom = new JPanel(new BorderLayout(0, 2));
        pnlPrenom.setOpaque(false);
        pnlPrenom.add(new JLabel("Prénom"), BorderLayout.NORTH);
        txtEtuPrenom = UIUtils.creerChampModerne(12, "Ex: Ibrahima");
        pnlPrenom.add(txtEtuPrenom, BorderLayout.CENTER);

        JPanel pnlNom = new JPanel(new BorderLayout(0, 2));
        pnlNom.setOpaque(false);
        pnlNom.add(new JLabel("Nom"), BorderLayout.NORTH);
        txtEtuNom = UIUtils.creerChampModerne(12, "Ex: SOW");
        pnlNom.add(txtEtuNom, BorderLayout.CENTER);

        rowNomPrenom.add(pnlPrenom);
        rowNomPrenom.add(pnlNom);
        form.add(rowNomPrenom);

        // Email universitaire
        JPanel pnlEmail = new JPanel(new BorderLayout(0, 2));
        pnlEmail.setOpaque(false);
        pnlEmail.add(new JLabel("Adresse email institutionnelle (@ugb.edu.sn)"), BorderLayout.NORTH);
        txtEtuEmail = UIUtils.creerChampModerne(20, "ex: ibrahima.sow@ugb.edu.sn");
        pnlEmail.add(txtEtuEmail, BorderLayout.CENTER);
        form.add(pnlEmail);

        // Code Étudiant
        JPanel pnlCode = new JPanel(new BorderLayout(0, 2));
        pnlCode.setOpaque(false);
        pnlCode.add(new JLabel("Code Étudiant officiel (ex: P28 0145)"), BorderLayout.NORTH);
        txtEtuCode = UIUtils.creerChampModerne(20, "Ex: P28 0145");
        pnlCode.add(txtEtuCode, BorderLayout.CENTER);
        form.add(pnlCode);

        body.add(form);
        carte.add(body, BorderLayout.CENTER);

        // Bouton de validation étudiant
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnConnexion = UIUtils.creerBoutonPrimaire("Accéder au Catalogue Documentaire →");
        btnConnexion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnexion.setPreferredSize(new Dimension(0, 42));
        btnConnexion.addActionListener(e -> tenterConnexionEtudiant());

        Runnable onEnter = this::tenterConnexionEtudiant;
        UIUtils.associerToucheEntree(txtEtuPrenom, onEnter);
        UIUtils.associerToucheEntree(txtEtuNom, onEnter);
        UIUtils.associerToucheEntree(txtEtuEmail, onEnter);
        UIUtils.associerToucheEntree(txtEtuCode, onEnter);

        actions.add(btnConnexion, BorderLayout.CENTER);
        carte.add(actions, BorderLayout.SOUTH);

        return carte;
    }

    private JPanel creerCartePersonnel() {
        JPanel carte = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(UIUtils.BORDURE_DOUCE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        carte.setOpaque(false);
        carte.setBorder(new EmptyBorder(18, 20, 16, 20));

        // Haut : Badge d'accès réservé
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel pillAcces = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pillAcces.setOpaque(false);
        JLabel lblPill = new JLabel("🛡 Espace Réservé Personnel & Enseignants");
        lblPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPill.setForeground(UIUtils.BLEU_SECONDAIRE);
        pillAcces.add(lblPill);
        top.add(pillAcces, BorderLayout.WEST);

        carte.add(top, BorderLayout.NORTH);

        // Centre : Titre + Formulaire
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel lblTitre = new JLabel("Administration & Gestionnaires");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitre.setForeground(UIUtils.TEXTE_FONCE);
        body.add(lblTitre);
        body.add(Box.createRigidArea(new Dimension(0, 3)));

        JLabel lblDesc = new JLabel("Authentification sécurisée pour le dépôt légal, l'indexation et la modération.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(UIUtils.TEXTE_SECONDAIRE);
        body.add(lblDesc);
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // Formulaire
        JPanel form = new JPanel(new GridLayout(3, 1, 0, 10));
        form.setOpaque(false);

        // Identifiant universitaire
        JPanel pnlEmail = new JPanel(new BorderLayout(0, 2));
        pnlEmail.setOpaque(false);
        pnlEmail.add(new JLabel("Identifiant universitaire (@ugb.edu.sn)"), BorderLayout.NORTH);
        txtPersoEmail = UIUtils.creerChampModerne(20, "ex: moussa.diakhame@ugb.edu.sn");
        pnlEmail.add(txtPersoEmail, BorderLayout.CENTER);
        form.add(pnlEmail);

        // Mot de passe
        JPanel pnlPass = new JPanel(new BorderLayout(0, 2));
        pnlPass.setOpaque(false);
        pnlPass.add(new JLabel("Mot de passe sécurisé"), BorderLayout.NORTH);
        txtPersoPass = UIUtils.creerChampMotDePasse(20);
        pnlPass.add(txtPersoPass, BorderLayout.CENTER);
        form.add(pnlPass);

        body.add(form);
        carte.add(body, BorderLayout.CENTER);

        // Bouton de connexion personnel
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        UIUtils.BoutonModerne btnConnexion = new UIUtils.BoutonModerne(
                "Connexion à la Console de Gestion 🔒",
                UIUtils.BLEU_SECONDAIRE,
                new Color(30, 41, 59),
                new Color(15, 23, 42),
                Color.WHITE,
                Color.WHITE,
                new Color(15, 23, 42),
                12
        );
        btnConnexion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnexion.setPreferredSize(new Dimension(0, 42));
        btnConnexion.addActionListener(e -> tenterConnexionPersonnel());

        Runnable onEnter = this::tenterConnexionPersonnel;
        UIUtils.associerToucheEntree(txtPersoEmail, onEnter);
        UIUtils.associerToucheEntree(txtPersoPass, onEnter);

        actions.add(btnConnexion, BorderLayout.CENTER);
        carte.add(actions, BorderLayout.SOUTH);

        return carte;
    }

    private String getTexteStatutBD() {
        if (DAOFactory.isUsingJdbc()) {
            return "● Infrastructure UGB : Serveur MySQL Connecté ('" + DBConnection.getDatabase() + "')";
        } else {
            return "● Infrastructure UGB : Mode Mémoire Vive Actif (Collections Java synchronisées)";
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
                                "L'application est synchronisée via JDBC.",
                        "MySQL Opérationnel", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Le serveur MySQL n'a pas répondu sur " + DBConnection.getHost() + ":" + DBConnection.getPort() + ".\n" +
                                "L'application continue de fonctionner en Mode Mémoire (Collections Java).",
                        "Serveur MySQL Hors-ligne", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void ouvrirDialogueConfigBDD() {
        JDialog dialog = new JDialog(this, "Configuration de la Base de Données MySQL", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        UIUtils.associerToucheEchap(dialog);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 18, 20));
        form.setBackground(Color.WHITE);
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

        int row = 0;
        g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        form.add(UIUtils.creerLabelChamp("Hôte MySQL :"), g);
        g.gridx = 1; g.weightx = 0.65;
        form.add(txtHost, g);

        row++;
        g.gridx = 0; g.gridy = row;
        form.add(UIUtils.creerLabelChamp("Port :"), g);
        g.gridx = 1;
        form.add(txtPort, g);

        row++;
        g.gridx = 0; g.gridy = row;
        form.add(UIUtils.creerLabelChamp("Base de données :"), g);
        g.gridx = 1;
        form.add(txtDatabase, g);

        row++;
        g.gridx = 0; g.gridy = row;
        form.add(UIUtils.creerLabelChamp("Utilisateur :"), g);
        g.gridx = 1;
        form.add(txtUser, g);

        row++;
        g.gridx = 0; g.gridy = row;
        form.add(UIUtils.creerLabelChamp("Mot de passe :"), g);
        g.gridx = 1;
        form.add(txtPass, g);

        dialog.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(new Color(248, 250, 252));
        JButton btnFermer = UIUtils.creerBoutonSecondaire("Fermer");
        btnFermer.addActionListener(e -> dialog.dispose());
        actions.add(btnFermer);

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
