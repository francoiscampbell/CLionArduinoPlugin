package io.github.francoiscambell.clionarduinoplugin.resources;

import com.intellij.openapi.application.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import org.apache.sanselan.util.*;

import java.io.*;

/**
 * Created by francois on 15-08-03.
 */
public class ArduinoToolchainFiles {
    public static InputStream getArduinoToolchainCmake() {
        return ArduinoToolchainFiles.class.getResourceAsStream("arduino-cmake/cmake/ArduinoToolchain.cmake");
    }

    public static InputStream getArduinoCmake() {
        return ArduinoToolchainFiles.class.getResourceAsStream("arduino-cmake/cmake/Platform/Arduino.cmake");
    }

    public static void copyToProject(final Project project) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualFile cmakeDirectory = project.getBaseDir().createChildDirectory(this, "cmake");
                    VirtualFile platformDirectory = cmakeDirectory.createChildDirectory(this, "Platform");

                    VirtualFile arduinoToolchain = cmakeDirectory.createChildData(this, "ArduinoToolchain.cmake");
                    VirtualFile arduino = platformDirectory.createChildData(this, "Arduino.cmake");

                    OutputStream arduinoToolchainOutputStream = arduinoToolchain.getOutputStream(this);
                    OutputStream arduinoOutputStream = arduino.getOutputStream(this);

                    IOUtils.copyStreamToStream(getArduinoToolchainCmake(), arduinoToolchainOutputStream);
                    IOUtils.copyStreamToStream(getArduinoCmake(), arduinoOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
