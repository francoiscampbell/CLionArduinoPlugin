package io.github.francoiscambell.clionarduinoplugin.filetypes;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.fileTypes.*;
import org.jetbrains.annotations.*;

/**
 * Created by francois on 15-08-03.
 */
public class ArduinoFileTypeRegistration implements ApplicationComponent {
    public void initComponent() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
                        FileTypeManager.getInstance().associateExtension(cpp, "ino");
                        FileTypeManager.getInstance().associateExtension(cpp, "pde");
                    }
                });
            }
        });
    }

    public void disposeComponent() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
                        FileTypeManager.getInstance().removeAssociatedExtension(cpp, "ino");
                        FileTypeManager.getInstance().removeAssociatedExtension(cpp, "pde");
                    }
                });
            }
        });
    }

    @NotNull
    public String getComponentName() {
        return "ArduinoFileTypeRegistration";
    }
}
