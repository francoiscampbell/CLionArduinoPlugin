package io.github.francoiscambell.clionarduinoplugin.components;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.fileTypes.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;
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
                        FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension(Strings.CPP_EXT);
                        FileTypeManager.getInstance().associateExtension(cpp, Strings.INO_EXT);
                        FileTypeManager.getInstance().associateExtension(cpp, Strings.PDE_EXT);
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
                        FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension(Strings.CPP_EXT);
                        FileTypeManager.getInstance().removeAssociatedExtension(cpp, Strings.INO_EXT);
                        FileTypeManager.getInstance().removeAssociatedExtension(cpp, Strings.PDE_EXT);
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
