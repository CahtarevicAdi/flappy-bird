package com.example;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;


    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;


    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }


    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }


    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    Clip passClip;
    Clip gameOverClip;
    Clip backgroundClip;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);


        backgroundImg = new ImageIcon(getClass().getClassLoader().getResource("flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getClassLoader().getResource("flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getClassLoader().getResource("toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getClassLoader().getResource("bottompipe.png")).getImage();


        try {
            passClip = AudioSystem.getClip();
            passClip.open(AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("pass.wav")));

            gameOverClip = AudioSystem.getClip();
            gameOverClip.open(AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("gameover.wav")));


            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("background.wav")));
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        bird = new Bird(birdImg);
        pipes = new ArrayList<>();


        placePipesTimer = new Timer(1500, e -> placePipes());
        placePipesTimer.start();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
                passClip.setFramePosition(0);
                passClip.start();
            }

            if (collision(bird, pipe)) {
                gameOver = true;
                gameOverClip.setFramePosition(0);
                gameOverClip.start();
            }
        }

        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);

        if (bird.y > boardHeight) {
            gameOver = true;
        }


        if (score > 0 && score % 20 == 0) {
            if (velocityX > -8) {
                velocityX -= 0.8;
            }
            if (placePipesTimer.getDelay() > 1200) {
                placePipesTimer.setDelay(placePipesTimer.getDelay() - 50);
            }
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        } else {
            placePipesTimer.stop();
            gameLoop.stop();
            backgroundClip.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                resetGame();
            }
        }
    }

    public void resetGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        velocityX = -4;
        placePipesTimer.setDelay(1500);

        backgroundClip.setFramePosition(0);
        backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        backgroundClip.start();

        gameLoop.start();
        placePipesTimer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
