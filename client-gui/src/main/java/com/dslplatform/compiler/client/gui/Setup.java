package com.dslplatform.compiler.client.gui;

import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class Setup {
    public static void setLookAndFeel(final boolean decorated) {
        Toolkit.getDefaultToolkit().setDynamicLayout(decorated);
        System.setProperty("sun.awt.noerasebackground",
                String.valueOf(decorated));

        JFrame.setDefaultLookAndFeelDecorated(decorated);
        JDialog.setDefaultLookAndFeelDecorated(decorated);
    }
}
