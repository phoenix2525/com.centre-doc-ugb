package sn.ugb.centredoc;

import sn.ugb.centredoc.ui.LoginFrame;

import javax.swing.*;

/**
 * Point d'entrée principal de l'application :
 * Système d'Information des Centres de Documentation de l'UGB (Gestion des thèses et mémoires).
 * UE : Programmation Orientée Objet 2 (L3 Informatique / MIAGE - UGB).
 */
public class Main {
    public static void main(String[] args) {
        // Initialisation de la charte graphique UGB et du Look and Feel moderne
        sn.ugb.centredoc.ui.UIUtils.initialiserTheme();

        // Lancement de l'interface graphique sur le thread Swing dédié (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
