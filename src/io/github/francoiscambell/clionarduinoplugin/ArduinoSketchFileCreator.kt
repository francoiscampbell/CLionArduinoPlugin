package io.github.francoiscambell.clionarduinoplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.github.francoiscambell.clionarduinoplugin.resources.Strings
import java.io.IOException

/**
 * Created by francois on 15-08-04.
 */
object ArduinoSketchFileCreator {

    fun createSketchFileWithName(project: Project, directory: VirtualFile, name: String): VirtualFile? {
        ApplicationManager.getApplication().runWriteAction {
            try {
                val sketch = directory.createChildData(this, name)
                val sketchDocument = FileDocumentManager.getInstance().getDocument(sketch)
                if (sketchDocument != null) {
                    CommandProcessor.getInstance().executeCommand(project, { sketchDocument.setText(Strings.DEFAULT_ARDUINO_SKETCH_CONTENTS) }, null, null, sketchDocument)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.findChild(name)
    }

    fun addFileToCMakeLists(project: Project, file: VirtualFile) {
        CMakeListsEditor.getInstance(project.baseDir)["SOURCE_FILES"] = file.name
    }
}
