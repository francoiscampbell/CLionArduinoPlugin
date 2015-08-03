package io.github.francoiscambell.clionarduinoplugin.filetypes;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.fileTypes.*;
import org.jetbrains.annotations.*;

/**
 * Created by francois on 15-08-03.
 */
public class ArduinoFileTypeRegistration implements ApplicationComponent {
    public ArduinoFileTypeRegistration() {
    }

    public void initComponent() {
        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
            FileTypeManager.getInstance().associateExtension(cpp, "ino");
            FileTypeManager.getInstance().associateExtension(cpp, "pde");
        }));
    }

    public void disposeComponent() {
        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
            FileTypeManager.getInstance().removeAssociatedExtension(cpp, "ino");
            FileTypeManager.getInstance().removeAssociatedExtension(cpp, "pde");
        }));
    }

    @NotNull
    public String getComponentName() {
        return "ArduinoFileTypeRegistration";
    }
}
