package com.gordonfromblumberg.games.core.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class FileUtilsTest {
    @Test
    public void getNameWithoutExtension() {
        Assertions.assertEquals("log", FileUtils.getNameWithoutExtension(new File("log.txt")));
        Assertions.assertEquals("app", FileUtils.getNameWithoutExtension(new File("app.log")));
        Assertions.assertEquals("without_ext", FileUtils.getNameWithoutExtension(new File("without_ext")));
        Assertions.assertEquals("some.dots", FileUtils.getNameWithoutExtension(new File("some.dots.txt")));
        Assertions.assertEquals("1", FileUtils.getNameWithoutExtension(new File("1.longlonglongextension")));
    }
}
