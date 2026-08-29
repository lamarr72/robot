package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Менеджер для сохранения и загрузки локальных рекордов игры. 
 */
public class ScoreManager {
    //сохранение файла в домашнюю редикторию пользователя
    private static final String SCORE_FILE = System.getProperty("user.home") + File.separator + "traffic_racer_scores.properties";
    
    private int bestScore = 0;
    private String bestPlayer = "AAA";

    public ScoreManager() {
        //загрузка рекорда сразу при создании объекта
        loadScore();
    }

    /**
     * Загрузка реконда из файла.
     */
    public void loadScore() {
        File file = new File(SCORE_FILE);
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

        try (FileOutputStream fos = new FileOutputStream(SCORE_FILE)) {
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