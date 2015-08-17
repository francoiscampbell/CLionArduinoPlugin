package io.github.francoiscambell.clionarduinoplugin.wizards;

import com.intellij.ide.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.io.*;
import com.intellij.openapi.vfs.*;
import com.jetbrains.cidr.cpp.*;
import com.jetbrains.cidr.cpp.cmake.projectWizard.*;
import com.jetbrains.cidr.cpp.cmake.workspace.*;
import io.github.francoiscambell.clionarduinoplugin.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;

import java.io.*;

/**
 * Created by francois on 15-08-14.
 */
public class NewArduinoProjectWizard extends CMakeProjectWizard {
    private NewCMakeProjectStepAdapter adapter = new NewCMakeProjectStepAdapter();

    public NewArduinoProjectWizard() {
        super("New Arduino Sketch Project", "NewArduinoSketchProject");
        initWithStep(adapter);
    }

    @Override
    protected boolean tryFinish() {
        String projectRootPath = this.adapter.getLocation();
        File projectRootDir = new File(projectRootPath);
        if (projectRootDir.exists()) {
            String[] fileList = projectRootDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return !".DS_Store".equalsIgnoreCase(name) && !"Thumbs.db".equalsIgnoreCase(name);
                }
            });
            if (fileList != null && fileList.length > 0) {
                int dialogAnswer = Messages.showYesNoDialog(String
                        .format("Directory \'%s\' already exists and not empty.\nWould you like to continue?", projectRootPath), "Project Directory Already Exists", Messages
                        .getQuestionIcon());
                if (dialogAnswer != 0) {
                    return false;
                }
            }
        } else {
            try {
                VfsUtil.createDirectories(projectRootPath);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            } catch (IOException e) {
                return false;
            }
        }

        String projectRootDirParentPath = projectRootDir.getParent();
        if (projectRootDirParentPath != null) {
            RecentProjectsManager.getInstance().setLastProjectCreationLocation(projectRootDirParentPath);
        }

        try {
            createProject(this.adapter.getName(), projectRootPath);
            return true;
        } catch (IOException e) {
            CPPLog.LOG.warn(e);
            return false;
        }
    }

    public static String createProject(String projectName, String projectRootPath) throws IOException {
        File projectRoot = new File(projectRootPath);
        File cMakeLists = new File(projectRoot, "CMakeLists.txt");
        if (!cMakeLists.exists() && !cMakeLists.createNewFile()) {
            throw new IOException("Cannot create file " + cMakeLists);
        } else {
            projectName = FileUtil.sanitizeFileName(projectName);
            File mainSketchFile = new File(projectRoot, projectName + ".ino");
            if (!mainSketchFile.exists() && !mainSketchFile.createNewFile()) {
                throw new IOException("Cannot create file " + mainSketchFile);
            } else {
                FileUtil.writeToFile(mainSketchFile, Strings.DEFAULT_ARDUINO_SKETCH_CONTENTS);

                VirtualFile cMakeListsVirtualFile = VfsUtil.findFileByIoFile(cMakeLists, true);
                CMakeListsEditor cMakeListsEditor = CMakeListsEditor.getInstance(cMakeListsVirtualFile);
                cMakeListsEditor.clear();
                cMakeListsEditor.minVersion("2.8.4");
                cMakeListsEditor.set("CMAKE_TOOLCHAIN_FILE", "${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake");
                cMakeListsEditor.set("PROJECT_NAME", projectName);
                cMakeListsEditor.project("${PROJECT_NAME}");
                cMakeListsEditor.blankLine();
                cMakeListsEditor.set("${CMAKE_PROJECT_NAME}_SKETCH", projectName + ".ino");
                cMakeListsEditor.method("generate_arduino_firmware", "${CMAKE_PROJECT_NAME}");

                ArduinoToolchainFiles.copyToDirectory(VfsUtil.findFileByIoFile(projectRoot, true));

                return projectName;
            }
        }
    }

    @Override
    protected void doRunWizard() {
        VirtualFile projectRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(this.adapter.getLocation());
        if (projectRoot == null) {
            return;
        }
        refreshProjectDir(projectRoot);
        final VirtualFile cMakeLists = projectRoot.findChild("CMakeLists.txt");
        if (cMakeLists == null) {
            return;
        }
        final VirtualFile mainSketchFile = projectRoot.findChild(this.adapter.getName() + ".ino");
        if (mainSketchFile == null) {
            return;
        }
        final Project project = CMakeWorkspace.openProject(cMakeLists, null, false);
        if (project == null) {
            return;
        }
        (new OpenFileDescriptor(project, cMakeLists)).navigate(false);
        (new OpenFileDescriptor(project, mainSketchFile)).navigate(true);
    }
}
