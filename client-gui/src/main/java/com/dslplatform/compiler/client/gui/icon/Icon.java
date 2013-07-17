package com.dslplatform.compiler.client.gui.icon;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public enum Icon {
    LOGO_16("/logo-16.png"),
    LOGO_32("/logo-32.png"),
    LOGO_64("/logo-64.png"),
    LOGIN("/login.png");

    public final Image image;
    public final ImageIcon icon;
    public final int width, height;

    private Icon(final String path) {
        try {
            image = ImageIO.read(Icon.class.getResourceAsStream(path));
            icon = new ImageIcon(image);

            width = image.getWidth(null);
            height = image.getHeight(null);
        }
        catch (final IOException e) {
            throw new RuntimeException("Could not load icon Resource!");
        }
    }
}
