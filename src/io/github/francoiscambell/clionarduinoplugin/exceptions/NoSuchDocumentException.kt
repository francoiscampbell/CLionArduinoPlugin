package io.github.francoiscambell.clionarduinoplugin.exceptions

import com.intellij.openapi.vfs.VirtualFile

/**
 * Created by francois on 2016-01-21.
 */
class NoSuchDocumentException : Exception {
    constructor(virtualFile: VirtualFile) : super(virtualFile.name)
}