package io.github.francoiscambell.clionarduinoplugin.wizards

import com.intellij.ide.RecentProjectsManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jetbrains.cidr.cpp.CPPLog
import com.jetbrains.cidr.cpp.cmake.projectWizard.CMakeProjectWizard
import com.jetbrains.cidr.cpp.cmake.projectWizard.NewCMakeProjectStepAdapter
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import io.github.francoiscambell.clionarduinoplugin.CMakeListsEditor
import io.github.francoiscambell.clionarduinoplugin.resources.ArduinoToolchainFiles
import io.github.francoiscambell.clionarduinoplugin.resources.Strings
import java.io.File
import java.io.IOException

/**
 * Created by francois on 15-08-14.
 */
class NewArduinoProjectWizard : CMakeProjectWizard("New Arduino Sketch Project", "NewArduinoSketchProject") {
    private val adapter = NewCMakeProjectStepAdapter()

    init {
        initWithStep(adapter)
    }

    override fun tryFinish(): Boolean {
        val projectRootPath = this.adapter.location
        val projectRootDir = File(projectRootPath)
        if (projectRootDir.exists()) {
            val fileList = projectRootDir.list { dir, name -> !".DS_Store".equals(name, ignoreCase = true) && !"Thumbs.db".equals(name, ignoreCase = true) }
            if (fileList != null && fileList.size > 0) {
                val dialogAnswer = Messages.showYesNoDialog("Directory \'%s\' already exists and not empty.\nWould you like to continue?".format(projectRootPath), "Project Directory Already Exists", Messages.getQuestionIcon())
                if (dialogAnswer != 0) {
                    return false
                }
            }
        } else {
            try {
                VfsUtil.createDirectories(projectRootPath)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                throw e
            } catch (e: IOException) {
                return false
            }

        }

        val projectRootDirParentPath = projectRootDir.parent
        if (projectRootDirParentPath != null) {
            RecentProjectsManager.getInstance().lastProjectCreationLocation = projectRootDirParentPath
        }

        try {
            createProject(this.adapter.name, projectRootPath)
            return true
        } catch (e: IOException) {
            CPPLog.LOG.warn(e)
            return false
        }

    }

    override fun doRunWizard() {
        val projectRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(this.adapter.location) ?: return
        CMakeProjectWizard.refreshProjectDir(projectRoot)
        val cMakeLists = projectRoot.findChild("CMakeLists.txt") ?: return
        val mainSketchFile = projectRoot.findChild(this.adapter.name + ".ino") ?: return
        val project = CMakeWorkspace.openProject(cMakeLists, null, false) ?: return
        deleteBuildOutputDir(project)
        OpenFileDescriptor(project, cMakeLists).navigate(false)
        OpenFileDescriptor(project, mainSketchFile).navigate(true)
    }

    private fun deleteBuildOutputDir(project: Project) {
        FileUtil.delete(CMakeWorkspace.getInstance(project).projectGeneratedDir)
    }

    companion object {

        @Throws(IOException::class)
        fun createProject(projectName: String, projectRootPath: String): String {
            val projectRoot = File(projectRootPath)
            val cMakeLists = File(projectRoot, "CMakeLists.txt")
            if (!cMakeLists.exists() && !cMakeLists.createNewFile()) {
                throw IOException("Cannot create file " + cMakeLists)
            } else {
                val sanitizedProjectName = FileUtil.sanitizeFileName(projectName)
                val mainSketchFile = File(projectRoot, sanitizedProjectName + ".ino")
                if (!mainSketchFile.exists() && !mainSketchFile.createNewFile()) {
                    throw IOException("Cannot create file " + mainSketchFile)
                } else {
                    FileUtil.writeToFile(mainSketchFile, Strings.DEFAULT_ARDUINO_SKETCH_CONTENTS)

                    val cMakeListsVirtualFile = VfsUtil.findFileByIoFile(cMakeLists, true) ?: return ""
                    val cMakeListsEditor = CMakeListsEditor.getInstance(cMakeListsVirtualFile)
                    cMakeListsEditor.clear()
                    cMakeListsEditor.minVersion("2.8.4")
                    cMakeListsEditor.set("CMAKE_TOOLCHAIN_FILE", "\${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake")
                    cMakeListsEditor.set("PROJECT_NAME", sanitizedProjectName)
                    cMakeListsEditor.project("\${PROJECT_NAME}")
                    cMakeListsEditor.blankLine()
                    cMakeListsEditor.set("\${CMAKE_PROJECT_NAME}_SKETCH", sanitizedProjectName + ".ino")
                    cMakeListsEditor.method("generate_arduino_firmware", "\${CMAKE_PROJECT_NAME}")


                    val projectRootVirtualFile = VfsUtil.findFileByIoFile(projectRoot, true) ?: return ""
                    ArduinoToolchainFiles.copyToDirectory(projectRootVirtualFile)

                    return sanitizedProjectName
                }
            }
        }
    }
}
