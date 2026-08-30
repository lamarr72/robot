package gui;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GamePhysicsTest {

    private GameModel model;

    @Before
    public void setUp() {
        //создание нового изолированного экземпляра модели игры перед каждым тестом
        model = new GameModel();
    }

    /**
     * Тест: игрок не должен выезжать за левую обочину
     */
    @Test
    public void testPlayerCannotMoveBeyondLeftBorder() {
        //прямая установка Модели флага движения влево
        model.setMovingLeft(true);

        //симуляция 100 тиков игры (прошла 1 секунда)
        for (int i = 0; i < 100; i++) {
            //передаем виртуальные размеры экрана прямо в метод физики
            model.updatePhysics(800, 600);
        }

        //вычисление ожидаемой границы (ROAD_WIDTH / 2 - CAR_WIDTH / 2) -> 300/2 - 40/2 = 150 - 20 = 130
        int expectedLimit = -130;
        
        //проверка, что координата X не ушла дальше -130
        assertEquals(expectedLimit, model.getPlayerX());
    }

    /**
     * Тест: Проверка столкновения с машиной (Game Over)
     */
    @Test
    public void testCollisionTriggersGameOver() {
        //игра началась в статусе PLAYING
        assertEquals(GameModel.GameState.PLAYING, model.getGameState());

        //создание препятствия
        GameModel.Obstacle obs = new GameModel.Obstacle();
        obs.xOffset = 0;
        obs.y = 450;
        obs.speed = 10;
        
        //добавление препятствия в список модели
        model.getObstacles().add(obs);

        //симуляция нескольких тиков физики, чтобы машина доехала до нас
        for (int i = 0; i < 10; i++) {
            model.updatePhysics(800, 600);
        }

        //проверка, что статус игры изменился на GAME_OVER из-за пересечения хитбоксов
        assertEquals(GameModel.GameState.GAME_OVER, model.getGameState());
    }

    /**
     * Тест: Машины, уехавшие за экран, удаляются из памяти
     */
    @Test
    public void testObstacleGarbageCollection() {
        //прогон одного "холостого" тика, чтобы сработал первый автоматический спавн
        //и таймер (spawnTimer) перезарядился
        model.updatePhysics(800, 600);
        //очистка списка от этой сгенерированной машины
        model.getObstacles().clear(); 

        //создание тестовой машины и помещение её далеко за пределы экрана
        GameModel.Obstacle obs = new GameModel.Obstacle();
        obs.y = 1000; //высота экрана 600, машина уже проехала мимо
        model.getObstacles().add(obs);
        
        assertEquals(1, model.getObstacles().size());

        //реальный тик физики для проверки удаления
        model.updatePhysics(800, 600);

        //проверка, что итератор удалил машину из списка, а новая не появилась
        assertEquals(0, model.getObstacles().size());
    }
}