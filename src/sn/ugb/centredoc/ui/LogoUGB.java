package sn.ugb.centredoc.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Composant de rendu du logo officiel de l'Université Gaston Berger (UGB).
 * Tente de charger l'image PNG (depuis le classpath ou le système de fichiers),
 * et propose un dessin vectoriel de secours de haute qualité si l'image est absente.
 */
public class LogoUGB extends JComponent {
    private static BufferedImage imageLogoCache = null;
    private static boolean imageTente = false;

    private final int largeurCible;
    private final int hauteurCible;
    private final boolean afficherTexte;

    public LogoUGB(int largeur, int hauteur) {
        this(largeur, hauteur, false);
    }

    public LogoUGB(int largeur, int hauteur, boolean afficherTexte) {
        this.largeurCible = largeur;
        this.hauteurCible = hauteur;
        this.afficherTexte = afficherTexte;
        setPreferredSize(new Dimension(largeur, hauteur));
        setOpaque(false);
        chargerImageSiBesoin();
    }

    private static synchronized void chargerImageSiBesoin() {
        if (imageTente) return;
        imageTente = true;

        // 1. Tenter par le ClassLoader (dans le JAR ou bin/)
        String[] ressources = {
                "/resources/logo8UGB.jpg",
                "/resources/logo_ugb.jpg",
                "/resources/logo_ugb.png"
        };
        for (String res : ressources) {
            try (InputStream is = LogoUGB.class.getResourceAsStream(res)) {
                if (is != null) {
                    imageLogoCache = ImageIO.read(is);
                    if (imageLogoCache != null) return;
                }
            } catch (Exception ignored) {}
        }

        // 2. Tenter chemins relatifs courants sur le disque
        String[] cheminsPossibles = {
                "logo8UGB.jpg",
                "com.centre-doc-ugb/logo8UGB.jpg",
                "src/resources/logo8UGB.jpg",
                "src/resources/logo_ugb.jpg",
                "src/resources/logo_ugb.png",
                "bin/resources/logo8UGB.jpg",
                "bin/resources/logo_ugb.jpg",
                "resources/logo8UGB.jpg",
                "resources/logo_ugb.jpg",
                "com.centre-doc-ugb/src/resources/logo8UGB.jpg",
                "com.centre-doc-ugb/src/resources/logo_ugb.jpg",
                "com.centre-doc-ugb/src/resources/logo_ugb.png",
                "C:/Users/ibrah/Desktop/Projet_JAVA/logo8UGB.jpg",
                "C:/Users/ibrah/Downloads/logo_ugb.png"
        };

        for (String chemin : cheminsPossibles) {
            try {
                File f = new File(chemin);
                if (f.exists() && f.isFile()) {
                    imageLogoCache = ImageIO.read(f);
                    if (imageLogoCache != null) return;
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        if (imageLogoCache != null) {
            // Dessin de l'image officielle UGB centrée avec respect du ratio
            int imgW = imageLogoCache.getWidth();
            int imgH = imageLogoCache.getHeight();

            double ratio = Math.min((double) w / imgW, (double) h / imgH);
            int destW = (int) (imgW * ratio);
            int destH = (int) (imgH * ratio);

            int x = (w - destW) / 2;
            int y = (h - destH) / 2;

            // Fond blanc arrondi propre et net
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, destW, destH, 12, 12);

            // Clip arrondi pour intégration parfaite
            Shape oldClip = g2.getClip();
            g2.clip(new java.awt.geom.RoundRectangle2D.Float(x, y, destW, destH, 12, 12));
            g2.drawImage(imageLogoCache, x, y, destW, destH, null);
            g2.setClip(oldClip);

            // Fine bordure dorée/douce
            g2.setColor(new Color(249, 168, 37, 180));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, destW - 1, destH - 1, 12, 12);
        } else {
            // Dessin vectoriel de secours stylisé UGB
            dessinerLogoVectorielSecours(g2, w, h);
        }

        g2.dispose();
    }

    private void dessinerLogoVectorielSecours(Graphics2D g2, int w, int h) {
        int dim = Math.min(w, h);
        int x = (w - dim) / 2;
        int y = (h - dim) / 2;

        // Écusson bleu UGB
        g2.setColor(new Color(21, 101, 192));
        g2.fillRoundRect(x + 2, y + 2, dim - 4, dim - 4, dim / 4, dim / 4);

        // Bordure dorée
        g2.setColor(new Color(249, 168, 37));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(x + 2, y + 2, dim - 4, dim - 4, dim / 4, dim / 4);

        // Lettres "UGB"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (dim * 0.32)));
        FontMetrics fm = g2.getFontMetrics();
        String sigle = "UGB";
        int tx = x + (dim - fm.stringWidth(sigle)) / 2;
        int ty = y + (dim + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(sigle, tx, ty);
    }

    /**
     * Crée un composant d'en-tête combiné avec logo et texte institutionnel.
     */
    public static JPanel creerHeaderLogo(int hauteurLogo, String titre, String sousTitre) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);

        LogoUGB logo = new LogoUGB(hauteurLogo, hauteurLogo);
        panel.add(logo);

        JPanel textes = new JPanel(new GridLayout(2, 1, 0, 2));
        textes.setOpaque(false);

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitre.setForeground(Color.WHITE);

        JLabel lblSous = new JLabel(sousTitre);
        lblSous.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSous.setForeground(new Color(225, 235, 245));

        textes.add(lblTitre);
        textes.add(lblSous);

        panel.add(textes);
        return panel;
    }
}
