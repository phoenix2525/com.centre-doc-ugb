package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.dao.DAOFactory;
import sn.ugb.centredoc.exception.*;
import sn.ugb.centredoc.model.*;
import sn.ugb.centredoc.service.AuthService;
import sn.ugb.centredoc.service.UtilisateurService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Mini-fenêtre modale d'Enrôlement et de Modification d'un étudiant.
 * Fenêtre à part entière centrée qui s'ouvre au milieu de l'écran lorsque
 * le gestionnaire clique sur le bouton d'enrôlement.
 * Règles métier respectées : email @ugb.edu.sn, unicité, alerte hors UFR.
 */
public class DialogueEnrolementEtudiant extends JDialog {

    private final Utilisateur gestionnaire;
    private final UtilisateurService utilisateurService;
    private final boolean modeEdition;
    private final int idEtudiant;
    private boolean succes = false;

    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtEmail;
    private JTextField txtCode;
    private JComboBox<Ufr> comboUfr;
    private Ufr ufrGestionnaire;

    /**
     * Constructeur unique : modeEdition=false pour un nouvel enrôlement,
     * modeEdition=true pour modifier l'étudiant identifié par idEtudiant
     * (nom, prenom, email, code servent alors au pré-remplissage).
     */
    public DialogueEnrolementEtudiant(Window parent,
                                      Utilisateur gestionnaire,
                                      UtilisateurService utilisateurService,
                                      boolean modeEdition,
                                      int idEtudiant,
                                      String nom,
                                      String prenom,
                                      String email,
                                      String code) {
        super(parent, modeEdition ? "Modifier l'étudiant #" + idEtudiant : "Enrôlement d'un Nouvel Étudiant",
                ModalityType.APPLICATION_MODAL);
        this.gestionnaire = gestionnaire;
        this.utilisateurService = utilisateurService;
        this.modeEdition = modeEdition;
        this.idEtudiant = idEtudiant;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI(nom, prenom, email, code);
        pack();
        setLocationRelativeTo(parent);
        UIUtils.associerToucheEchap(this);
    }

    /** Indique si l'enrôlement ou la modification a été validé avec succès. */
    public boolean isSucces() {
        return succes;
    }

