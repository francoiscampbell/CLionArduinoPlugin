package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import com.jetbrains.cidr.cpp.cmake.*;
import com.jetbrains.cidr.cpp.cmake.workspace.*;
import io.github.francoiscambell.clionarduinoplugin.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * Created by francois on 15-08-03.
 */
public class ConvertToSketchProject extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final String projectName = project.getName();
        final CMakeListsEditor cMakeListsEditor = CMakeListsEditor.getInstance(project);

        deleteMainCpp(project);

        cMakeListsEditor.clear();
        cMakeListsEditor.minVersion("3.2");
        cMakeListsEditor.set("CMAKE_TOOLCHAIN_FILE", "${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake");
        cMakeListsEditor.set("PROJECT_NAME", projectName);
        cMakeListsEditor.project("${PROJECT_NAME}");
        cMakeListsEditor.blankLine();
        cMakeListsEditor.set("${CMAKE_PROJECT_NAME}_SKETCH", getMainSketchFile(project).getName());
        cMakeListsEditor.method("generate_arduino_firmware", "${CMAKE_PROJECT_NAME}");

        ArduinoToolchainFiles.copyToProject(project);
        deleteBuildOutputDir(project);

        CMakeSettings.getInstance(project).setAutoReloadEnabled(true);

    }

    private void deleteMainCpp(Project project) {
        final VirtualFile mainCpp = project.getBaseDir().findChild(Strings.MAIN_CPP_FILENAME);
        if (mainCpp != null) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainCpp.delete(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void deleteBuildOutputDir(Project project) {
        try {
            FileUtils.deleteDirectory(CMakeWorkspace.getInstance(project).getProjectGeneratedDir());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public VirtualFile getMainSketchFile(Project project) {
        VirtualFile projectBaseDir = project.getBaseDir();
        String projectName = project.getName();

        VirtualFile mainInoFile = projectBaseDir.findChild(projectName + Strings.DOT_INO_EXT);
        if (mainInoFile == null) {
            VirtualFile mainPdeFile = projectBaseDir.findChild(projectName + Strings.DOT_PDE_EXT);
            if (mainPdeFile == null) {
                return ArduinoSketchFileCreator
                        .createSketchFileWithName(project, projectBaseDir, projectName + Strings.DOT_INO_EXT);
            } else {
                return mainPdeFile;
            }
        } else {
            return mainInoFile;
        }
    }
}
