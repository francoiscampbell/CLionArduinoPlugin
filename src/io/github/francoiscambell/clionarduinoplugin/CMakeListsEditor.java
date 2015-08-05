package io.github.francoiscambell.clionarduinoplugin;

import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;

/**
 * Created by francois on 15-08-04.
 */
public class CMakeListsEditor {
    private static CMakeListsEditor INSTANCE;
    private Project project;

    private CMakeListsEditor(Project project) {
        this.project = project;
    }

    public static CMakeListsEditor getInstance(Project project) {
        if (INSTANCE == null) {
            INSTANCE = new CMakeListsEditor(project);
        }
        return INSTANCE;
    }

    public VirtualFile getCMakeListsVirtualFile() {
        return project.getBaseDir().findChild(Strings.CMAKE_LISTS_FILENAME);
    }

    public Document getCMakeListsDocument() {
        return FileDocumentManager.getInstance().getDocument(getCMakeListsVirtualFile());
    }

    public void clear() {
        final Document cMakeLists = getCMakeListsDocument();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.deleteString(0, getCMakeListsDocument().getTextLength());
                    }
                }, null, null, cMakeLists);
            }
        });
    }

    public void addLine(int line, final String text) {
        final Document cMakeLists = getCMakeListsDocument();
        final int lineEndOffset = cMakeLists.getLineEndOffset(line);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.insertString(lineEndOffset, text + "\n");
                    }
                }, null, null, cMakeLists);
            }
        });
    }

    public void appendLine(String text) {
        int lastLine = getCMakeListsDocument().getLineCount();
        if (lastLine != 0) {
            lastLine--;
        }
        addLine(lastLine, text);
    }

    public void blankLine() {
        appendLine("");
    }

    public void minVersion(String minVersion) {
        method("cmake_minimum_required", "VERSION", minVersion);
    }

    public void set(String var, String value) {
        method("set", var, value);
    }

    public void project(String projectName) {
        method("project", projectName);
    }

    public void method(String methodName, String... args) {
        StringBuilder builder = new StringBuilder(methodName);
        builder.append('(');
        for (String arg : args) {
            builder.append(arg);
            builder.append(' ');
        }
        if (args.length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(')');
        appendLine(builder.toString());
    }
}