    private void initUI(String nom, String prenom, String email, String code) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));
        root.setPreferredSize(new Dimension(500, 470));

        // =====================================================================
        // 1. En-tête : Icône ronde + Titres
        // =====================================================================
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setOpaque(false);

        JPanel iconePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.BADGE_BLEU_FOND);
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(UIUtils.BLEU_UGB);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 44, 44);

                g2.setColor(UIUtils.BLEU_UGB);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                String sign = "🎓";
                int tx = (46 - fm.stringWidth(sign)) / 2;
                int ty = (46 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(sign, tx, ty);

                g2.dispose();
            }
        };
        iconePanel.setPreferredSize(new Dimension(46, 46));
        iconePanel.setOpaque(false);
        headerPanel.add(iconePanel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);

        JLabel lblSurTitre = new JLabel(modeEdition ? "MODIFICATION D'UN COMPTE ÉTUDIANT" : "MODULE D'ENRÔLEMENT DES ÉTUDIANTS");
        lblSurTitre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSurTitre.setForeground(UIUtils.TEXTE_SECONDAIRE);

        JLabel lblGrandTitre = new JLabel(modeEdition ? "Modifier les informations de l'étudiant"
                : "Inscrire un nouvel étudiant au système");
        lblGrandTitre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGrandTitre.setForeground(UIUtils.TEXTE_FONCE);

        titleBlock.add(lblSurTitre);
        titleBlock.add(lblGrandTitre);
        headerPanel.add(titleBlock, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);

        // =====================================================================
        // 2. Encart de rappel UFR gestionnaire
        // =====================================================================
        String codeUfr = (gestionnaire.getIdUfr() != null)
                ? String.valueOf(gestionnaire.getIdUfr()) : "UFR";
        if (gestionnaire.getNomUfr() != null && !gestionnaire.getNomUfr().isEmpty()) {
            codeUfr = gestionnaire.getNomUfr();
        }

        JPanel encartUfrInfo = new JPanel(new BorderLayout());
        encartUfrInfo.setBackground(UIUtils.BADGE_BLEU_FOND);
        encartUfrInfo.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblInfoUfrGest = new JLabel("<html><b>UFR gérée :</b> " + codeUfr
                + " — Tout enregistrement hors de cette UFR sollicite une confirmation expresse.</html>");
        lblInfoUfrGest.setFont(UIUtils.FONT_PETIT);
        lblInfoUfrGest.setForeground(UIUtils.BADGE_BLEU_TEXT);
        encartUfrInfo.add(lblInfoUfrGest, BorderLayout.CENTER);

        root.add(encartUfrInfo, BorderLayout.NORTH);

        // =====================================================================
        // 3. Formulaire de saisie (avec scroll de sécurité)
        // =====================================================================
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNom = UIUtils.creerChampTexte(16);
        txtPrenom = UIUtils.creerChampTexte(16);
        txtEmail = UIUtils.creerChampModerne(16, "prenom.nom@ugb.edu.sn");
        txtCode = UIUtils.creerChampTexte(16);
        comboUfr = new JComboBox<>();
        comboUfr.setFont(UIUtils.FONT_NORMAL);

        chargerListeUfrs();

        // Pré-remplissage en mode édition
        if (modeEdition) {
            txtNom.setText(nom != null ? nom : "");
            txtPrenom.setText(prenom != null ? prenom : "");
            txtEmail.setText(email != null ? email : "");
            txtCode.setText(code != null ? code : "");
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtNom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtPrenom, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Email (@ugb.edu.sn) :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("Code Étudiant :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(txtCode, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; form.add(new JLabel("UFR de l'étudiant :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(comboUfr, gbc);

        JScrollPane scrollForm = new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        root.add(scrollForm, BorderLayout.CENTER);

        // =====================================================================
        // 4. Boutons d'actions
        // =====================================================================
        JPanel btnPanel = new JPanel(new BorderLayout(10, 0));
        btnPanel.setOpaque(false);

        JButton btnAnnuler = UIUtils.creerBoutonSecondaire("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        btnPanel.add(btnAnnuler, BorderLayout.WEST);

        JButton btnValider = UIUtils.creerBoutonPrimaire(modeEdition ? "✓ Enregistrer les Modifications" : "✓ Valider l'Enrôlement");
        btnValider.addActionListener(e -> valider());
        btnPanel.add(btnValider, BorderLayout.EAST);

        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void chargerListeUfrs() {
        try {
            List<Ufr> ufrs = DAOFactory.getUfrDAO().listerTous();
            Ufr ufrSelection = null;
            for (Ufr u : ufrs) {
                comboUfr.addItem(u);
                if (gestionnaire.getIdUfr() != null && u.getIdUfr() == gestionnaire.getIdUfr()) {
                    ufrSelection = u;
                }
            }
            if (ufrSelection != null) {
                comboUfr.setSelectedItem(ufrSelection);
            }
            this.ufrGestionnaire = ufrSelection;
        } catch (Exception e) {
            System.err.println("Erreur chargement UFRs : " + e.getMessage());
        }
    }

    private void valider() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String code = txtCode.getText().trim();
        Ufr ufrChoisie = (Ufr) comboUfr.getSelectedItem();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom de l'étudiant est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtNom.requestFocus();
            return;
        }
        if (prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom de l'étudiant est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtPrenom.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "L'adresse email est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        try {
            AuthService.validerEmailUGB(email);
        } catch (ChampInvalideException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Format Email Non Conforme", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le code étudiant est obligatoire (ex: P28 0145).", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            txtCode.requestFocus();
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
                for (int i = 0; i < comboUfr.getItemCount(); i++) {
                    Ufr u = comboUfr.getItemAt(i);
                    if (u != null && u.getIdUfr() == idUfrGestionnaire) {
                        comboUfr.setSelectedIndex(i);
                        break;
                    }
                }
                comboUfr.requestFocus();
                return;
            } else if (dialogAlerte.getResultat() != DialogueAlerteEnrolementHorsUfr.Resultat.CONFIRMER) {
                comboUfr.requestFocus();
                return;
            }
        }

        try {
            if (modeEdition) {
                utilisateurService.modifierEtudiant(idEtudiant, nom, prenom, email, code, ufrChoisie.getIdUfr(), gestionnaire);
                JOptionPane.showMessageDialog(this,
                        "Informations de l'étudiant mises à jour avec succès !",
                        "Mise à Jour Réussie", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Etudiant nouvelEtu = utilisateurService.creerEtudiant(
                        nom, prenom, email, code, ufrChoisie.getIdUfr(), gestionnaire
                );
                JOptionPane.showMessageDialog(this,
                        "Étudiant enrôlé avec succès !\n\n"
                        + "• Nom & Prénom : " + nouvelEtu.getNomComplet() + "\n"
                        + "• Code Étudiant : " + nouvelEtu.getCodeEtudiant() + "\n"
                        + "• Email : " + nouvelEtu.getEmail() + "\n"
                        + "• UFR de rattachement : " + ufrChoisie.getCode() + " - " + ufrChoisie.getNom(),
                        "Enrôlement Réussi", JOptionPane.INFORMATION_MESSAGE);
            }
            succes = true;
            dispose();
        } catch (ChampInvalideException | DoublonException | AccesRefuseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle Métier Non Respectée", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
