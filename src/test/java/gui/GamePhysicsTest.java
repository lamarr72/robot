package gui;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GamePhysicsTest {

    private GameVisualizer game;

    @Before
    public void setUp() {
        //создание нового экземпляра игры перед каждым тестом
        game = new GameVisualizer();
        //установка размера окна вручную
        game.setSize(800, 600); 
    }

    /**
     * Тест: игрок не должен выезжать за левую обочину
     */
    @Test
    public void testPlayerCannotMoveBeyondLeftBorder() {
        //установка игроку флаг движения влево
        game.getActionMap().get("left_down").actionPerformed(null);

        //симуляция 100 тиков игры (прошла 1 секунда)
        for (int i = 0; i < 100; i++) {
            game.onModelUpdateEvent();
        }

        //вычисление ожидаемой границы (ROAD_WIDTH / 2 - CAR_WIDTH / 2) -> 300/2 - 40/2 = 150 - 20 = 130
        int expectedLimit = -130;
        
        //проверка, что координата X не ушла дальше -130
        assertEquals(expectedLimit, game.m_playerX);
    }

    /**
     * Тест: Проверка столкновения с машиной (Game Over)
     */
    @Test
    public void testCollisionTriggersGameOver() {
        //игра началась в статусе PLAYING
        assertEquals(GameVisualizer.GameState.PLAYING, game.gameState);

        //создание препятствия
        GameVisualizer.Obstacle obs = game.new Obstacle();
        obs.xOffset = 0;
        obs.y = 450;
        obs.speed = 10;
        
        //добавление препятствия в список игры
        game.obstacles.add(obs);

        //симуляция нескольких тиков физики, чтобы машина доехала до нас
        for (int i = 0; i < 10; i++) {
            game.onModelUpdateEvent();
        }

        //проверка, что статус игры изменился на GAME_OVER из-за пересечения хитбоксов
        assertEquals(GameVisualizer.GameState.GAME_OVER, game.gameState);
    }

    /**
     * Тест: Машины, уехавшие за экран, удаляются из памяти
     */
    @Test
    public void testObstacleGarbageCollection() {
        //прогон одиного "холостой" тик, чтобы сработал первый автоматический спавн
        // и таймер (spawnTimer) перезарядился
        game.onModelUpdateEvent();
        //очистка списока от этой сгенерированной машины
        game.obstacles.clear(); 

        //создание тестовой машины и помещение её далеко за пределы экрана
        GameVisualizer.Obstacle obs = game.new Obstacle();
        obs.y = 1000; //высота экрана 600, машина уже проехала мимо
        game.obstacles.add(obs);
        
        assertEquals(1, game.obstacles.size());

        //реальный тик физики для проверки удаления
        game.onModelUpdateEvent();

        //проверка, что итератор удалил машину из списка, а новая не появилась
        assertEquals(0, game.obstacles.size());
    }
}