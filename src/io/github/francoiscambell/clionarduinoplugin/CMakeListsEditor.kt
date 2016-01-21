package io.github.francoiscambell.clionarduinoplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import java.util.*

/**
 * Created by francois on 15-08-04.
 */
class CMakeListsEditor private constructor(private val cMakeListsVirtualFile: VirtualFile) {

    private val cMakeListsDocument: Document
        get() = FileDocumentManager.getInstance().getDocument(cMakeListsVirtualFile)

    fun clear() {
        val cMakeLists = cMakeListsDocument
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(null, {
                cMakeLists.deleteString(0, cMakeLists.textLength)
                FileDocumentManager.getInstance().saveDocument(cMakeLists)
            }, null, null, cMakeLists)
        }
    }

    fun addLine(line: Int, text: String) {
        val cMakeLists = cMakeListsDocument
        val lineEndOffset = cMakeLists.getLineEndOffset(line)

        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(null, {
                cMakeLists.insertString(lineEndOffset, text + "\n")
                FileDocumentManager.getInstance().saveDocument(cMakeLists)
            }, null, null, cMakeLists)
        }
    }

    fun appendLine(text: String) {
        var lastLine = cMakeListsDocument.lineCount
        if (lastLine != 0) {
            lastLine--
        }
        addLine(lastLine, text)
    }

    fun blankLine() {
        appendLine("")
    }

    fun minVersion(minVersion: String) {
        method("cmake_minimum_required", "VERSION", minVersion)
    }

    operator fun set(`var`: String, value: String) {
        method("set", `var`, value)
    }

    fun project(projectName: String) {
        method("project", projectName)
    }

    fun method(methodName: String, vararg args: String) {
        val builder = StringBuilder(methodName)
        builder.append('(')
        for (arg in args) {
            builder.append(arg)
            builder.append(' ')
        }
        if (args.size > 0) {
            builder.deleteCharAt(builder.length - 1)
        }
        builder.append(')')
        appendLine(builder.toString())
    }

    companion object {
        private val INSTANCES = WeakHashMap<VirtualFile, CMakeListsEditor>()

        fun getInstance(cMakeLists: VirtualFile): CMakeListsEditor {
            if (!INSTANCES.containsKey(cMakeLists)) {
                INSTANCES.put(cMakeLists, CMakeListsEditor(cMakeLists))
            }
            return INSTANCES[cMakeLists]
        }
    }
}
