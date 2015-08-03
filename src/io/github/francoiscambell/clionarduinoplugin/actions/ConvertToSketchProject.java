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
        final PsiDirectory projectRootDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

        final String projectName = project.getName();
        Application app = ApplicationManager.getApplication();

        PsiFile cmakeListsPsiFile = projectRootDirectory.findFile("CMakeLists.txt");
        final Document cmakeListsDocument = psiDocumentManager.getDocument(cmakeListsPsiFile);
        final int endOfFirstLine = cmakeListsDocument.getCharsSequence().toString().indexOf('\n');
        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        cmakeListsDocument
                                .insertString(endOfFirstLine, "\nset(CMAKE_TOOLCHAIN_FILE ${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake)");
                        cmakeListsDocument.insertString(0, "set(PROJECT_NAME " + projectName + ")\n");

                    }
                }, null, null, cmakeListsDocument);
            }
        });

        PsiFile mainInoFile = projectRootDirectory.findFile(projectName + ".ino");
        PsiFile mainPdeFile = projectRootDirectory.findFile(projectName + ".pde");
        if (mainInoFile == null && mainPdeFile == null) {
            app.runWriteAction(new Runnable() {
                @Override
                public void run() {
                    PsiFile mainProjectFile = projectRootDirectory.createFile(projectName + ".ino");
                    final Document mainProjectDocument = PsiDocumentManager.getInstance(project)
                                                                           .getDocument(mainProjectFile);
                    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                        @Override
                        public void run() {
                            mainProjectDocument
                                    .setText("#include <Arduino.h>\n\nvoid setup() {\n\n}\n\nvoid loop() {\n\n}");
                        }
                    }, null, null, mainProjectDocument);
                }
            });
        }

        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (projectRootDirectory.findSubdirectory("build") == null) {
                    projectRootDirectory.createSubdirectory("build");
                }
            }
        });

        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        cmakeListsDocument.insertString(cmakeListsDocument
                                .getTextLength(), "\nset(${CMAKE_PROJECT_NAME}_SKETCH ${CMAKE_PROJECT_NAME}.ino)\ngenerate_arduino_firmware(${CMAKE_PROJECT_NAME})\n");
                    }
                }, null, null, cmakeListsDocument);
            }
        });

        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
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
            }
        });

    }
}
