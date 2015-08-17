package io.github.francoiscambell.clionarduinoplugin;

import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.*;

import java.util.*;

/**
 * Created by francois on 15-08-04.
 */
public class CMakeListsEditor {
    private static Map<VirtualFile, CMakeListsEditor> INSTANCES = new WeakHashMap<VirtualFile, CMakeListsEditor>();
    private VirtualFile cMakeListsVirtualFile;

    private CMakeListsEditor(VirtualFile cMakeLists) {
        this.cMakeListsVirtualFile = cMakeLists;
    }

    public static CMakeListsEditor getInstance(VirtualFile cMakeLists) {
        if (!INSTANCES.containsKey(cMakeLists)) {
            INSTANCES.put(cMakeLists, new CMakeListsEditor(cMakeLists));
        }
        return INSTANCES.get(cMakeLists);
    }

    private VirtualFile getCMakeListsVirtualFile() {
        return cMakeListsVirtualFile;
    }

    private Document getCMakeListsDocument() {
        return FileDocumentManager.getInstance().getDocument(getCMakeListsVirtualFile());
    }

    public void clear() {
        final Document cMakeLists = getCMakeListsDocument();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(null, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.deleteString(0, cMakeLists.getTextLength());
                        FileDocumentManager.getInstance().saveDocument(cMakeLists);
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
                CommandProcessor.getInstance().executeCommand(null, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.insertString(lineEndOffset, text + "\n");
                        FileDocumentManager.getInstance().saveDocument(cMakeLists);
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
