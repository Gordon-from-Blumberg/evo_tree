package com.gordonfromblumberg.games.core.common.chunk;

import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.utils.IntVector2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkManagerTest {
    private final ChunkManager<IntVector2> chunkManager = new ChunkManager<>(100, 50, 16);

    static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(0, 0, 32, 32, arrayOf(0, 0, 20, 0)),
                Arguments.of(0, 0, 10, 0, arrayOf(0, 0)),
                Arguments.of(40, 49, 1, 10, arrayOf(0, 0, 50, 21)),
                Arguments.of(0, 49, 20, 22, arrayOf(0, 0, 20, 0, 20, 21)),
                Arguments.of(0, 49, 20, 19, arrayOf(0, 0, 20, 0)),
                Arguments.of(32, 49, 33, 49, arrayOf()),
                Arguments.of(33, 49, 32, 49, arrayOf(0, 0, 20, 0, 20, 21, 50, 21)),
                Arguments.of(21, 49, 21, 49, arrayOf()),
                Arguments.of(20, 49, 20, 49, arrayOf(20, 0, 20, 21)),
                Arguments.of(20, 16, 20, 16, arrayOf(20, 0))
        );
    }

    @BeforeEach
    void setup() {
        chunkManager.addObject(new IntVector2(0, 0), 0, 0);
        chunkManager.addObject(new IntVector2(20, 0), 20, 0);
        chunkManager.addObject(new IntVector2(20, 21), 20, 21);
        chunkManager.addObject(new IntVector2(50, 21), 50, 21);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void test(int x1, int y1, int x2, int y2, Array<IntVector2> expected) {
        System.out.println("Test line from " + x1 + ", " + y1 + " to " + x2 + ", " + y2);
        Array<IntVector2> objects = chunkManager.findObjectsUnderLine(x1, y1, x2, y2);
        System.out.println("Found objects " + objects);
        assertEquals(expected.size, objects.size);
        for (IntVector2 expectedVector : expected) {
            assertTrue(objects.contains(expectedVector, false));
        }
    }

    private static Array<IntVector2> arrayOf(int... coords) {
        Array<IntVector2> array = new Array<>(coords.length / 2);
        for (int i = 0; i < coords.length; i += 2) {
            array.add(new IntVector2(coords[i], coords[i + 1]));
        }
        return array;
    }
}
