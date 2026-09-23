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
import java.awt.event.KeyEvent;

/**
 * Utilitaires d'interface graphique pour assurer une charte graphique élégante,
 * moderne, hautement accessible, cohérente et fidèle à l'Université Gaston Berger (UGB).
 * Résout tout conflit de contraste (pas d'écritures invisibles ou de blanc-sur-gris sous Windows).
 */
public class UIUtils {
    // =========================================================================
    // Palette institutionnelle UGB (Haut Contraste & Accessibilité WCAG)
    // =========================================================================
    public static final Color BLEU_UGB = new Color(10, 45, 95);           // Bleu nuit #0A2D5F
    public static final Color BLEU_SECONDAIRE = new Color(24, 75, 145);   // Bleu intermédiaire #184B91
    public static final Color BLEU_HOVER = new Color(18, 65, 130);
    public static final Color BLEU_PRESSE = new Color(6, 30, 65);
    public static final Color BLEU_SELECTION = new Color(218, 232, 252);   // Fond de sélection tableau

    public static final Color OR_UGB = new Color(212, 160, 23);           // Doré ambre UGB #D4A017
    public static final Color OR_HOVER = new Color(230, 178, 35);
    public static final Color OR_PRESSE = new Color(185, 138, 15);
    public static final Color TEXTE_ACCENT_DORE = new Color(145, 95, 0);  // Doré sombre très lisible sur fond blanc

    public static final Color FOND_CLAIR = new Color(245, 247, 250);      // Fond de fenêtre doux et moderne
    public static final Color FOND_CARTE = Color.WHITE;
    public static final Color BLANC = Color.WHITE;

    // Typographie à fort contraste (évite toute écriture délavée ou invisible)
    public static final Color TEXTE_FONCE = new Color(33, 37, 41);        // #212529 (Charcoal sombre, contraste > 15:1)
    public static final Color TEXTE_SECONDAIRE = new Color(73, 80, 87);   // #495057 (Gris sombre contrasté)
    public static final Color TEXTE_GRIS = new Color(100, 110, 120);      // #646E78 (Contraste suffisant)
    public static final Color BORDURE_DOUCE = new Color(206, 212, 218);   // #CED4DA
    public static final Color LIGNE_ALTERNEE = new Color(248, 250, 254);

    // Couleurs sémantiques nettes
    public static final Color VERT_SUCCES = new Color(21, 115, 50);
    public static final Color ORANGE_AVERT = new Color(185, 75, 0);
    public static final Color ROUGE_DANGER = new Color(195, 30, 45);

    // Badges / Pilules d'état (fond doux pastel + texte sombre très contrasté)
    public static final Color BADGE_VERT_FOND = new Color(220, 245, 225);
    public static final Color BADGE_VERT_TEXT = new Color(15, 105, 45);

    public static final Color BADGE_BLEU_FOND = new Color(225, 238, 254);
    public static final Color BADGE_BLEU_TEXT = new Color(15, 80, 175);

    public static final Color BADGE_ORANGE_FOND = new Color(254, 236, 224);
    public static final Color BADGE_ORANGE_TEXT = new Color(175, 50, 10);

    public static final Color BADGE_ROUGE_FOND = new Color(253, 232, 234);
    public static final Color BADGE_ROUGE_TEXT = new Color(165, 25, 35);

    public static final Color BADGE_VIOLET_FOND = new Color(240, 232, 252);
    public static final Color BADGE_VIOLET_TEXT = new Color(95, 30, 155);

    public static final Color BADGE_TEAL_FOND = new Color(225, 246, 246);
    public static final Color BADGE_TEAL_TEXT = new Color(0, 105, 105);

