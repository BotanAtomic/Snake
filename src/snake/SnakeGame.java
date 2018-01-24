package snake;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Random;


public class SnakeGame {
    private static int LAYOUT_X = 500;
    private static int LAYOUT_Y = 500;

    private static int CELL_SIZE = 10;

    private final ImageView imageView;
    private final Stage stage;

    private int speed;
    private Snake snake;
    private Apple apple;
    private boolean active = true;

    private int level = 1;


    private boolean allowRestart = false;

    private Direction direction = Direction.NORTH;

    SnakeGame(ImageView imageView, Stage stage) {
        this.imageView = imageView;
        this.stage = stage;

        stage.getScene().setOnKeyPressed((key) -> {
            switch (key.getCode()) {
                case RIGHT:
                    changeDirection(SnakeGame.Direction.EST);
                    break;
                case LEFT:
                    changeDirection(SnakeGame.Direction.WEST);
                    break;
                case UP:
                    changeDirection(SnakeGame.Direction.NORTH);
                    break;
                case DOWN:
                    changeDirection(SnakeGame.Direction.SOUTH);
                    break;
                case ENTER:
                    if (allowRestart)
                        start();
                    break;
            }
        });

        start();
    }

    private void start() {
        new Thread(() -> {
            allowRestart = false;
            this.apple = Apple.random();
            this.snake = new Snake(LAYOUT_X / 2, LAYOUT_Y / 2);

            while (active) {
                try {
                    run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void changeDirection(Direction direction) {
        if (Direction.opposite(direction) == this.direction)
            return;

        this.direction = direction;
    }

    private void onCrash() {
        this.active = false;
        this.speed = 0;
        this.level = 1;
        this.allowRestart = true;

        Platform.runLater(() -> stage.setTitle("Snake - Perdu"));

        drawEnd();
    }

    private void onEat() {
        this.apple = Apple.random();
        this.snake.add(direction);
        speed += 8;

        Platform.runLater(() -> stage.setTitle("Snake - Level " + ++level));
    }

    private void run() throws InterruptedException {
        snake.move(direction);

        Queue first = snake.queueList.getFirst();

        if (first.x < 0 || first.y < 0 || first.x > LAYOUT_X || first.y > LAYOUT_Y) {
            onCrash();
            return;
        }

        if (snake.queueList.stream().filter(q -> !first.equals(q)).anyMatch(q -> onZoneOf(q.x, q.y, first.x, first.y))) {
            onCrash();
            return;
        }

        if (onZoneOf(first.x, first.y, apple.x, apple.y))
            onEat();

        draw();

        Thread.sleep(150 - speed < 30 ? 30 : 150 - speed);
    }

    private boolean onZoneOf(int x, int y, int x1, int y1) {
        return x == x1 && y == y1;
    }

    private void draw() {
        BufferedImage bufferedImage = new BufferedImage(LAYOUT_X, LAYOUT_Y, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        graphics.drawString("O", apple.x, apple.y);

        snake.queueList.forEach(q -> graphics.drawString("X", q.x, q.y));

        imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
    }

    private void drawEnd() {
        BufferedImage bufferedImage = new BufferedImage(LAYOUT_X, LAYOUT_Y, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        FontMetrics metrics = graphics.getFontMetrics();

        try {

            Image end = ImageIO.read(getClass().getResource("end.gif"));

            graphics.drawImage(end, LAYOUT_X / 2 - end.getWidth(null) / 2, LAYOUT_Y / 2 - end.getHeight(null) / 2, null);

            String scoreText = "Niveau " + level + " ? Peux mieux faire !";
            String restartText = "Tapez ENTRER pour recommencer";

            graphics.drawString(scoreText, LAYOUT_X / 2 - (metrics.stringWidth(scoreText)) / 2, LAYOUT_Y / 2 + end.getHeight(null));
            graphics.drawString(restartText, LAYOUT_X / 2 - (metrics.stringWidth(restartText)) / 2,
                    LAYOUT_Y / 2 + end.getHeight(null) + 25);

        } catch (IOException e) {
            e.printStackTrace();
        }


        imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
    }

    protected enum Direction {
        NORTH,
        SOUTH,
        EST,
        WEST;

        private static Direction opposite(Direction direction) {
            return direction == NORTH ? SOUTH : direction == SOUTH ? NORTH : direction == WEST ? EST : direction == EST ? WEST : null;
        }

    }

    private static class Apple {
        private int x;
        private int y;

        private Apple(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private static Apple random() {
            int x = random(LAYOUT_X);
            int y = random(LAYOUT_Y);

            System.out.printf("New apple %d / %d \n", x, y);

            return new Apple(x, y);
        }

        private static int random(int size) {
            int i = new Random().nextInt(size);

            while (i % CELL_SIZE != 0 && i > 0)
                i = new Random().nextInt(size);

            return i;
        }

    }

    private static class Snake {
        private ArrayDeque<Queue> queueList = new ArrayDeque<>();

        private Snake(int x, int y) {
            queueList.addFirst(new Queue(x, y));
        }

        private void move(Direction direction) {
            Pair<Integer, Integer> next = queueList.getFirst().nextPosition(direction);

            queueList.removeLast();

            queueList.addFirst(new Queue(next.getKey(), next.getValue()));
        }

        private void add(Direction direction) {
            Pair<Integer, Integer> next = queueList.getFirst().nextPosition(direction);

            queueList.addFirst(new Queue(next.getKey(), next.getValue()));
        }

    }

    private static class Queue {
        private int x;
        private int y;

        private Queue(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private Pair<Integer, Integer> nextPosition(Direction direction) {
            switch (direction) {
                case EST:
                    return new Pair<>(x + CELL_SIZE, y);
                case WEST:
                    return new Pair<>(x - CELL_SIZE, y);
                case NORTH:
                    return new Pair<>(x, y - CELL_SIZE);
                case SOUTH:
                    return new Pair<>(x, y + CELL_SIZE);
            }
            return null;
        }

    }

}
