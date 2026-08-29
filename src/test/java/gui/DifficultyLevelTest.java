package gui;

import org.junit.Test;
import static org.junit.Assert.*;

public class DifficultyLevelTest {

    @Test
    public void testEasyDifficultyMultipliers() {
        DifficultyLevel easy = DifficultyLevel.EASY;
        
        //Assert: Ожидаемое значение, Фактическое значение, Допустимая погрешность
        assertEquals(0.8, easy.getSpeedMultiplier(), 0.001);
        assertEquals(1.5, easy.getSpawnRateMultiplier(), 0.001);
        assertEquals(0.5, easy.getScoreMultiplier(), 0.001);
        assertEquals("Легко", easy.getDisplayName());
    }

    @Test
    public void testHardDifficultyMultipliers() {
        DifficultyLevel hard = DifficultyLevel.HARD;
        
        assertEquals(1.5, hard.getSpeedMultiplier(), 0.001);
        assertEquals(0.8, hard.getSpawnRateMultiplier(), 0.001);
        assertEquals(2.0, hard.getScoreMultiplier(), 0.001);
        assertEquals("Сложно", hard.getDisplayName());
    }
}