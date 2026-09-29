package sn.ugb.centredoc.ui;

import sn.ugb.centredoc.model.Ufr;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Boîte de dialogue modale de contrôle décanal et d'avertissement d'enrôlement hors UFR.
 * Conforme aux spécifications graphiques Stitch (Academic Prestige & High-Density Rigor).
 * Permet au gestionnaire de confirmer l'affectation ciblée ou de corriger automatiquement vers son UFR.
 */
public class DialogueAlerteEnrolementHorsUfr extends JDialog {

    public enum Resultat {
        CONFIRMER,
        CORRIGER,
        ANNULER
    }

    private Resultat resultat = Resultat.ANNULER;

    public DialogueAlerteEnrolementHorsUfr(Window parent,
                                          String nomEtudiant,
                                          String prenomEtudiant,
                                          String codeEtudiant,
                                          Ufr ufrGestionnaire,
                                          Ufr ufrChoisie) {
        super(parent, "Contrôle de Cohérence Décanale", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI(nomEtudiant, prenomEtudiant, codeEtudiant, ufrGestionnaire, ufrChoisie);
        pack();
        setLocationRelativeTo(parent);
    }

    public Resultat getResultat() {
        return resultat;
    }

    private void initUI(String nomEtudiant,
                        String prenomEtudiant,
                        String codeEtudiant,
                        Ufr ufrGestionnaire,
                        Ufr ufrChoisie) {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(24, 24, 22, 24));
        root.setPreferredSize(new Dimension(540, 460));

        // =====================================================================
        // 1. En-tête : Icône d'avertissement + Titres Stitch
        // =====================================================================
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setOpaque(false);

        // Badge circulaire d'icône d'alerte
        JPanel iconePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 243, 199)); // #FEF3C7 - Jaune ambré très doux
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(new Color(245, 158, 11)); // #F59E0B - Bordure ambre
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 44, 44);

                g2.setColor(new Color(180, 83, 9)); // #B45309
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String sign = "⚠";
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

        JLabel lblSurTitre = new JLabel("CONTRÔLE DE COHÉRENCE DÉCANALE");
        lblSurTitre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSurTitre.setForeground(new Color(194, 65, 12)); // #C2410C - Orange décanal soutenu

        JLabel lblGrandTitre = new JLabel("Attention : Enrôlement hors UFR de tutelle");
        lblGrandTitre.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblGrandTitre.setForeground(new Color(15, 23, 42)); // #0F172A

        titleBlock.add(lblSurTitre);
        titleBlock.add(lblGrandTitre);
        headerPanel.add(titleBlock, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);

        // =====================================================================
        // 2. Corps : Explication détaillée & Cartes comparatives
        // =====================================================================
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        String nomComplet = ((prenomEtudiant != null ? prenomEtudiant : "") + " " + (nomEtudiant != null ? nomEtudiant : "")).trim();
        String codeUfrGest = (ufrGestionnaire != null && ufrGestionnaire.getCode() != null) ? ufrGestionnaire.getCode() : "SAT";
        String nomUfrGest = (ufrGestionnaire != null && ufrGestionnaire.getNom() != null) ? ufrGestionnaire.getNom() : "Sciences Appliquées & Technologies";

        String codeUfrCible = (ufrChoisie != null && ufrChoisie.getCode() != null) ? ufrChoisie.getCode() : "UFR";
        String nomUfrCible = (ufrChoisie != null && ufrChoisie.getNom() != null) ? ufrChoisie.getNom() : "";

        // Texte explicatif HTML
        JEditorPane txtExplication = new JEditorPane();
        txtExplication.setContentType("text/html");
        txtExplication.setEditable(false);
        txtExplication.setOpaque(false);
        txtExplication.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        txtExplication.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        String html = "<html><body style='font-family: Segoe UI, sans-serif; color: #334155; margin: 0; line-height: 1.45;'>"
                + "Vous êtes actuellement gestionnaire habilité du centre documentaire de l'<b>UFR " + codeUfrGest + "</b>.<br>"
                + "Vous êtes sur le point d'enrôler l'étudiant <b>" + nomComplet + "</b> "
                + "(Code : <code style='background: #F1F5F9; padding: 2px 5px; color: #0F172A; border-radius: 3px;'><b>"
                + (codeEtudiant != null ? codeEtudiant : "") + "</b></code>) au sein de l'<b>UFR " + codeUfrCible + " (" + nomUfrCible + ")</b>.<br><br>"
                + "<span style='color: #64748B;'>ⓘ Cette inscription rattachera l'étudiant au catalogue et aux droits d'emprunt spécifiques de l'UFR "
                + codeUfrCible + ". Confirmez-vous qu'il ne s'agit pas d'une erreur de saisie d'UFR ?</span>"
                + "</body></html>";
        txtExplication.setText(html);
        centerPanel.add(txtExplication);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // Grille des deux cartes comparatives (Actuel vs Cible)
        JPanel comparisonGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        comparisonGrid.setOpaque(false);
        comparisonGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Carte 1 : Centre actuel
        JPanel cardActuel = creerCarteComparaison(
                "Votre Centre Actuel",
                "UFR " + codeUfrGest + " (" + tronquer(nomUfrGest, 18) + ")",
                "Saint-Louis • Campus 1",
                new Color(248, 250, 252),
                new Color(226, 232, 240),
                new Color(15, 23, 42)
        );

        // Carte 2 : Affectation ciblée (Mise en valeur ambre/orange)
        JPanel cardCible = creerCarteComparaison(
                "Affectation Ciblée",
                "UFR " + codeUfrCible + " (" + tronquer(nomUfrCible, 18) + ")",
                "Saint-Louis • Campus UGB",
                new Color(255, 251, 235), // #FFFBEB
                new Color(245, 158, 11),  // #F59E0B
                new Color(180, 83, 9)     // #B45309
        );

        comparisonGrid.add(cardActuel);
        comparisonGrid.add(cardCible);
        centerPanel.add(comparisonGrid);

        root.add(centerPanel, BorderLayout.CENTER);

        // =====================================================================
        // 3. Pied de page : 2 Boutons d'action Stitch
        // =====================================================================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setOpaque(false);

        // Bouton 1 : Corriger vers UFR du gestionnaire
        JButton btnCorriger = UIUtils.creerBoutonSecondaire("↺ Corriger vers UFR " + codeUfrGest);
        btnCorriger.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCorriger.addActionListener(e -> {
            this.resultat = Resultat.CORRIGER;
            dispose();
        });
        footerPanel.add(btnCorriger);

        // Bouton 2 : Confirmer l'affectation à l'autre UFR (Bouton ambre/orange Stitch)
        UIUtils.BoutonModerne btnConfirmer = new UIUtils.BoutonModerne(
                "✓ Confirmer l'affectation à l'UFR " + codeUfrCible,
                new Color(217, 119, 6),  // #D97706
                new Color(194, 65, 12),  // #C2410C
                new Color(154, 52, 18),
                Color.WHITE,
                Color.WHITE,
                new Color(180, 83, 9),
                10
        );
        btnConfirmer.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmer.addActionListener(e -> {
            this.resultat = Resultat.CONFIRMER;
            dispose();
        });
        footerPanel.add(btnConfirmer);

        root.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel creerCarteComparaison(String surtitre, String titre, String campus,
                                        Color fond, Color bordure, Color texteTitre) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fond);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(bordure);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblSur = new JLabel(surtitre);
        lblSur.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSur.setForeground(new Color(100, 116, 139));

        JLabel lblNom = new JLabel(titre);
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNom.setForeground(texteTitre);

        JLabel lblCampus = new JLabel(campus);
        lblCampus.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCampus.setForeground(new Color(148, 163, 184));

        card.add(lblSur);
        card.add(lblNom);
        card.add(lblCampus);
        return card;
    }

    private String tronquer(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
