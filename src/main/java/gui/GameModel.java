package gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Игровая модель.
 * Отвечает за математику, физику, очки и состояния.
 */
public class GameModel {

    public enum GameState { PLAYING, GAME_OVER }
    public static final int ROAD_WIDTH = 300;
    public static final int CAR_WIDTH = 40;
    public static final int CAR_HEIGHT = 70;
    private static final double BASE_SPEED = 3.0;
    private final int playerSpeedX = 5;

    // ПАРАМЕТРЫ ИГРЫ И СОСТОЯНИЕ
    private volatile GameState gameState = GameState.PLAYING;
    private volatile DifficultyLevel currentDifficulty = DifficultyLevel.NORMAL;
    
    private volatile int m_playerX = 0;
    private volatile boolean movingLeft = false;
    private volatile boolean movingRight = false;
    
    private volatile double distanceTravalled = 0;
    private volatile int score = 0;
    private volatile double currentWorldSpeed = BASE_SPEED;
    private double roadOffset = 0;

    // ПРЕПЯТСТВИЯ
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Random random = new Random();
    private int spawnTimer = 0;

    // РЕКОРДЫ
    private final ScoreManager scoreManager = new ScoreManager();

    /**
     * Внутренний класс препятствия
     */
    public static class Obstacle {
        public int xOffset;
        public double y;
        public double speed;
    }

    // МЕТОДЫ УПРАВЛЕНИЯ
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
    
    public void setDifficulty(DifficultyLevel difficulty) {
        this.currentDifficulty = difficulty;
        restartGame();
    }

    public void restartGame() {
        m_playerX = 0; 
        distanceTravalled = 0;
        score = 0;
        currentWorldSpeed = BASE_SPEED * currentDifficulty.getSpeedMultiplier();
        obstacles.clear();
        scoreManager.loadScore();
        gameState = GameState.PLAYING;
    }

    /**
     * Главный метод физики. Вызывается таймером.
     * Принимает ширину и высоту окна, чтобы понимать границы экрана.
     */
    public void updatePhysics(int width, int height) {
        if (gameState == GameState.GAME_OVER || width == 0 || height == 0) {
            return;
        }

        // ДВИЖЕНИЕ ИГРОКА
        if (movingLeft && !movingRight) m_playerX -= playerSpeedX;
        if (movingRight && !movingLeft) m_playerX += playerSpeedX;

        // ОГРАНИЧЕНИЕ ВЫХОДА ЗА ПРЕДЕЛЫ ДОРОГИ
        int maxLimit = ROAD_WIDTH / 2 - CAR_WIDTH / 2;
        if (m_playerX < -maxLimit) m_playerX = -maxLimit;
        if (m_playerX > maxLimit) m_playerX = maxLimit;

        // НАЧИСЛЕНИЕ ОЧКОВ И УСКОРЕНИЕ
        currentWorldSpeed = (BASE_SPEED * currentDifficulty.getSpeedMultiplier()) + (score / 500.0);
        distanceTravalled += currentWorldSpeed;
        score = (int) ((distanceTravalled / 100) * 10 * currentDifficulty.getScoreMultiplier());

        // АНИМАЦИЯ РАЗМЕТКИ 
        roadOffset -= currentWorldSpeed;
        if (roadOffset < 0) roadOffset = 40;

        // ГЕНЕРАЦИЯ ПРЕПЯТСТВИЙ
        spawnTimer--;
        if (spawnTimer <= 0) {
            spawnObstacle(); 
            spawnTimer = (int) (Math.max(30, 100 - currentWorldSpeed * 5) * currentDifficulty.getSpawnRateMultiplier());
        }

        // ДВИЖЕНИЕ ПРЕПЯТСТВИЙ + ПРОВЕРКА КОЛЛИЗИЙ
        int playerY = height - 100;
        Rectangle playerRect = new Rectangle(width / 2 + m_playerX - CAR_WIDTH / 2, playerY, CAR_WIDTH, CAR_HEIGHT);
        
        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obs = iterator.next();
            obs.y += currentWorldSpeed + obs.speed;

            //уход препятствия за нижний экран
            if (obs.y > height) {
                iterator.remove();
                continue;
            }
            
            Rectangle obsRect = new Rectangle(width / 2 + obs.xOffset - CAR_WIDTH / 2, (int) obs.y, CAR_WIDTH, CAR_HEIGHT);
            
            // авария
            if (playerRect.intersects(obsRect)) {
                gameState = GameState.GAME_OVER;
            }
        }
    }

    private void spawnObstacle() {
        Obstacle obs = new Obstacle();
        int maxLimit = ROAD_WIDTH / 2 - CAR_WIDTH / 2;
        obs.xOffset = random.nextInt(maxLimit * 2) - maxLimit;
        obs.y = -CAR_HEIGHT;
        obs.speed = random.nextDouble() * 2;
        obstacles.add(obs);
    }

    // ГЕТТЕРЫ ДЛЯ ВИЗУАЛИЗАТОРА
    public GameState getGameState() { return gameState; }
    public DifficultyLevel getDifficulty() { return currentDifficulty; }
    public int getPlayerX() { return m_playerX; }
    public int getScore() { return score; }
    public double getCurrentWorldSpeed() { return currentWorldSpeed; }
    public double getRoadOffset() { return roadOffset; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public ScoreManager getScoreManager() { return scoreManager; }
}