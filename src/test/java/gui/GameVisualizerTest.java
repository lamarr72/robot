package gui;

import org.junit.Assert;
import org.junit.Test;
import java.awt.Point;

public class GameVisualizerTest {

    @Test
    public void testRobotMovesTowardsTarget() {
        GameVisualizer visualizer = new GameVisualizer();
        
        // зpадача размера панели, иначе getWidth() вернет 0 
        visualizer.setSize(400, 400);
        
        // робота в точку (10, 10), вправо (0 радиан)
        visualizer.setRobotPositionForTest(10, 10, 0);
        
        // цель далеко вправо
        visualizer.setTargetPosition(new Point(100, 10));
        
        // иситация одиного "тика" физической модели
        visualizer.onModelUpdateEvent();
        
        // робот должен был сдвинуться вправо (Х должен стать больше 10)
        Assert.assertTrue("Робот должен двигаться вправо", visualizer.getRobotPositionX() > 10);
        
        // по оси Y он двигаться не должен, так как цель строго по X
        Assert.assertEquals(10.0, visualizer.getRobotPositionY(), 0.001);
    }

    @Test
    public void testRobotBoundaryClamping() {
        GameVisualizer visualizer = new GameVisualizer();
        
        // имитация размер окна 400x400
        visualizer.setSize(400, 400);
        
        // робот вплотную к левой границе (X = 0) и направление налево
        visualizer.setRobotPositionForTest(0, 100, Math.PI);
        
        // цель за экраном слева
        visualizer.setTargetPosition(new Point(-50, 100));
        
        //  шаг. По обычной математике робот ушел бы в минус,
        // но moveRobot должна приравнять координату к 0.
        visualizer.onModelUpdateEvent();
        
        // проверка, что робот не провалился в отрицательные координаты
        Assert.assertEquals("Робот не должен выходить за левую границу (< 0)", 
                            0.0, visualizer.getRobotPositionX(), 0.0001);
    }
}