    // Polices standardisées (Segoe UI)
    public static final Font FONT_GRAND_TITRE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITRE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SOUS_TITRE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_GRAS = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_PETIT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 28);

    /**
     * Initialise les propriétés graphiques globales de Swing (UIManager) pour garantir
     * une cohérence visuelle intégrale, un rendu net des polices et éviter les bugs de thèmes Windows.
     */
    public static void initialiserTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Forcer le rendu anti-crénelé des polices
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Paramètres par défaut UIManager
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
        UIManager.put("Table.selectionForeground", BLEU_UGB);
        UIManager.put("Table.gridColor", new Color(235, 238, 242));
        UIManager.put("TitledBorder.titleColor", BLEU_UGB);
    }

    /**
     * Composant bouton personnalisé avec dessin vectoriel 2D anti-crénelé.
     * Immunisé contre le bug de Windows L&F (qui repeignait les boutons en gris natif avec texte blanc invisible).
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
            setBorder(new EmptyBorder(8, 16, 8, 16));
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
                bg = new Color(235, 238, 242);
                fg = new Color(130, 138, 148);
                border = new Color(215, 220, 226);
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

            // Dessin du fond arrondi
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, rayon, rayon);

            // Dessin de la bordure
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, rayon, rayon);
            }

            // Dessin du texte parfaitement centré
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
            int w = fm.stringWidth(getText()) + 32;
            int h = fm.getHeight() + 16;
            return new Dimension(Math.max(w, super.getPreferredSize().width), Math.max(h, 34));
        }
    }

    /**
     * Crée un bouton primaire stylisé (Bleu institutionnel UGB, texte blanc, contrasté).
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
                8
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un bouton secondaire sobre et soigné (Fond blanc/gris clair, texte sombre charcoal, bordure douce).
     */
    public static JButton creerBoutonSecondaire(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                BLANC,
                new Color(242, 245, 249),
                new Color(230, 235, 242),
                TEXTE_FONCE,
                TEXTE_FONCE,
                BORDURE_DOUCE,
                8
        );
        btn.setFont(FONT_NORMAL);
        return btn;
    }

    /**
     * Crée un bouton d'accentuation (Doré UGB ambré avec texte sombre hautement lisible).
     */
    public static JButton creerBoutonAccent(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                OR_UGB,
                OR_HOVER,
                OR_PRESSE,
                new Color(20, 25, 30),
                new Color(20, 25, 30),
                new Color(180, 130, 10),
                8
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un bouton de suppression / action critique (Rouge danger très visible, texte net).
     */
    public static JButton creerBoutonDanger(String texte) {
        BoutonModerne btn = new BoutonModerne(
                texte,
                new Color(254, 242, 242),
                new Color(220, 53, 69),
                new Color(185, 35, 50),
                new Color(185, 25, 40),
                Color.WHITE,
                new Color(245, 195, 200),
                8
        );
        btn.setFont(FONT_GRAS);
        return btn;
    }

    /**
     * Crée un champ de texte aéré avec marge interne, bordure soignée et fond blanc garanti.
     */
    public static JTextField creerChampTexte(int colonnes) {
        JTextField tf = new JTextField(colonnes);
        tf.setFont(FONT_NORMAL);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXTE_FONCE);
        tf.setCaretColor(TEXTE_FONCE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDURE_DOUCE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    /**
     * Crée un champ de mot de passe aéré avec fond blanc garanti.
     */
    public static JPasswordField creerChampMotDePasse(int colonnes) {
        JPasswordField pf = new JPasswordField(colonnes);
        pf.setFont(FONT_NORMAL);
        pf.setBackground(Color.WHITE);
        pf.setForeground(TEXTE_FONCE);
        pf.setCaretColor(TEXTE_FONCE);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDURE_DOUCE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return pf;
    }

    /**
     * Bandeau d'en-tête supérieur institutionnel UGB (Bleu profond, typographie blanche et dorée).
     */
    public static JPanel creerBandeauHaut(String titre, String sousTitre, String infoUtilisateur) {
        JPanel bandeau = new JPanel(new BorderLayout());
        bandeau.setBackground(BLEU_UGB);
        bandeau.setBorder(new EmptyBorder(14, 22, 14, 22));

        JPanel textePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textePanel.setOpaque(false);

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(FONT_TITRE);
        lblTitre.setForeground(BLANC);

        JLabel lblSousTitre = new JLabel(sousTitre);
        lblSousTitre.setFont(FONT_PETIT);
        lblSousTitre.setForeground(new Color(205, 225, 255));

        textePanel.add(lblTitre);
        textePanel.add(lblSousTitre);

        bandeau.add(textePanel, BorderLayout.WEST);

        if (infoUtilisateur != null) {
            JLabel lblUser = new JLabel("[U] " + infoUtilisateur);
            lblUser.setFont(FONT_GRAS);
            lblUser.setForeground(new Color(255, 235, 175));
            bandeau.add(lblUser, BorderLayout.EAST);
        }

        return bandeau;
    }

    /**
     * Bordure avec titre pour carte moderne (Titre bleu UGB lisible et contrasté).
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
     * - Hauteur de ligne aérée (30px)
     * - En-tête bleu nuit UGB avec police grasse blanche (Résout le bug de l'en-tête blanc sur gris de Windows)
     * - Alternance de couleurs de lignes (striping zébré doux)
     * - Tri interactif par clic sur colonne
     * - Rendu de badges colorés pour les statuts, types et rôles avec fort contraste
     */
    public static void configurerTableModerne(JTable table) {
        table.setRowHeight(30);
        table.setFont(FONT_NORMAL);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXTE_FONCE);
        table.setSelectionBackground(BLEU_SELECTION);
        table.setSelectionForeground(BLEU_UGB);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 238, 242));

        // En-tête moderne explicite (évite la réécriture par Windows Look and Feel)
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_GRAS);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
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
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(24, 75, 145)),
                        new EmptyBorder(7, 10, 7, 10)
                ));
                return lbl;
            }
        });

        // Renderer personnalisé pour toutes les cellules : alternance de lignes + badges sémantiques
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));

                String strVal = (value != null) ? value.toString() : "";

                // Détection sémantique des badges
                boolean isBadge = false;
                Color badgeBg = null;
                Color badgeFg = null;
                String badgeText = strVal;

                if (strVal.equalsIgnoreCase("TELECHARGEABLE") || strVal.toLowerCase().contains("téléchargeable")) {
                    isBadge = true;
                    badgeBg = BADGE_VERT_FOND;
                    badgeFg = BADGE_VERT_TEXT;
                    badgeText = "✔ " + strVal;
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
                    if (isBadge) {
                        lbl.setForeground(badgeFg);
                    } else {
                        lbl.setForeground(BLEU_UGB);
                    }
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

        // Activation du tri dynamique interactif
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
     * Separateur horizontal leger pour structurer les formulaires et volets.
     */
    public static JSeparator creerSeparateur() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDURE_DOUCE);
        sep.setBackground(FOND_CLAIR);
        return sep;
    }

    /**
     * Label de titre de section stylise (petite majuscule bleue UGB + espacement).
     */
    public static JLabel creerLabelSection(String texte) {
        JLabel lbl = new JLabel(texte.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(BLEU_SECONDAIRE);
        lbl.setBorder(new EmptyBorder(6, 0, 2, 0));
        return lbl;
    }

    /**
     * Cree une carte statistique avec une bande coloree en haut (accent moderne).
     * @param titre         Libelle de la carte
     * @param valeur        Label de la valeur principale
     * @param couleurAccent Couleur de l'accent en haut
     */
    public static JPanel creerCarteStatAvecAccent(String titre, JLabel valeur, Color couleurAccent) {
        JPanel carte = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Ombre portee legere
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(3, 5, getWidth() - 4, getHeight() - 4, 10, 10);
                // Fond blanc
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 10, 10);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 10, 10);
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(0, 5));
        accent.setOpaque(false);
        carte.add(accent, BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout());
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(10, 14, 12, 14));

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(FONT_GRAS);
        lblTitre.setForeground(TEXTE_SECONDAIRE);
        corps.add(lblTitre, BorderLayout.NORTH);

        valeur.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valeur.setForeground(couleurAccent);
        valeur.setHorizontalAlignment(SwingConstants.LEFT);
        corps.add(valeur, BorderLayout.CENTER);

        carte.add(corps, BorderLayout.CENTER);
        return carte;
    }

    /**
     * Cree un label de champ de formulaire (en haut, petit, bleu).
     */
    public static JLabel creerLabelChamp(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXTE_SECONDAIRE);
        return lbl;
    }

    /**
     * Cree un badge de niveau d'acces proeminent (grand, centre, avec symbole ASCII).
     */
    public static JPanel creerBadgeAccesProeminent(String niveauAcces) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        badge.setOpaque(true);

        Color bgColor;
        Color fgColor;
        String symbole;
        String libelle;

        switch (niveauAcces.toUpperCase()) {
            case "TELECHARGEABLE":
                bgColor = new Color(220, 245, 225);
                fgColor = new Color(15, 105, 45);
                symbole = "[+]";
                libelle = "TELECHARGEABLE";
                break;
            case "CONSULTATION_SEULE":
                bgColor = new Color(225, 238, 254);
                fgColor = new Color(15, 80, 175);
                symbole = "[o]";
                libelle = "CONSULTATION SEULE";
                break;
            case "RESTREINT":
                bgColor = new Color(254, 236, 224);
                fgColor = new Color(175, 50, 10);
                symbole = "[x]";
                libelle = "ACCES RESTREINT";
                break;
            default:
                bgColor = FOND_CLAIR;
                fgColor = TEXTE_SECONDAIRE;
                symbole = "[ ]";
                libelle = niveauAcces;
        }

        badge.setBackground(bgColor);
        badge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fgColor.brighter(), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lbl = new JLabel(symbole + " " + libelle);
        lbl.setFont(FONT_GRAS);
        lbl.setForeground(fgColor);
        badge.add(lbl);
        return badge;
    }
}
