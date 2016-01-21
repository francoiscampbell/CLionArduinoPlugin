package io.github.francoiscambell.clionarduinoplugin.resources

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.IOUtils
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * Created by francois on 15-08-03.
 */
object ArduinoToolchainFiles {
    val arduinoToolchainCmake: InputStream
        get() = ArduinoToolchainFiles::class.java!!.getResourceAsStream("arduino-cmake/cmake/ArduinoToolchain.cmake")

    val arduinoCmake: InputStream
        get() = ArduinoToolchainFiles::class.java!!.getResourceAsStream("arduino-cmake/cmake/Platform/Arduino.cmake")

    fun copyToDirectory(projectRoot: VirtualFile) {
        ApplicationManager.getApplication().runWriteAction {
            try {
                val cmakeDirectory = projectRoot.createChildDirectory(this, "cmake")
                val platformDirectory = cmakeDirectory.createChildDirectory(this, "Platform")

                val arduinoToolchain = cmakeDirectory.createChildData(this, "ArduinoToolchain.cmake")
                val arduino = platformDirectory.createChildData(this, "Arduino.cmake")

                val arduinoToolchainOutputStream = arduinoToolchain.getOutputStream(this)
                val arduinoOutputStream = arduino.getOutputStream(this)

                val arduinoToolchainInputStream = arduinoToolchainCmake
                val arduinoInputStream = arduinoCmake

                try {
                    IOUtils.copy(arduinoToolchainInputStream, arduinoToolchainOutputStream)
                    IOUtils.copy(arduinoInputStream, arduinoOutputStream)
                } finally {
                    closeStreams(arduinoToolchainOutputStream,
                            arduinoOutputStream,
                            arduinoToolchainInputStream,
                            arduinoInputStream)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun closeStreams(vararg streams: Closeable) {
        for (c in streams) {
            IOUtils.closeQuietly(c)
        }
    }
}
