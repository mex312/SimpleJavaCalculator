package org.example;

import javax.swing.*;
import java.awt.*;
import java.beans.BeanProperty;

public class CustomJButton extends JButton {


    Color hoveredColor;

    @BeanProperty(expert = false, description = "Color which button takes when cursor hovers over it")
    public void setHoveredColor(Color col) {hoveredColor = col;}
    public Color getHoveredColor() {return hoveredColor;}

    public CustomJButton() {
        this("");
    }

    public CustomJButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
        super.setFocusable(false);
        super.setBorderPainted(false);
        super.setForeground(Color.white);
    }

    @Override
    public void paintComponent(Graphics g) {
        Color background = getBackground();
        Color hovered = hoveredColor;
        Color pressed = new Color(Math.max(hovered.getRed() - 20, 0), Math.max(hovered.getGreen() - 20, 0), Math.max(hovered.getBlue() - 20, 0));

        if(getModel().isPressed())
            g.setColor(pressed);
        else if(getModel().isRollover())
            g.setColor(hovered);
        else
            g.setColor(getBackground());

        g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        super.paintComponent(g);
    }


    @Override
    public void setContentAreaFilled(boolean b) {}

    @Override
    public void setBorderPainted(boolean b) {}
}
