package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;
import org.apache.sanselan.util.*;

import java.io.*;

/**
 * Created by francois on 15-08-03.
 */
public class ConvertToSketchProject extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        PsiDirectory projectRootDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

        String projectName = project.getName();
        Application app = ApplicationManager.getApplication();

        PsiFile cmakeListsPsiFile = projectRootDirectory.findFile("CMakeLists.txt");
        Document cmakeListsDocument = psiDocumentManager.getDocument(cmakeListsPsiFile);
        int endOfFirstLine = cmakeListsDocument.getCharsSequence().toString().indexOf('\n');
        app.runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(project, () -> {
                cmakeListsDocument
                        .insertString(endOfFirstLine, "\nset(CMAKE_TOOLCHAIN_FILE ${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake)");
                cmakeListsDocument.insertString(0, "set(PROJECT_NAME " + projectName + ")\n");

            }, null, null, cmakeListsDocument);
        });

        PsiFile mainInoFile = projectRootDirectory.findFile(projectName + ".ino");
        PsiFile mainPdeFile = projectRootDirectory.findFile(projectName + ".pde");
        if (mainInoFile == null && mainPdeFile == null) {
            app.runWriteAction(() -> {
                PsiFile mainProjectFile = projectRootDirectory.createFile(projectName + ".ino");
                Document mainProjectDocument = PsiDocumentManager.getInstance(project).getDocument(mainProjectFile);
                CommandProcessor.getInstance().executeCommand(project, () -> {
                    mainProjectDocument.setText("#include <Arduino.h>\n\nvoid setup() {\n\n}\n\nvoid loop() {\n\n}");
                }, null, null, mainProjectDocument);
            });
        }

        app.runWriteAction(() -> {
            if (projectRootDirectory.findSubdirectory("build") == null) {
                projectRootDirectory.createSubdirectory("build");
            }
        });

        app.runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(project, () -> {
                cmakeListsDocument.insertString(cmakeListsDocument
                        .getTextLength(), "\nset(${PROJECT_NAME}_SKETCH ${PROJECT_NAME}.ino)\ngenerate_arduino_firmware(${PROJECT_NAME})\n");
            }, null, null, cmakeListsDocument);
        });

        app.runWriteAction(() -> {
            PsiDirectory cmakeDirectory = projectRootDirectory.createSubdirectory("cmake");
            PsiDirectory platformDirectory = cmakeDirectory.createSubdirectory("Platform");

            PsiFile arduinoToolchain = cmakeDirectory.createFile("ArduinoToolchain.cmake");
            PsiFile arduino = platformDirectory.createFile("Arduino.cmake");

            VirtualFile arduinoToolchainVirtualFile = arduinoToolchain.getVirtualFile();
            VirtualFile arduinoVirtualFile = arduino.getVirtualFile();

            try {
                OutputStream arduinoToolchainOutputStream = arduinoToolchainVirtualFile.getOutputStream(null);
                OutputStream arduinoOutputStream = arduinoVirtualFile.getOutputStream(null);

                IOUtils.copyStreamToStream(ArduinoToolchainFiles.ARDUINO_TOOLCHAIN_CMAKE, arduinoToolchainOutputStream);
                IOUtils.copyStreamToStream(ArduinoToolchainFiles.ARDUINO_CMAKE, arduinoOutputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

    }
}
