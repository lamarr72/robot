package gui;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ScoreManagerTest {

    private Path tempScoreFile;
    private ScoreManager scoreManager;

    @Before
    public void setUp() throws IOException {
        //создание временного файла во временной директории ОС
        tempScoreFile = Files.createTempFile("test_scores", ".properties");
        scoreManager = new ScoreManager(tempScoreFile.toString());
    }

    @After
    public void tearDown() throws IOException {
        //удаление временного файла
        Files.deleteIfExists(tempScoreFile);
    }

    @Test
    public void testInitialValuesWhenFileIsEmpty() {
        //у свежесозданного менеджера с пустым файлом должны быть значения по умолчанию
        assertEquals(0, scoreManager.getBestScore());
        assertEquals("AAA", scoreManager.getBestPlayer());
    }

    @Test
    public void testSaveAndLoadScore() {
        //Arrange (Подготовка)
        String testPlayer = "RACER";
        int testScore = 1550;

        //Act (Действие)
        scoreManager.saveScore(testPlayer, testScore);
        
        //создание нового экземпляра ScoreManager, который читает тот же файл, 
        // чтобы проверить именно чтение с диска, а не просто переменные в памяти
        ScoreManager newManager = new ScoreManager(tempScoreFile.toString());

        //Assert (Проверка)
        assertEquals(testScore, newManager.getBestScore());
        assertEquals(testPlayer, newManager.getBestPlayer());
    }

    @Test
    public void testSaveScoreWithEmptyName() {
        //Act: попытка сохранить пустое имя или пробелы
        scoreManager.saveScore("   ", 500);

        //Assert: менеджер должен подставить дефолтное имя "AAA"
        assertEquals("AAA", scoreManager.getBestPlayer());
        assertEquals(500, scoreManager.getBestScore());
    }
}