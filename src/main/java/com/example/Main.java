package com.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        flappyBird.requestFocusInWindow();
    }
}
