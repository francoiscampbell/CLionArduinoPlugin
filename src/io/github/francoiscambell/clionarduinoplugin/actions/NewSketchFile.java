package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.ide.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;

/**
 * Created by francois on 15-08-02.
 */
public class NewSketchFile extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final IdeView view = e.getRequiredData(LangDataKeys.IDE_VIEW);

        final PsiDirectory directory = view.getOrChooseDirectory();
        if (directory == null) {
            return;
        }

        ApplicationManager.getApplication().runWriteAction(() -> {
            //create sketch file
            PsiFile sketch = directory.createFile("test3.ino");
            Document sketchDocument = PsiDocumentManager.getInstance(project).getDocument(sketch);
            CommandProcessor.getInstance().executeCommand(project, () -> {
                sketchDocument.setText("#include <Arduino.h>\n\nvoid setup() {\n\n}\n\nvoid loop() {\n\n}");
            }, null, null, sketchDocument);

            //add the file to CMakeLists.txt sources
            VirtualFile cmakeLists = project.getBaseDir().findChild("CMakeLists.txt");
            PsiFile cmakeListsPsiFile = PsiManager.getInstance(project).findFile(cmakeLists);
            Document cmakeListsDocument = PsiDocumentManager.getInstance(project).getDocument(cmakeListsPsiFile);
            CommandProcessor.getInstance().executeCommand(project, () -> {
                cmakeListsDocument.insertString(cmakeListsDocument.getTextLength(), "\nset(SOURCE_FILES test3.ino)");
            }, null, null, cmakeListsDocument);

            //open the file in the editor
            FileEditorManager.getInstance(project).openFile(sketch.getVirtualFile(), true, true);
        });
    }
}
