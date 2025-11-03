package com.mycompany.bst_du.gui;

import com.mycompany.bst_du.gui.model.GuiModel;
import com.mycompany.bst_du.gui.controller.GuiController;
import com.mycompany.bst_du.gui.view.MainFrame;

import javax.swing.*;

/**
 * SK: Hlavný vstupný bod GUI aplikácie.
 * Inicializuje model, kontrolér a hlavné okno (MainFrame) v rámci Swing vlákna.
 */
public final class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiModel model = new GuiModel();
            GuiController ctl = new GuiController(model);
            MainFrame f = new MainFrame(ctl);
            f.setVisible(true);
        });
    }
}
