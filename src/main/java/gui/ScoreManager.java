package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ScoreManager {
    private final String scoreFilePath; 

    private int bestScore = 0;
    private String bestPlayer = "AAA";

    //основной конструктор для игры
    public ScoreManager() {
        //вызов второго конструктора, передавая стандартный путь
        this(System.getProperty("user.home") + File.separator + ".traffic_racer_scores.properties");
    }

    //конструктор для тестов
    public ScoreManager(String customFilePath) {
        this.scoreFilePath = customFilePath;
        loadScore();
    }

    /**
     * Загрузка реконда из файла.
     */
    public void loadScore() {
        File file = new File(scoreFilePath);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);
                
                bestScore = Integer.parseInt(props.getProperty("bestScore", "0"));
                bestPlayer = props.getProperty("bestPlayer", "AAA");
            } catch (Exception e) {
                System.err.println("Ошибка при загрузке реконда: " + e.getMessage());
            }
        }
    }

    /**
     * Сохранение новго рекорда в файл.
     */
    public void saveScore(String player, int score) {
        this.bestPlayer = (player != null && !player.trim().isEmpty()) ? player.trim() : "AAA";
        this.bestScore = score;

        try (FileOutputStream fos = new FileOutputStream(scoreFilePath)) {
            Properties props = new Properties();
            props.setProperty("bestScore", String.valueOf(bestScore));
            props.setProperty("bestPlayer", bestPlayer);
            props.store(fos, "Traffic Racer Hight Score Data");
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении реконда: " + e.getMessage());
        }
    }

    public int getBestScore() { return bestScore; }
    public String getBestPlayer() { return bestPlayer; }
}