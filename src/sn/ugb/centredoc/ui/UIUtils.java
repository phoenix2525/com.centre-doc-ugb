package sn.ugb.centredoc.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

/**
 * Utilitaires d'interface graphique pour le Système
 * d'Information des Centres de Documentation de l'Université Gaston Berger (UGB).
 * Regroupe les couleurs, polices et composants graphiques réutilisés dans toute l'application.
 */
public class UIUtils {
    // =========================================================================
    // Palette de couleurs de la charte graphique du projet
    // =========================================================================
    public static final Color BLEU_UGB = new Color(29, 78, 216);         // #1D4ED8 - Bleu principal du projet
    public static final Color BLEU_SECONDAIRE = new Color(15, 23, 42);   // #0F172A - Bleu nuit (ardoise)
    public static final Color BLEU_HOVER = new Color(30, 64, 175);       // #1E40AF - Bleu foncé survol
    public static final Color BLEU_PRESSE = new Color(23, 37, 84);       // #172554 - Bleu profond enfoncé
    public static final Color BLEU_SELECTION = new Color(224, 238, 255);  // #E0EEFF - Fond sélection table doux

    public static final Color ACCENT_EMERAUDE = new Color(5, 150, 105);  // #059669 - Émeraude (succès, téléchargement)
    public static final Color ACCENT_HOVER = new Color(4, 120, 87);      // #047857
    public static final Color ACCENT_PRESSE = new Color(6, 95, 70);

    public static final Color OR_UGB = new Color(217, 119, 6);           // #D97706 - Ambre institutionnel
    public static final Color OR_HOVER = new Color(245, 158, 11);         // #F59E0B
    public static final Color OR_PRESSE = new Color(180, 83, 9);          // #B45309
    public static final Color TEXTE_ACCENT_DORE = new Color(180, 83, 9);

    public static final Color FOND_CLAIR = new Color(248, 249, 255);     // #F8F9FF - Fond très clair des fenêtres
    public static final Color FOND_CARTE = Color.WHITE;
    public static final Color BLANC = Color.WHITE;

    // Typographie contrastée, lisible à l'écran
    public static final Color TEXTE_FONCE = new Color(11, 28, 48);       // #0B1C30 - Encre profonde
    public static final Color TEXTE_SECONDAIRE = new Color(67, 70, 85);  // #434655 - Gris ardoise soutenu
    public static final Color TEXTE_GRIS = new Color(116, 118, 134);     // #747686 - Gris contour
    public static final Color BORDURE_DOUCE = new Color(226, 232, 240);  // #E2E8F0 - Bordure subtile
    public static final Color BORDURE_ACCENT = new Color(203, 213, 225); // #CBD5E1
    public static final Color LIGNE_ALTERNEE = new Color(248, 250, 253);

    // Couleurs sémantiques (succès, avertissement, erreur)
    public static final Color VERT_SUCCES = new Color(5, 150, 105);      // #059669
    public static final Color ORANGE_AVERT = new Color(234, 88, 12);     // #EA580C
    public static final Color ORANGE_AVERT_FONCE = new Color(194, 65, 12);// #C2410C
    public static final Color ROUGE_DANGER = new Color(220, 38, 38);     // #DC2626

    // Couleurs des badges d'état
    public static final Color BADGE_VERT_FOND = new Color(236, 253, 245);
    public static final Color BADGE_VERT_TEXT = new Color(4, 120, 87);

    public static final Color BADGE_BLEU_FOND = new Color(239, 246, 255);
    public static final Color BADGE_BLEU_TEXT = new Color(29, 78, 216);

    public static final Color BADGE_ORANGE_FOND = new Color(255, 251, 235);
    public static final Color BADGE_ORANGE_TEXT = new Color(180, 83, 9);

    public static final Color BADGE_ROUGE_FOND = new Color(254, 242, 242);
    public static final Color BADGE_ROUGE_TEXT = new Color(185, 28, 28);

    public static final Color BADGE_VIOLET_FOND = new Color(243, 229, 245);
    public static final Color BADGE_VIOLET_TEXT = new Color(106, 27, 154);

