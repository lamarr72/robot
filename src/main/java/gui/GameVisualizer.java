package gui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

/**
 * Панель для визуализации игры.
 */
public class GameVisualizer extends JPanel {

    //ссылка на математическую модель игры
    private final GameModel model;

    //флаг единичного вызова окна рекорда
    private volatile boolean isGameOverProcessed = false;

    //инициализация общего таймера
    private final Timer m_timer = new Timer("event generator", true);

    public GameVisualizer() {
        //создание модели игры
        this.model = new GameModel();
        
        setupKeyBindings();
        setDoubleBuffered(true);

        // ЗАДАЧА РЕНДЕРИНГА (30 мс)
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(GameVisualizer.this::repaint);
            }
        }, 0, 30);

        // ЗАДАЧА ОБНОВЛЕНИЯ ФИЗИКИ (10 мс)
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Передаем модели текущие размеры окна для расчетов
                model.updatePhysics(getWidth(), getHeight());
                checkGameOverUI();
            }
        }, 0, 10);
    }

    /**
     * Внешний метод для меню выбора сложности
     */
    public void setDifficulty(DifficultyLevel difficulty) {
        model.setDifficulty(difficulty);
        isGameOverProcessed = false;
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left_down");
        actionMap.put("left_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { model.setMovingLeft(true); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left_up");
        actionMap.put("left_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { model.setMovingLeft(false); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right_down");
        actionMap.put("right_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { model.setMovingRight(true); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right_up");
        actionMap.put("right_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { model.setMovingRight(false); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "space_down");
        actionMap.put("space_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.getGameState() == GameModel.GameState.GAME_OVER) {
                    model.restartGame();
                    isGameOverProcessed = false;
                }
            }
        });
    }

    /**
     * Проверка, нужно ли показать окно рекорда после аварии
     */
    private void checkGameOverUI() {
        if (model.getGameState() == GameModel.GameState.GAME_OVER && !isGameOverProcessed) {
            isGameOverProcessed = true; //блокировка повторного вызова
            
            int currentScore = model.getScore();
            ScoreManager sm = model.getScoreManager();
            
            if (currentScore > sm.getBestScore()) {
                SwingUtilities.invokeLater(() -> showNewRecordDialog(currentScore, sm));
            }
        }
    }

    private void showNewRecordDialog(int newScore, ScoreManager sm) {
        JTextField nameField = new JTextField(15);
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null) super.insertString(fb, offset, string.toUpperCase(), attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null) super.replace(fb, offset, length, text.toUpperCase(), attrs);
            }
        });

        Object[] message = { "Новый рекорд: " + newScore + "!\nВведите имя:", nameField }; 
        int option = JOptionPane.showConfirmDialog(this, message, "Новый рекорд", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            sm.saveScore(nameField.getText(), newScore);
        }
    }

    // ОТРИСОВКА (чтение данных из модели)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;

        // ОТРИСОВКА ОБОЧИНЫ
        g2d.setColor(new Color(34, 139, 34)); 
        g2d.fillRect(0, 0, width, height);

        // ОТРИСОВКА ДОРОГИ
        g2d.setColor(Color.DARK_GRAY);
        int roadLeftX = centerX - GameModel.ROAD_WIDTH / 2;
        g2d.fillRect(roadLeftX, 0, GameModel.ROAD_WIDTH, height);

        // ОТРИСОВКА РАЗМЕТКИ
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{20, 20}, (float) model.getRoadOffset()));
        g2d.drawLine(centerX, 0, centerX, height);

        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(roadLeftX, 0, roadLeftX, height);
        g2d.drawLine(roadLeftX + GameModel.ROAD_WIDTH, 0, roadLeftX + GameModel.ROAD_WIDTH, height);

        // ОТРИСОВКА ПРЕПЯТСТВИЙ
        g2d.setColor(Color.RED);
        //блокировка списка на время отрисовки, чтобы фоновый поток физики не удалял/добавлял машины во время прохода по циклу
        synchronized (model.getObstacles()) {
            for (GameModel.Obstacle obs : model.getObstacles()) {
                g2d.fillRect(centerX + obs.xOffset - GameModel.CAR_WIDTH / 2, (int) obs.y, GameModel.CAR_WIDTH, GameModel.CAR_HEIGHT);
            }
        }

        // ОТРИСОВКА ИГРОКА
        int playerY = height - 100;
        g2d.setColor(Color.CYAN);
        g2d.fillRect(centerX + model.getPlayerX() - GameModel.CAR_WIDTH / 2, playerY, GameModel.CAR_WIDTH, GameModel.CAR_HEIGHT);

        // ОТРИСОВКА ИНТЕРФЕЙСА
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + model.getScore(), 20, 30);
        g2d.drawString("Speed: " + String.format("%.1f", model.getCurrentWorldSpeed()), 20, 60);
        g2d.drawString("Diff: " + model.getDifficulty().getDisplayName(), 20, 120);
        
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Best: " + model.getScoreManager().getBestScore() + " (" + model.getScoreManager().getBestPlayer() + ")", 20, 90);

        // GAME OVER
        if (model.getGameState() == GameModel.GameState.GAME_OVER) {
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.fillRect(0, 0, width, height);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String goText = "GAME OVER";
            int textWidth = g2d.getFontMetrics().stringWidth(goText);
            g2d.drawString(goText, centerX - textWidth / 2, height / 2);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String restartText = "Press SPACE to Restart";
            int restWidth = g2d.getFontMetrics().stringWidth(restartText);
            g2d.drawString(restartText, centerX - restWidth / 2, height / 2 + 40);
        }
    }
}