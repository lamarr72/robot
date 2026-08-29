package gui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

/**
 * Панель для визауализации игры
 */
public class GameVisualizer extends JPanel {

    // СОСТОЯНИЯ ИГРЫ
    //перечисление возможных состояний игры
    public enum GameState {PLAYING, GAME_OVER}

    //текущее состояние игры
    volatile GameState gameState = GameState.PLAYING;

    // КОНСТАНТЫ ДОРОГИ И МАШИН
    private static final int ROAD_WIDTH = 300;
    private static final int CAR_WIDTH = 40;
    private static final int CAR_HEIGHT = 70;
    private static final double BASE_SPEED = 3.0;

    // ПАРАМЕТРЫ ИГРОКА
    //смещение игрока по X
    volatile int m_playerX = 0;

    //флаги клавиш
    private volatile boolean movingLeft = false;
    private volatile boolean movingRight = false;

    //скорость маневрирования игрока (пиксель/тик)
    private final int playerSpeedX = 5;

    //ПАРАМЕТРЫ ИГРЫ И ОЧКИ
    //суммарное пройденное расстояние 
    private volatile double distanceTravalled = 0;
    //текущий счет 
    private volatile int score = 0;
    //текущая скорость мирв
    private volatile double currentWorldSpeed = BASE_SPEED;

    //ТЕКУЩАЯ СЛОЖНОСТЬ
    private volatile DifficultyLevel currentDifficulty = DifficultyLevel.NORMAL;

    public void setDifficulty(DifficultyLevel difficulty) {
        this.currentDifficulty = difficulty;
        restartGame(); //gерезапуск игры при смене сложности
    }

    //смещение для создания эффекта движения преривистой линии разметки на дороге
    private double roadOffset = 0;

    // ПРЕПЯТСТВИЯ
    //список активных препятствий на экране
    final List<Obstacle> obstacles = new ArrayList<>();
    //генератор чисел для спавна препятсвий в случаных местах
    private final Random random = new Random();
    //таймер для следующего препятсвия 
    private int spawnTimer = 0;

    //экземпляр менеждера рекордов
    private final ScoreManager scoreManager = new ScoreManager();
    //флаг единичного вызова окна рекорда
    private volatile boolean isGameOverProcessed = false;

    //инициализация общего таймера для запуска рендеринга и физики
    private final Timer m_timer = initTimer();

    //создание демон поток-таймера
    private static Timer initTimer() {
        return new Timer("event generator", true);
    }

