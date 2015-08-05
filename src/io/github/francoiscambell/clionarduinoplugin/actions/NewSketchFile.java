package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.ide.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import io.github.francoiscambell.clionarduinoplugin.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;
import io.github.francoiscambell.clionarduinoplugin.resources.icons.*;
import org.jetbrains.annotations.*;

import java.io.*;

/**
 * Created by francois on 15-08-02.
 */
public class NewSketchFile extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final IdeView view = e.getRequiredData(LangDataKeys.IDE_VIEW);

        PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) {
            return;
        }
        final VirtualFile directoryVirtualFile = directory.getVirtualFile();

        boolean loop = false;
        do {
            String filename = getDesiredFilename(project);
            if (filename == null) { //cancel
                return;
            }
            if (filename.isEmpty()) { //no name entered
                showEmptyFilenameError(project);
                loop = true;
                continue;
            }
            filename = correctExtension(filename); //add .ino if the current filename doesn't end with .ino or .pde

            VirtualFile existingFile = directoryVirtualFile.findChild(filename);
            if (existingFile != null) {
                int overwriteChoice = getOverwriteChoice(project); //ask to overwrite file
                switch (overwriteChoice) {
                    case Messages.YES:
                        deleteVirtualFile(existingFile);
                        loop = false;
                        break;
                    case Messages.NO:
                        loop = true;
                        continue;
                    case Messages.CANCEL:
                        return;
                }
            }
            VirtualFile sketch = ArduinoSketchFileCreator
                    .createSketchFileWithName(project, directoryVirtualFile, filename);
//            ArduinoSketchFileCreator.addFileToCMakeLists(project, sketch); //not sure if i need to do this or not
            FileEditorManager.getInstance(project).openFile(sketch, true, true); //open in editor
        } while (loop);
    }

    private void showEmptyFilenameError(Project project) {
        Messages.showErrorDialog(project, Strings.ENTER_FILENAME, Strings.ERROR);
    }

    private int getOverwriteChoice(Project project) {
        return Messages
                .showYesNoCancelDialog(project, Strings.QUESTION_OVERWRITE, Strings.FILE_ALREADY_EXISTS, ArduinoIcon.ARDUINO_ICON);
    }

    private String getDesiredFilename(Project project) {
        return Messages
                .showInputDialog(project, Strings.ENTER_FILENAME, Strings.SKETCH_NAME, ArduinoIcon.ARDUINO_ICON);
    }

    @NotNull
    private String correctExtension(String filename) {
        if (!(filename.endsWith(Strings.DOT_INO_EXT) || filename.endsWith(Strings.DOT_PDE_EXT))) {
            filename = filename + Strings.DOT_INO_EXT;
        }
        return filename;
    }


    private void deleteVirtualFile(final VirtualFile virtualFile) {
        if (virtualFile == null) {
            return;
        }
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    virtualFile.delete(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
