package io.github.francoiscambell.clionarduinoplugin.resources;

import java.io.*;

/**
 * Created by francois on 15-08-03.
 */
public class ArduinoToolchainFiles {
    public static final InputStream ARDUINO_TOOLCHAIN_CMAKE = ArduinoToolchainFiles.class
            .getResourceAsStream("arduino-cmake/cmake/ArduinoToolchain.cmake");
    public static final InputStream ARDUINO_CMAKE = ArduinoToolchainFiles.class
            .getResourceAsStream("arduino-cmake/cmake/Platform/Arduino.cmake");
}
