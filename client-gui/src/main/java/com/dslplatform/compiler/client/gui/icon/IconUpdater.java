package com.dslplatform.compiler.client.gui.icon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class IconUpdater extends WindowAdapter {
    private final JFrame frame;

    public IconUpdater(final JFrame frame) {
        this.frame = frame;
        frame.addWindowListener(this);
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
        updateIcon(false);
    }

    @Override
    public void windowActivated(final WindowEvent e) {
        updateIcon(true);
    }

    private Color lastColor = Color.white;

    private void updateIcon(final boolean active) {
        final boolean hidden = 1 == frame.getState();

        final List<Image> iconList = new ArrayList<Image>();
        iconList.add(Icon.LOGO_64.image);
        iconList.add(Icon.LOGO_32.image);

        final Image img = Icon.LOGO_16.image;
        final int w = img.getWidth(null);
        final int h = img.getHeight(null);

        final Color bgColor = hidden
            ? lastColor
            : ((Color) UIManager.get(active ? "activeCaption" : "inactiveCaption"));

        lastColor = bgColor;

        final BufferedImage fixedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        final Graphics g = fixedImage.getGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, w, h);
        g.drawImage(img, 0, 0, null);
        g.dispose();

        iconList.add(fixedImage);
        frame.setIconImages(iconList);
    }
}
