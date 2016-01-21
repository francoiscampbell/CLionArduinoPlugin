package io.github.francoiscambell.clionarduinoplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import io.github.francoiscambell.clionarduinoplugin.ArduinoSketchFileCreator
import io.github.francoiscambell.clionarduinoplugin.resources.Strings
import io.github.francoiscambell.clionarduinoplugin.resources.icons.ArduinoIcon
import java.io.IOException

/**
 * Created by francois on 15-08-02.
 */
class NewSketchFile : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val view = e.getRequiredData(LangDataKeys.IDE_VIEW)

        val directory = view.orChooseDirectory ?: return
        val directoryVirtualFile = directory.virtualFile

        var loop = false
        do {
            var filename: String? = getDesiredFilename(project) ?: return //cancel
            if (filename.isEmpty()) {
                //no name entered
                showEmptyFilenameError(project)
                loop = true
                continue
            }
            filename = correctExtension(filename) //add .ino if the current filename doesn't end with .ino or .pde

            val existingFile = directoryVirtualFile.findChild(filename)
            if (existingFile != null) {
                val overwriteChoice = getOverwriteChoice(project) //ask to overwrite file
                when (overwriteChoice) {
                    Messages.YES -> {
                        deleteVirtualFile(existingFile)
                        loop = false
                    }
                    Messages.NO -> {
                        loop = true
                        continue
                    }
                    Messages.CANCEL -> return
                }
            }
            val sketch = ArduinoSketchFileCreator.createSketchFileWithName(project, directoryVirtualFile, filename)
            //            ArduinoSketchFileCreator.addFileToCMakeLists(project, sketch); //not sure if i need to do this or not
            FileEditorManager.getInstance(project).openFile(sketch, true, true) //open in editor
        } while (loop)
    }

    private fun showEmptyFilenameError(project: Project) {
        Messages.showErrorDialog(project, Strings.ENTER_FILENAME, Strings.ERROR)
    }

    private fun getOverwriteChoice(project: Project): Int {
        return Messages.showYesNoCancelDialog(project, Strings.QUESTION_OVERWRITE, Strings.FILE_ALREADY_EXISTS, ArduinoIcon.ARDUINO_ICON)
    }

    private fun getDesiredFilename(project: Project): String {
        return Messages.showInputDialog(project, Strings.ENTER_FILENAME, Strings.SKETCH_NAME, ArduinoIcon.ARDUINO_ICON)
    }

    private fun correctExtension(filename: String): String {
        var filename = filename
        if (!(filename.endsWith(Strings.DOT_INO_EXT) || filename.endsWith(Strings.DOT_PDE_EXT))) {
            filename = filename + Strings.DOT_INO_EXT
        }
        return filename
    }


    private fun deleteVirtualFile(virtualFile: VirtualFile?) {
        if (virtualFile == null) {
            return
        }
        ApplicationManager.getApplication().runWriteAction {
            try {
                virtualFile.delete(this)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