    public static final Color BADGE_TEAL_FOND = new Color(224, 242, 241);
    public static final Color BADGE_TEAL_TEXT = new Color(0, 105, 92);

    // Polices standardisées haute lisibilité
    public static final Font FONT_GRAND_TITRE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITRE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SOUS_TITRE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_GRAS = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_PETIT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 28);

    /**
     * Initialise les propriétés graphiques globales de Swing (UIManager).
     */
    public static void initialiserTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        UIManager.put("Label.foreground", TEXTE_FONCE);
        UIManager.put("TextField.foreground", TEXTE_FONCE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", TEXTE_FONCE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("TextArea.foreground", TEXTE_FONCE);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXTE_FONCE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXTE_FONCE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", BLEU_SELECTION);
        UIManager.put("Table.selectionForeground", BLEU_SECONDAIRE);
        UIManager.put("Table.gridColor", new Color(238, 242, 246));
        UIManager.put("TitledBorder.titleColor", BLEU_UGB);
    }

    /**
     * Panneau moderne à dégradé vertical ou horizontal.
     */
    public static class PanelDegrade extends JPanel {
        private final Color couleurHaut;
        private final Color couleurBas;
        private final boolean vertical;

        public PanelDegrade(Color couleurHaut, Color couleurBas, boolean vertical) {
            super(new BorderLayout());
            this.couleurHaut = couleurHaut;
            this.couleurBas = couleurBas;
            this.vertical = vertical;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = vertical
                    ? new GradientPaint(0, 0, couleurHaut, 0, h, couleurBas)
                    : new GradientPaint(0, 0, couleurHaut, w, 0, couleurBas);

            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel creerPanelDegrade(Color haut, Color bas, boolean vertical) {
        return new PanelDegrade(haut, bas, vertical);
    }

    /**
     * Panneau carte blanche flottante avec ombre portée douce et coins arrondis.
     */
    public static class CarteOmbrePanel extends JPanel {
        private final int rayon;

        public CarteOmbrePanel(int rayon) {
            super(new BorderLayout());
            this.rayon = rayon;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 18, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Ombre portée étagée subtile
            g2.setColor(new Color(15, 30, 60, 10));
            g2.fillRoundRect(4, 6, w - 8, h - 8, rayon + 4, rayon + 4);
            g2.setColor(new Color(15, 30, 60, 20));
            g2.fillRoundRect(2, 3, w - 4, h - 5, rayon + 2, rayon + 2);

            // Fond carte blanc pur
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 2, h - 4, rayon, rayon);

            // Bordure très fine
            g2.setColor(BORDURE_DOUCE);
            g2.drawRoundRect(0, 0, w - 3, h - 5, rayon, rayon);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel creerCarteOmbre(int rayon) {
        return new CarteOmbrePanel(rayon);
    }

    /**
     * Bouton moderne avec rendu 2D anti-crénelé, coins arrondis, états de survol et pression.
     */
    public static class BoutonModerne extends JButton {
        private final Color fondNormal;
        private final Color fondSurvol;
        private final Color fondPresse;
        private final Color texteNormal;
        private final Color texteSurvol;
        private final Color bordureCouleur;
        private final int rayon;

        public BoutonModerne(String texte, Color fondNormal, Color fondSurvol, Color fondPresse,
                             Color texteNormal, Color texteSurvol, Color bordureCouleur, int rayon) {
            super(texte);
            this.fondNormal = fondNormal;
            this.fondSurvol = fondSurvol;
            this.fondPresse = fondPresse;
            this.texteNormal = texteNormal;
            this.texteSurvol = texteSurvol;
            this.bordureCouleur = bordureCouleur;
            this.rayon = rayon;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color bg;
            Color fg;
            Color border;

            if (!isEnabled()) {
                bg = new Color(238, 242, 246);
                fg = new Color(150, 160, 175);
                border = new Color(220, 226, 235);
            } else if (getModel().isPressed()) {
                bg = fondPresse;
                fg = texteSurvol;
                border = (bordureCouleur != null) ? bordureCouleur.darker() : null;
            } else if (getModel().isRollover()) {
                bg = fondSurvol;
                fg = texteSurvol;
                border = bordureCouleur;
            } else {
                bg = fondNormal;
                fg = texteNormal;
                border = bordureCouleur;
            }

            // Fond arrondi
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, rayon, rayon);

            // Bordure
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, rayon, rayon);
            }

            // Texte centré
            g2.setFont(getFont());
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(getText())) / 2;
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 36;
            int h = fm.getHeight() + 18;
            return new Dimension(Math.max(w, super.getPreferredSize().width), Math.max(h, 36));
        }
    }

    /**
     * Crée un bouton primaire stylisé (Bleu UGB éclatant, texte blanc).
     */
    public static JButton creerBoutonPrimaire(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                BLEU_UGB,
                BLEU_HOVER,
                BLEU_PRESSE,
                BLANC,
                BLANC,
                BLEU_SECONDAIRE,
                12
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un bouton secondaire épuré (Fond blanc, texte foncé, bordure douce).
     */
    public static JButton creerBoutonSecondaire(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                BLANC,
                new Color(245, 248, 252),
                new Color(235, 240, 248),
                TEXTE_FONCE,
                TEXTE_FONCE,
                BORDURE_DOUCE,
                12
        );
        btn.setFont(FONT_NORMAL);
        return btn;
    }

    /**
     * Crée un bouton accentué (Ambre doré UGB avec texte sombre contrasté).
     */
    public static JButton creerBoutonAccent(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                OR_UGB,
                OR_HOVER,
                OR_PRESSE,
                TEXTE_FONCE,
                TEXTE_FONCE,
                new Color(230, 150, 20),
                12
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un bouton d'action critique / danger (Rouge corail contrasté).
     */
    public static JButton creerBoutonDanger(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                new Color(255, 241, 242),
                ROUGE_DANGER,
                new Color(180, 30, 30),
                ROUGE_DANGER,
                Color.WHITE,
                new Color(254, 205, 211),
                12
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un bouton d'accent émeraude moderne (Téléchargements, succès).
     */
    public static JButton creerBoutonEmeraude(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                ACCENT_EMERAUDE,
                ACCENT_HOVER,
                ACCENT_PRESSE,
                Color.WHITE,
                Color.WHITE,
                ACCENT_PRESSE,
                12
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un champ de texte soigné avec bordure dynamique au focus.
     */
    public static JTextField creerChampTexte(int colonnes) {
        return creerChampModerne(colonnes, null);
    }

    public static JTextField creerChampModerne(int colonnes, String placeholder) {
        JTextField tf = new JTextField(colonnes) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (placeholder != null && getText().isEmpty() && !(hasFocus())) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(TEXTE_GRIS);
                    Insets insets = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, insets.left, y);
                    g2.dispose();
                }
            }
        };
        tf.setFont(FONT_NORMAL);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXTE_FONCE);
        tf.setCaretColor(BLEU_UGB);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDURE_DOUCE, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        tf.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(BLEU_UGB, 2, true),
                        new EmptyBorder(7, 9, 7, 9)
                ));
                tf.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(BORDURE_DOUCE, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
                tf.repaint();
            }
        });

        return tf;
    }

    /**
     * Crée un champ de mot de passe avec bordure dynamique au focus.
     */
    public static JPasswordField creerChampMotDePasse(int colonnes) {
        JPasswordField pf = new JPasswordField(colonnes);
        pf.setFont(FONT_NORMAL);
        pf.setBackground(Color.WHITE);
        pf.setForeground(TEXTE_FONCE);
        pf.setCaretColor(BLEU_UGB);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDURE_DOUCE, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        pf.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                pf.setBorder(new CompoundBorder(
                        new LineBorder(BLEU_UGB, 2, true),
                        new EmptyBorder(7, 9, 7, 9)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                pf.setBorder(new CompoundBorder(
                        new LineBorder(BORDURE_DOUCE, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }
        });

        return pf;
    }

    /**
     * Crée un avatar circulaire avec les initiales d'un utilisateur sur fond coloré.
     */
    public static JPanel creerAvatarUtilisateur(String nomComplet, int taille) {
        String initiales = "U";
        if (nomComplet != null && !nomComplet.trim().isEmpty()) {
            String[] parties = nomComplet.trim().split("\\s+");
            if (parties.length >= 2) {
                initiales = ("" + parties[0].charAt(0) + parties[parties.length - 1].charAt(0)).toUpperCase();
            } else if (parties.length == 1 && parties[0].length() > 0) {
                initiales = ("" + parties[0].charAt(0)).toUpperCase();
            }
        }
        final String texteInit = initiales;

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int dim = Math.min(getWidth(), getHeight());
                int x = (getWidth() - dim) / 2;
                int y = (getHeight() - dim) / 2;

                // Fond dégradé circulaire ambre / émeraude
                GradientPaint gp = new GradientPaint(x, y, OR_UGB, x + dim, y + dim, new Color(245, 127, 23));
                g2.setPaint(gp);
                g2.fillOval(x, y, dim, dim);

                // Bordure fine blanche
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, dim, dim);

                // Initiales
                g2.setColor(TEXTE_FONCE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (dim * 0.42)));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (dim - fm.stringWidth(texteInit)) / 2;
                int ty = y + (dim + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(texteInit, tx, ty);

                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(taille, taille));
        avatar.setOpaque(false);
        return avatar;
    }

    /**
     * Bandeau d'en-tête supérieur institutionnel UGB moderne avec dégradé subtil,
     * logo UGB et profil utilisateur soigné.
     */
    public static JPanel creerBandeauHaut(String titre, String sousTitre, String infoUtilisateur) {
        JPanel bandeau = new PanelDegrade(BLEU_UGB, BLEU_SECONDAIRE, false);
        bandeau.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Partie gauche : Logo officiel + titres
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        gauche.setOpaque(false);

        LogoUGB logo = new LogoUGB(46, 46);
        gauche.add(logo);

        JPanel textePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        textePanel.setOpaque(false);

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(FONT_TITRE);
        lblTitre.setForeground(Color.WHITE);

        JLabel lblSousTitre = new JLabel(sousTitre);
        lblSousTitre.setFont(FONT_PETIT);
        lblSousTitre.setForeground(new Color(215, 230, 255));

        textePanel.add(lblTitre);
        textePanel.add(lblSousTitre);
        gauche.add(textePanel);

        bandeau.add(gauche, BorderLayout.WEST);

        // Partie droite : Avatar + profil utilisateur
        if (infoUtilisateur != null && !infoUtilisateur.trim().isEmpty()) {
            JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            droite.setOpaque(false);

            JPanel avatar = creerAvatarUtilisateur(infoUtilisateur, 36);
            droite.add(avatar);

            JLabel lblUser = new JLabel(infoUtilisateur);
            lblUser.setFont(FONT_GRAS);
            lblUser.setForeground(new Color(255, 240, 200));
            droite.add(lblUser);

            bandeau.add(droite, BorderLayout.CENTER);
        }

        return bandeau;
    }

    /**
     * Bandeau d'en-tête institutionnel spécialisé pour l'espace Gestionnaire.
     * Met remarquablement en évidence l'UFR gérée (ex: UFR SAT) dès la connexion.
     */
    public static JPanel creerBandeauGestionnaire(String titre, String sousTitre, String nomComplet,
                                                 String codeUfr, String nomUfr, JButton btnAction) {
        JPanel bandeau = new PanelDegrade(BLEU_UGB, BLEU_SECONDAIRE, false);
        bandeau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, OR_UGB),
                new EmptyBorder(10, 18, 10, 18)
        ));

        // 1. Partie gauche : Logo officiel + titres UGB
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        gauche.setOpaque(false);

        LogoUGB logo = new LogoUGB(50, 50);
        gauche.add(logo);

        JPanel textePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textePanel.setOpaque(false);

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitre.setForeground(Color.WHITE);

        JLabel lblSousTitre = new JLabel(sousTitre);
        lblSousTitre.setFont(FONT_PETIT);
        lblSousTitre.setForeground(new Color(215, 230, 255));

        textePanel.add(lblTitre);
        textePanel.add(lblSousTitre);
        gauche.add(textePanel);

        bandeau.add(gauche, BorderLayout.WEST);

        // 2. Partie centrale : Badge d'impact UFR très mis en valeur
        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centre.setOpaque(false);

        JPanel badgeUfr = new JPanel(new BorderLayout(8, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 25, 47, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(OR_UGB);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeUfr.setOpaque(false);
        badgeUfr.setBorder(new EmptyBorder(6, 18, 6, 18));

        JLabel lblTag = new JLabel("🏛 CENTRE DE DOCUMENTATION GÉRÉ :", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(OR_UGB);

        String ufrAffichage = (codeUfr != null && !codeUfr.trim().isEmpty()) ? "UFR " + codeUfr.trim() : "UFR";
        JLabel lblCode = new JLabel(ufrAffichage, SwingConstants.CENTER);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCode.setForeground(Color.WHITE);

        badgeUfr.add(lblTag, BorderLayout.NORTH);
        badgeUfr.add(lblCode, BorderLayout.CENTER);
        centre.add(badgeUfr);

        bandeau.add(centre, BorderLayout.CENTER);

        // 3. Partie droite : Avatar, Profil & Bouton déconnexion
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);

        if (nomComplet != null && !nomComplet.trim().isEmpty()) {
            JPanel avatar = creerAvatarUtilisateur(nomComplet, 38);
            droite.add(avatar);

            JPanel infoUser = new JPanel(new GridLayout(2, 1, 0, 2));
            infoUser.setOpaque(false);

            JLabel lblUser = new JLabel(nomComplet);
            lblUser.setFont(FONT_GRAS);
            lblUser.setForeground(Color.WHITE);

            JLabel lblRole = new JLabel("Gestionnaire • " + ufrAffichage);
            lblRole.setFont(FONT_PETIT);
            lblRole.setForeground(new Color(255, 230, 160));

            infoUser.add(lblUser);
            infoUser.add(lblRole);
            droite.add(infoUser);
        }

        if (btnAction != null) {
            droite.add(btnAction);
        }

        bandeau.add(droite, BorderLayout.EAST);

        return bandeau;
    }

    /**
     * Bordure avec titre pour carte moderne.
     */
    public static Border creerBordureCarte(String titre) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDURE_DOUCE, 1, true),
                titre,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_GRAS,
                BLEU_UGB
        );
    }

    /**
     * Configure et modernise une JTable :
     * - Hauteur de ligne aérée (32px)
     * - En-tête bleu UGB moderne #1565C0 avec typographie blanche
     * - Alternance de couleurs de lignes
     * - Badges colorés sémantiques arrondis
     * - Tri interactif
     */
    public static void configurerTableModerne(JTable table) {
        table.setRowHeight(32);
        table.setFont(FONT_NORMAL);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXTE_FONCE);
        table.setSelectionBackground(BLEU_SELECTION);
        table.setSelectionForeground(BLEU_SECONDAIRE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 243, 248));

        // En-tête moderne explicite
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_GRAS);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) defaultRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(BLEU_UGB);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(FONT_GRAS);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, BLEU_SECONDAIRE),
                        new EmptyBorder(8, 12, 8, 12)
                ));
                return lbl;
            }
        });

        // Renderer personnalisé pour toutes les cellules avec badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(4, 12, 4, 12));

                String strVal = (value != null) ? value.toString() : "";

                boolean isBadge = false;
                Color badgeBg = null;
                Color badgeFg = null;
                String badgeText = strVal;

                if (strVal.equalsIgnoreCase("TELECHARGEABLE") || strVal.toLowerCase().contains("téléchargeable")) {
                    isBadge = true;
                    badgeBg = BADGE_VERT_FOND;
                    badgeFg = BADGE_VERT_TEXT;
                    badgeText = "✓ " + strVal;
                } else if (strVal.equalsIgnoreCase("CONSULTATION_SEULE") || strVal.toLowerCase().contains("consultation seule")) {
                    isBadge = true;
                    badgeBg = BADGE_BLEU_FOND;
                    badgeFg = BADGE_BLEU_TEXT;
                    badgeText = "👁 " + strVal;
                } else if (strVal.equalsIgnoreCase("RESTREINT") || strVal.toLowerCase().contains("restreint")) {
                    isBadge = true;
                    badgeBg = BADGE_ORANGE_FOND;
                    badgeFg = BADGE_ORANGE_TEXT;
                    badgeText = "🔒 " + strVal;
                } else if (strVal.equalsIgnoreCase("THESE") || strVal.equalsIgnoreCase("THÈSE")) {
                    isBadge = true;
                    badgeBg = BADGE_VIOLET_FOND;
                    badgeFg = BADGE_VIOLET_TEXT;
                } else if (strVal.equalsIgnoreCase("MEMOIRE") || strVal.equalsIgnoreCase("MÉMOIRE")) {
                    isBadge = true;
                    badgeBg = BADGE_TEAL_FOND;
                    badgeFg = BADGE_TEAL_TEXT;
                } else if (strVal.equalsIgnoreCase("ADMIN")) {
                    isBadge = true;
                    badgeBg = BADGE_ROUGE_FOND;
                    badgeFg = BADGE_ROUGE_TEXT;
                } else if (strVal.equalsIgnoreCase("GESTIONNAIRE")) {
                    isBadge = true;
                    badgeBg = BADGE_BLEU_FOND;
                    badgeFg = BADGE_BLEU_TEXT;
                } else if (strVal.equalsIgnoreCase("ETUDIANT") || strVal.equalsIgnoreCase("ÉTUDIANT")) {
                    isBadge = true;
                    badgeBg = BADGE_VERT_FOND;
                    badgeFg = BADGE_VERT_TEXT;
                }

                lbl.setText(badgeText);
                lbl.setOpaque(true);

                if (isSelected) {
                    lbl.setBackground(BLEU_SELECTION);
                    lbl.setFont(FONT_GRAS);
                    lbl.setForeground(isBadge ? badgeFg : BLEU_SECONDAIRE);
                } else {
                    if (isBadge) {
                        lbl.setFont(FONT_BADGE);
                        lbl.setBackground(badgeBg);
                        lbl.setForeground(badgeFg);
                    } else {
                        lbl.setFont(FONT_NORMAL);
                        lbl.setBackground((row % 2 == 0) ? BLANC : LIGNE_ALTERNEE);
                        lbl.setForeground(TEXTE_FONCE);
                    }
                }

                return lbl;
            }
        });

        if (table.getModel() != null) {
            table.setAutoCreateRowSorter(true);
        }
    }

    /**
     * Associe la touche ENTRÉE à une action sur un composant.
     */
    public static void associerToucheEntree(JComponent comp, Runnable action) {
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "actionEntree");
        comp.getActionMap().put("actionEntree", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Ferme une boîte de dialogue avec la touche ÉCHAP.
     */
    public static void associerToucheEchap(JDialog dialog) {
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "fermerDialog");
        dialog.getRootPane().getActionMap().put("fermerDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    /**
     * Séparateur horizontal léger.
     */
    public static JSeparator creerSeparateur() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDURE_DOUCE);
        sep.setBackground(FOND_CLAIR);
        return sep;
    }

    /**
     * Label de titre de section stylisé.
     */
    public static JLabel creerLabelSection(String texte) {
        JLabel lbl = new JLabel(texte.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(BLEU_UGB);
        lbl.setBorder(new EmptyBorder(6, 0, 2, 0));
        return lbl;
    }

    /**
     * Crée une carte statistique avec une bande colorée d'accent en haut et ombre douce.
     */
    public static JPanel creerCarteStatAvecAccent(String titre, JLabel valeur, Color couleurAccent) {
        JPanel carte = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Ombre douce
                g2.setColor(new Color(15, 30, 60, 15));
                g2.fillRoundRect(3, 5, getWidth() - 4, getHeight() - 4, 12, 12);

                // Fond blanc
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 12, 12);

                // Bordure subtile
                g2.setColor(BORDURE_DOUCE);
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 12, 12);

                g2.dispose();
            }
        };
        carte.setOpaque(false);
        carte.setBorder(new EmptyBorder(0, 0, 6, 6));

        // Bande d'accent en haut
        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(couleurAccent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 12, 12);
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(0, 6));
        accent.setOpaque(false);
        carte.add(accent, BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 6));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(12, 16, 14, 16));

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(FONT_GRAS);
        lblTitre.setForeground(TEXTE_SECONDAIRE);
        corps.add(lblTitre, BorderLayout.NORTH);

        valeur.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valeur.setForeground(couleurAccent);
        valeur.setHorizontalAlignment(SwingConstants.LEFT);
        corps.add(valeur, BorderLayout.CENTER);

        carte.add(corps, BorderLayout.CENTER);
        return carte;
    }

    /**
     * Crée un label de champ de formulaire soigné.
     */
    public static JLabel creerLabelChamp(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXTE_SECONDAIRE);
        return lbl;
    }

    /**
     * Crée un badge de niveau d'accès proéminent.
     */
    public static JPanel creerBadgeAccesProeminent(String niveauAcces) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        badge.setOpaque(true);

        Color bgColor;
        Color fgColor;
        String symbole;
        String libelle;

        switch (niveauAcces.toUpperCase()) {
            case "TELECHARGEABLE":
                bgColor = BADGE_VERT_FOND;
                fgColor = BADGE_VERT_TEXT;
                symbole = "✓";
                libelle = "TÉLÉCHARGEABLE";
                break;
            case "CONSULTATION_SEULE":
                bgColor = BADGE_BLEU_FOND;
                fgColor = BADGE_BLEU_TEXT;
                symbole = "👁";
                libelle = "CONSULTATION SEULE";
                break;
            case "RESTREINT":
                bgColor = BADGE_ORANGE_FOND;
                fgColor = BADGE_ORANGE_TEXT;
                symbole = "🔒";
                libelle = "ACCÈS RESTREINT";
                break;
            default:
                bgColor = FOND_CLAIR;
                fgColor = TEXTE_SECONDAIRE;
                symbole = "•";
                libelle = niveauAcces;
        }

        badge.setBackground(bgColor);
        badge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fgColor, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));

        JLabel lbl = new JLabel(symbole + "  " + libelle);
        lbl.setFont(FONT_GRAS);
        lbl.setForeground(fgColor);
        badge.add(lbl);
        return badge;
    }

    /**
     * Barre d'en-tête affichée en haut de chaque fenêtre de l'application.
     * Contient le logo UGB, le titre, le badge de rôle, l'état de la base et l'utilisateur connecté.
     */
    public static JPanel creerBarreEnTete(String titreApp, String sousTitre, String rolePill,
                                          String utilisateur, boolean bdConnectee, JButton btnDeconnexion) {
        JPanel bar = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDURE_DOUCE);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(8, 18, 8, 18));
        bar.setPreferredSize(new Dimension(0, 56));

        // 1. Partie gauche : Logo officiel UGB + Titres
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);

        LogoUGB logo = new LogoUGB(38, 38);
        gauche.add(logo);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 1));
        titleBlock.setOpaque(false);

        JLabel lblTitre = new JLabel(titreApp != null ? titreApp : "Système d'Information Doc UGB");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitre.setForeground(TEXTE_FONCE);

        JLabel lblSous = new JLabel(sousTitre != null ? sousTitre : "PORTAIL DES THÈSES & MÉMOIRES");
        lblSous.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblSous.setForeground(TEXTE_GRIS);

        titleBlock.add(lblTitre);
        titleBlock.add(lblSous);
        gauche.add(titleBlock);
        bar.add(gauche, BorderLayout.WEST);

        // 2. Partie centrale : Badge de rôle + indicateur d'état de la base
        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        centre.setOpaque(false);

        if (rolePill != null && !rolePill.isEmpty()) {
            JPanel pillRole = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BLEU_UGB);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pillRole.setOpaque(false);
            JLabel lblRole = new JLabel(rolePill);
            lblRole.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblRole.setForeground(Color.WHITE);
            pillRole.add(lblRole);
            centre.add(pillRole);
        }

        // Indicateur statut réseau / BD
        JPanel pillStatut = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDURE_DOUCE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pillStatut.setOpaque(false);

        JLabel dotStatut = new JLabel(bdConnectee ? "●" : "○");
        dotStatut.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dotStatut.setForeground(bdConnectee ? VERT_SUCCES : ORANGE_AVERT);

        JLabel txtStatut = new JLabel(bdConnectee ? "Serveur UGB Opérationnel • BD Connectée" : "Mode Mémoire Vive Synchronisé");
        txtStatut.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtStatut.setForeground(TEXTE_SECONDAIRE);

        pillStatut.add(dotStatut);
        pillStatut.add(txtStatut);
        centre.add(pillStatut);

        bar.add(centre, BorderLayout.CENTER);

        // 3. Partie droite : Profil utilisateur & Déconnexion
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);

        if (utilisateur != null && !utilisateur.isEmpty()) {
            JPanel avatar = creerAvatarUtilisateur(utilisateur, 32);
            droite.add(avatar);

            JLabel lblNom = new JLabel(utilisateur);
            lblNom.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblNom.setForeground(TEXTE_FONCE);
            droite.add(lblNom);
        }

        if (btnDeconnexion != null) {
            droite.add(btnDeconnexion);
        }

        bar.add(droite, BorderLayout.EAST);
        return bar;
    }

    /**
     * Bandeau d'accueil sombre utilisé en haut des tableaux de bord (gestionnaire et admin).
     * Affiche un badge institutionnel, un titre principal et des boutons d'action rapide.
     */
    public static JPanel creerBandeauAccueil(String badgeTag, String titrePrincipal,
                                              String description, java.util.List<JButton> actionsRapides) {
        JPanel hero = new PanelDegrade(new Color(15, 23, 42), new Color(30, 58, 138), false);
        hero.setBorder(new EmptyBorder(18, 24, 18, 24));
        hero.setLayout(new BorderLayout(20, 0));

        // Gauche : Tag + Titre + Description
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        if (badgeTag != null && !badgeTag.isEmpty()) {
            JPanel tagPill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(16, 185, 129, 35)); // Vert émeraude subtil
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(16, 185, 129, 120));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            tagPill.setOpaque(false);
            tagPill.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblTag = new JLabel("🏛 " + badgeTag);
            lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTag.setForeground(new Color(110, 231, 183)); // Émeraude clair
            tagPill.add(lblTag);
            left.add(tagPill);
            left.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        JLabel lblTitre = new JLabel(titrePrincipal);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(lblTitre);
        left.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(203, 213, 225));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(lblDesc);

        hero.add(left, BorderLayout.CENTER);

        // Droite : Boutons d'action rapide
        if (actionsRapides != null && !actionsRapides.isEmpty()) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            right.setOpaque(false);
            for (JButton btn : actionsRapides) {
                right.add(btn);
            }
            hero.add(right, BorderLayout.EAST);
        }

        return hero;
    }

    /**
     * Carte d'indicateur clé (KPI) utilisée dans les tableaux de bord.
     */
    public static JPanel creerCarteIndicateur(String titre, String valeur, String detail, String icone, Color accent) {
        JLabel lblVal = new JLabel(valeur);
        return creerCarteIndicateur(titre, lblVal, detail, icone, accent);
    }

    public static JPanel creerCarteIndicateur(String titre, JLabel lblVal, String detail, String icone, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDURE_DOUCE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Haut : Titre + Icône
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitre = new JLabel(titre.toUpperCase());
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitre.setForeground(TEXTE_GRIS);
        top.add(lblTitre, BorderLayout.WEST);

        if (icone != null && !icone.isEmpty()) {
            JLabel lblIcone = new JLabel(icone);
            lblIcone.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblIcone.setForeground(accent != null ? accent : BLEU_UGB);
            top.add(lblIcone, BorderLayout.EAST);
        }
        card.add(top, BorderLayout.NORTH);

        // Milieu : Grande valeur numérique
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(TEXTE_FONCE);
        card.add(lblVal, BorderLayout.CENTER);

        // Bas : Détail / Tendance
        if (detail != null && !detail.isEmpty()) {
            JLabel lblDet = new JLabel(detail);
            lblDet.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDet.setForeground(accent != null ? accent : TEXTE_SECONDAIRE);
            card.add(lblDet, BorderLayout.SOUTH);
        }

        return card;
    }
}
