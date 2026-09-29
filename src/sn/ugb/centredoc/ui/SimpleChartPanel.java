package sn.ugb.centredoc.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Composant graphique moderne en Java 2D pur (sans bibliothèque externe).
 * Répond au module bonus 3.3 du sujet officiel : "Graphiques de statistiques".
 * Affiche des diagrammes en barres ou en anneau (donut chart) avec rendu anti-crénelé.
 */
public class SimpleChartPanel extends JPanel {

    public enum TypeGraphique {
        BARRES_HORIZONTALES,
        ANNEAU_CAMEMBERT
    }

    private TypeGraphique typeGraphique;
    private String titre;
    private Map<String, Integer> donnees;

    // Palette harmonieuse et lumineuse de couleurs pour les secteurs / barres
    private static final Color[] COULEURS = {
            UIUtils.BLEU_UGB,         // #1565C0 - Bleu UGB
            UIUtils.OR_UGB,           // #F9A825 - Ambre chaud UGB
            UIUtils.ACCENT_EMERAUDE,  // #00897B - Émeraude académique
            new Color(30, 136, 229),  // #1E88E5 - Bleu ciel moderne
            new Color(142, 36, 170),  // #8E24AA - Violet / Indigo
            new Color(251, 140, 0),   // #FB8C00 - Orange chaleureux
            UIUtils.VERT_SUCCES,      // #2E7D32 - Vert frais
            UIUtils.ROUGE_DANGER      // #C62828 - Rouge corail
    };

    public SimpleChartPanel(String titre, TypeGraphique typeGraphique) {
        this.titre = titre;
        this.typeGraphique = typeGraphique;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDURE_DOUCE, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
    }

    public void setDonnees(Map<String, Integer> nouvellesDonnees) {
        this.donnees = nouvellesDonnees;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dessin du titre
        g2.setFont(UIUtils.FONT_GRAS);
        g2.setColor(UIUtils.BLEU_UGB);
        g2.drawString(titre, 16, 22);

        if (donnees == null || donnees.isEmpty()) {
            g2.setFont(UIUtils.FONT_NORMAL);
            g2.setColor(UIUtils.TEXTE_GRIS);
            g2.drawString("Aucune donnée statistique disponible à afficher.", 20, getHeight() / 2);
            g2.dispose();
            return;
        }

        if (typeGraphique == TypeGraphique.BARRES_HORIZONTALES) {
            dessinerBarresHorizontales(g2);
        } else {
            dessinerAnneau(g2);
        }

        g2.dispose();
    }

    private void dessinerBarresHorizontales(Graphics2D g2) {
        int startY = 46;
        int maxLargeurBarre = getWidth() - 220;
        if (maxLargeurBarre < 50) maxLargeurBarre = 50;

        int maxValeur = 1;
        for (int val : donnees.values()) {
            if (val > maxValeur) maxValeur = val;
        }

        int hauteurBarre = 22;
        int espacement = 10;
        int index = 0;

        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            int y = startY + index * (hauteurBarre + espacement);
            if (y + hauteurBarre > getHeight() - 10) break;

            Color couleurBarre = COULEURS[index % COULEURS.length];

            // Libellé de la catégorie
            g2.setFont(UIUtils.FONT_NORMAL);
            g2.setColor(UIUtils.TEXTE_FONCE);
            String libelle = entry.getKey();
            if (libelle.length() > 18) libelle = libelle.substring(0, 16) + "…";
            g2.drawString(libelle, 16, y + 16);

            // Calcul de la largeur proportionnelle
            int largeur = (int) (((double) entry.getValue() / maxValeur) * maxLargeurBarre);
            if (largeur < 6 && entry.getValue() > 0) largeur = 6;

            int xBarre = 140;

            // Barre avec dégradé subtil
            GradientPaint gradient = new GradientPaint(
                    xBarre, y, couleurBarre,
                    xBarre + largeur, y, couleurBarre.brighter()
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(xBarre, y, largeur, hauteurBarre, 8, 8);

            // Valeur numérique affichée à côté
            g2.setFont(UIUtils.FONT_GRAS);
            g2.setColor(couleurBarre.darker());
            g2.drawString(String.valueOf(entry.getValue()), xBarre + largeur + 8, y + 16);

            index++;
        }
    }

    private void dessinerAnneau(Graphics2D g2) {
        int total = 0;
        for (int v : donnees.values()) total += v;

        int diametre = Math.min(getWidth() - 180, getHeight() - 60);
        if (diametre < 60) diametre = 60;

        int xCentre = 30;
        int yCentre = 40;

        if (total == 0) {
            g2.setFont(UIUtils.FONT_NORMAL);
            g2.setColor(UIUtils.TEXTE_GRIS);
            g2.drawString("Total : 0", xCentre + 20, yCentre + diametre / 2);
            return;
        }

        double angleDebut = 0.0;
        int index = 0;

        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            double arcAngle = (entry.getValue() * 360.0) / total;
            Color col = COULEURS[index % COULEURS.length];
            g2.setColor(col);
            g2.fillArc(xCentre, yCentre, diametre, diametre, (int) Math.round(angleDebut), (int) Math.ceil(arcAngle));
            angleDebut += arcAngle;
            index++;
        }

        // Trou central pour effet donut moderne
        int trouDiametre = (int) (diametre * 0.55);
        int xTrou = xCentre + (diametre - trouDiametre) / 2;
        int yTrou = yCentre + (diametre - trouDiametre) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(xTrou, yTrou, trouDiametre, trouDiametre);

        // Texte central dans le trou du donut
        g2.setFont(UIUtils.FONT_PETIT);
        g2.setColor(UIUtils.TEXTE_GRIS);
        g2.drawString("TOTAL", xTrou + trouDiametre / 2 - 16, yTrou + trouDiametre / 2 - 6);
        g2.setFont(UIUtils.FONT_TITRE);
        g2.setColor(UIUtils.BLEU_UGB);
        String strTotal = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(strTotal, xTrou + (trouDiametre - fm.stringWidth(strTotal)) / 2, yTrou + trouDiametre / 2 + 16);

        // Légende latérale
        int xLegende = xCentre + diametre + 30;
        int yLegende = 50;
        index = 0;

        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            if (yLegende > getHeight() - 16) break;
            Color col = COULEURS[index % COULEURS.length];

            g2.setColor(col);
            g2.fillRoundRect(xLegende, yLegende, 12, 12, 4, 4);

            double pct = (total > 0) ? (entry.getValue() * 100.0 / total) : 0;
            String text = String.format("%s : %d (%.0f%%)", entry.getKey(), entry.getValue(), pct);

            g2.setFont(UIUtils.FONT_PETIT);
            g2.setColor(UIUtils.TEXTE_FONCE);
            g2.drawString(text, xLegende + 18, yLegende + 10);

            yLegende += 22;
            index++;
        }
    }
}
