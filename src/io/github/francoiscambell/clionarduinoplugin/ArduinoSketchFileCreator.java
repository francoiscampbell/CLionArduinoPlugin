package io.github.francoiscambell.clionarduinoplugin;

import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;

import java.io.*;

/**
 * Created by francois on 15-08-04.
 */
public class ArduinoSketchFileCreator {

    public static VirtualFile createSketchFileWithName(final Project project, final VirtualFile directory, final String name) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualFile sketch = directory.createChildData(this, name);
                    final Document sketchDocument = FileDocumentManager.getInstance().getDocument(sketch);
                    if (sketchDocument != null) {
                        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                            @Override
                            public void run() {
                                sketchDocument.setText(Strings.DEFAULT_ARDUINO_SKETCH_CONTENTS);
                            }
                        }, null, null, sketchDocument);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return directory.findChild(name);
    }

    public static void addFileToCMakeLists(final Project project, final VirtualFile file) {
        CMakeListsEditor.getInstance(project).set("SOURCE_FILES", file.getName());
    }
}
