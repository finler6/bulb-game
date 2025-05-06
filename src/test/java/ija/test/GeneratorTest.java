package ija.test;

import lightbulb.model.Board;
import lightbulb.model.LevelGenerator;
import lightbulb.controller.GameController;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяем, что после генерации (без шаффла) все лампы питаются.
 */
class GeneratorTest {

    private final LevelGenerator gen = new LevelGenerator();

    @RepeatedTest(1000)
    void everyLampGetsPower() {
        // ⚠  shuffle     ↓↓↓
        Board b = gen.generate(10, 10, 4, 25, /*shuffle*/ 0);
        GameController gc = new GameController(b);
        gc.updateConnections();
        assertTrue(gc.isGameWon(), "Найдена лампа без питания");
    }

}
