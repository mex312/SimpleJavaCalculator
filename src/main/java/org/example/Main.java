package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Main {
    static HashMap<Character, Boolean> keys = new HashMap<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        GUI gui = new GUI();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(gui.getRoot());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setBackground(new Color(30, 31, 34));
        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(keys.getOrDefault(e.getKeyChar(), true)){
//                    gui.listen(e);
                    keys.put(e.getKeyChar(), false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys.put(e.getKeyChar(), true);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                gui.listen(e);
            }
        });
    }
}