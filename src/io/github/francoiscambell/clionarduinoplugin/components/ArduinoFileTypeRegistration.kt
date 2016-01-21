package io.github.francoiscambell.clionarduinoplugin.components

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileTypes.FileTypeManager
import io.github.francoiscambell.clionarduinoplugin.resources.Strings

/**
 * Created by francois on 15-08-03.
 */
class ArduinoFileTypeRegistration : ApplicationComponent {

    override fun initComponent() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                val cpp = FileTypeManager.getInstance().getFileTypeByExtension(Strings.CPP_EXT)
                FileTypeManager.getInstance().associateExtension(cpp, Strings.INO_EXT)
                FileTypeManager.getInstance().associateExtension(cpp, Strings.PDE_EXT)
            }
        }
    }

    override fun disposeComponent() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                val cpp = FileTypeManager.getInstance().getFileTypeByExtension(Strings.CPP_EXT)
                FileTypeManager.getInstance().removeAssociatedExtension(cpp, Strings.INO_EXT)
                FileTypeManager.getInstance().removeAssociatedExtension(cpp, Strings.PDE_EXT)
            }
        }
    }

    override fun getComponentName(): String {
        return "ArduinoFileTypeRegistration"
    }
}