    public GameVisualizer() {
        setupKeyBindings();
        setDoubleBuffered(true); //двойная буферизация для устранения траблов с отрисовкой

        // ЗАДАЧА РЕНДЕРИНГА
        //запуск перерисовки 
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(GameVisualizer.this::repaint);
            }
        }, 0, 30);

        // ЗАДАЧА ОБНОВЛЕНИЯ ФИЗИКИ
        //запуск расчет координат
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 10);
    }

    /**
     * Настройка управления
     */
    private void setupKeyBindings() {
        //получение карту ввода для перехватат нажатий клавиш (окно активно)
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        //получение карты действий для связки надатий с конкретным кодом
        ActionMap actionMap = getActionMap();

        // ОБРАБОТКА ВЛЕВО
        //привязка события "Нажата клавиша влево" к идентификатору left_down
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left_down");
        //определение действия
        actionMap.put("left_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { movingLeft = true; }
        });

        //привязка события "Отпускание влево" к идентификатору left_up
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left_up");
        //определение действия
        actionMap.put("left_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { movingLeft = false; }
        });

        // ОБРАБОТКА ДВИЖЕНИЯ ВПРАВО
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right_down");
        actionMap.put("right_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { movingRight = true; }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right_up");
        actionMap.put("right_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { movingRight = false; }
        });

        // ОБРАБОТКА ПЕРЕЗАПУСКА
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "space_down");
        actionMap.put("space_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.GAME_OVER) {
                    restartGame();
                }
            }
        });
    }

    /**
     * Сброс всех параметров 
     */
    private void restartGame() {
        m_playerX = 0; 
        distanceTravalled = 0;
        score = 0;
        currentWorldSpeed = BASE_SPEED * currentDifficulty.getSpeedMultiplier();
        obstacles.clear();
        isGameOverProcessed = false;
        scoreManager.loadScore();
        gameState = GameState.PLAYING;
    }

    /**
     * Метод обновления мат и физ модели игры (каждые 10мс)
     */
    protected void onModelUpdateEvent() {
        if (gameState == GameState.GAME_OVER) {
            if (!isGameOverProcessed) {
                isGameOverProcessed = true;
                processGameOver();;
            }
            return;
        }

        // ДВИЖЕНИЕ ИГРОКА
        if (movingLeft && !movingRight) m_playerX -= playerSpeedX;
        if (movingRight && !movingLeft) m_playerX += playerSpeedX;

        // ОГРАНИЧЕНИЕ ВЫХОДА ЗА ПРЕДЕЛЫ ДОРОГИ
        //макс допустимое отклонение от центра
        int maxLimit = ROAD_WIDTH / 2 - CAR_WIDTH / 2;
        //если игрок сильно ушел влево/врпаво - возвращение на край
        if (m_playerX < -maxLimit) m_playerX = -maxLimit;
        if (m_playerX > maxLimit) m_playerX = maxLimit;

        // НАЧИСЛЕНИЕ ОЧКОВ И УСКОРЕНИЕ
        //постепенное увеличение скорости (в зависимости от счета)
        currentWorldSpeed = (BASE_SPEED * currentDifficulty.getSpeedMultiplier()) + (score / 500.0);
        distanceTravalled += currentWorldSpeed;
        
        //очки умножаются на коэффициент сложности
        score = (int) ((distanceTravalled / 100) * 10 * currentDifficulty.getScoreMultiplier());

        // АНИМАЦИЯ РАЗМЕТКИ 
        roadOffset -= currentWorldSpeed;
        if (roadOffset < 0) roadOffset = 40;

        // ГЕНЕРАЦИЯ ПРЕПЯТСТВИЙ
        spawnTimer--;
        if (spawnTimer <= 0) {
            spawnObstacle(); 
            //базовый таймер спавна умножаем на коэффициент сложности
            spawnTimer = (int) (Math.max(30, 100 - currentWorldSpeed * 5) * currentDifficulty.getSpawnRateMultiplier());
        }
        // ДВИЖЕНИЕ ПРЕПЯТСТВИЙ + ПРОВЕРКА КОЛЛИЗИЙ
        int playerY = getHeight() - 100;

        //создание хитбокса 
        Rectangle playerRect = new Rectangle(getWidth() / 2 + m_playerX - CAR_WIDTH / 2, 
            playerY, CAR_WIDTH, CAR_HEIGHT);
        
        //итератор для удаления элементов из списка во время цикла
        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obs = iterator.next();
            obs.y += currentWorldSpeed + obs.speed;

            //уход препятствия за нижний экран
            if (obs.y > getHeight()) {
                iterator.remove();
                continue;
            }
            Rectangle obsRect = new Rectangle(getWidth() / 2 + obs.xOffset - CAR_WIDTH / 2,
                (int) obs.y, CAR_WIDTH, CAR_HEIGHT);
            
            //авария
            if (playerRect.intersects(obsRect)) {
                gameState = GameState.GAME_OVER;
            }
        }
    }

    private void processGameOver() {
        if (score > scoreManager.getBestScore()) {
            //передача вызова окна в поток Swing (чтобы не блокировать таймер физики)
            SwingUtilities.invokeLater(() -> {
                JTextField nameField = new JTextField(15);

                //фильтр автомат перевода букв в верхний регистр
                ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                        if (string != null) {
                           super.insertString(fb, offset, string.toUpperCase(), attr);
                        }
                    }

                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                        if (text != null) {
                            super.replace(fb, offset, length, text.toUpperCase(), attrs);
                        }
                    }
                });
                Object[] message = {
                    "Новый рекорд: " + score + "!\nВведите имя:", nameField
                }; 

                int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Новый рекорд",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );

                if (option == JOptionPane.OK_OPTION) {
                    scoreManager.saveScore(nameField.getText(), score);
                }
            });
        }
    }

    /** 
     * Создание нового препятствия в случайной точке дороги
     */
    private void spawnObstacle() {
        Obstacle obs = new Obstacle();

        //вычисление макс возможное отклонение от центра
        int maxLimit = ROAD_WIDTH / 2 - CAR_WIDTH / 2;
        //случайное смещение по X (от левой до правой границы)
        obs.xOffset = random.nextInt(maxLimit * 2) - maxLimit;
        
        obs.y = -CAR_HEIGHT;
        //случайная скорость препятствия
        obs.speed = random.nextDouble() * 2;

        //добавление препятствия в список на отрисовку
        obstacles.add(obs);
    }

    // ОТРИСОВКА
    @Override
    protected void paintComponent(Graphics g) {
        //вызов род метода для очистки фона
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //получение текущих разеров панели
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;

        // ОТРИСОВКА ОБОЧИНЫ
        g2d.setColor(new Color(34, 139, 34)); 
        g2d.fillRect(0, 0, width, height);

        // ОТРИСОВКА ДОРОГИ
        g2d.setColor(Color.DARK_GRAY);
        int roadLeftX = centerX - ROAD_WIDTH / 2;
        g2d.fillRect(roadLeftX, 0, ROAD_WIDTH, height);

        // ОТРИСОВКА РАЗМЕТКИ
        g2d.setColor(Color.WHITE);
        //настройка кисти
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{20, 20}, (float) roadOffset));
        g2d.drawLine(centerX, 0, centerX, height); //рисуется донизу

        //возращение кисти к сплошной линии дл яотрисовки сплошных грыниц дороги
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(roadLeftX, 0, roadLeftX, height);
        g2d.drawLine(roadLeftX + ROAD_WIDTH, 0, roadLeftX + ROAD_WIDTH, height);

        // ОТРИСОВКА ПРЕПЯТСТВИЙ
        g2d.setColor(Color.RED);
        for (Obstacle obs : obstacles) {
            g2d.fillRect(centerX + obs.xOffset - CAR_WIDTH / 2, (int) obs.y, CAR_WIDTH, CAR_HEIGHT);
        }

        // ОТРИСОВКА ИГРОКА
        int playerY = height - 100;
        g2d.setColor(Color.CYAN);
        g2d.fillRect(centerX + m_playerX - CAR_WIDTH / 2, playerY, CAR_WIDTH, CAR_HEIGHT);

        // ОТРИСОВКА ИНТЕРФЕЙСА
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Speed: " + String.format("%.1f", currentWorldSpeed), 20, 60);
        g2d.drawString("Diff: " + currentDifficulty.getDisplayName(), 20, 120);
        //отрисовка текущего рекорда
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Best: " + scoreManager.getBestScore() + " (" + scoreManager.getBestPlayer() + ")", 20, 90);

        // GAME OVER
        if (gameState == GameState.GAME_OVER) {
            //черный цвет с прозрачностью 150 из 255
            g2d.setColor(new Color(0, 0, 0, 150)); 
            //заливка
            g2d.fillRect(0, 0, width, height);

            //настройка текста "GAME OVER"
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String goText = "GAME OVER";
            //ширина строки в пикселях для отцентровки
            int textWidth = g2d.getFontMetrics().stringWidth(goText);
            g2d.drawString(goText, centerX - textWidth / 2, height / 2);

            //настройка текста подсказки "Press SPACE"
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String restartText = "Press SPACE to Restart";
            int restWidth = g2d.getFontMetrics().stringWidth(restartText);
            g2d.drawString(restartText, centerX - restWidth / 2, height / 2 + 40);
        }
    }
    /**
    * Внутренний класс, представляющий препятствие.
    * Хранит: позицию и индивидуальную скорость.
    */
    public class Obstacle {
            int xOffset;
            double y;
            double speed;
        }
}
