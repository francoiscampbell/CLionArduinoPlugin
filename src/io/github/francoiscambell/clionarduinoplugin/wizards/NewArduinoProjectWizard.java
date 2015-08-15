package io.github.francoiscambell.clionarduinoplugin.wizards;

import com.intellij.ide.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.io.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.*;
import com.jetbrains.cidr.cpp.*;
import com.jetbrains.cidr.cpp.cmake.projectWizard.*;
import com.jetbrains.cidr.cpp.cmake.workspace.*;
import com.jetbrains.cidr.lang.*;
import com.jetbrains.cidr.lang.formatting.*;
import com.jetbrains.cidr.lang.parser.*;
import com.jetbrains.cidr.lang.psi.*;
import com.jetbrains.cidr.lang.util.*;
import io.github.francoiscambell.clionarduinoplugin.resources.*;

import java.io.*;

/**
 * Created by francois on 15-08-14.
 */
public class NewArduinoProjectWizard extends CMakeProjectWizard {
    private NewArduinoProjectStepAdapter adapter = new NewArduinoProjectStepAdapter();

    public NewArduinoProjectWizard() {
        super("New Arduino Sketch Project", "NewArduinoSketchProject");
        initWithStep(adapter);
    }

    @Override
    protected boolean tryFinish() {
        String var1 = this.adapter.getLocation();
        File var2 = new File(var1);
        if (var2.exists()) {
            String[] var3 = var2.list(new FilenameFilter() {
                public boolean accept(File var1, String var2) {
                    return !".DS_Store".equalsIgnoreCase(var2) && !"Thumbs.db".equalsIgnoreCase(var2);
                }
            });
            if (var3 != null && var3.length > 0) {
                int var4 = Messages.showYesNoDialog(String
                        .format("Directory \'%s\' already exists and not empty.\nWould you like to continue?", new Object[]{var1}), "Project Directory Already Exists", Messages
                        .getQuestionIcon());
                if (var4 != 0) {
                    return false;
                }
            }
        } else {
            try {
                VfsUtil.createDirectories(var1);
            } catch (RuntimeException var6) {
                var6.printStackTrace();
                throw var6;
            } catch (IOException var7) {
                return false;
            }
        }

        String var8 = var2.getParent();
        if (var8 != null) {
            RecentProjectsManager.getInstance().setLastProjectCreationLocation(var8);
        }

        try {
            createProject(this.adapter.getName(), var1);
            return true;
        } catch (IOException var5) {
            CPPLog.LOG.warn(var5);
            return false;
        }
    }

    public static String createProject(String var0, String var1) throws IOException {
        File var2 = new File(var1);
        File var3 = new File(var2, "CMakeLists.txt");
        if (!var3.exists() && !var3.createNewFile()) {
            throw new IOException("Cannot create file " + var3);
        } else {
            var0 = FileUtil.sanitizeFileName(var0);
            FileUtil.writeToFile(var3, getCMakeListsFileHeader(var0) + "set(CMAKE_TOOLCHAIN_FILE ${CMAKE_SOURCE_DIR}/cmake/ArduinoToolchain.cmake)");
            File var4 = new File(var2, var0 + ".ino");
            if (!var4.exists() && !var4.createNewFile()) {
                throw new IOException("Cannot create file " + var4);
            } else {
                FileUtil.writeToFile(var4, Strings.DEFAULT_ARDUINO_SKETCH_CONTENTS);
                return var0;
            }
        }
    }

    @Override
    protected void doRunWizard() {
        VirtualFile var1 = LocalFileSystem.getInstance().refreshAndFindFileByPath(this.adapter.getLocation());
        refreshProjectDir(var1);
        final VirtualFile var2 = var1.findChild("CMakeLists.txt");
        CPPLog.LOG.assertTrue(var2 != null);
        final VirtualFile var3 = var1.findChild(this.adapter.getLocation() + ".ino");
        CPPLog.LOG.assertTrue(var3 != null);
        final Project var4 = CMakeWorkspace.openProject(var2, (Project) null, false);
        if (var4 != null) {
            runPostWriteAction(var4, new Runnable() {
                public void run() {
                    NewArduinoProjectWizard.a(var4, var3);
                    CMakeProjectWizard.reformatMakeFile(var4, var2);
                }
            });
            (new OpenFileDescriptor(var4, var2)).navigate(false);
            (new OpenFileDescriptor(var4, var3)).navigate(true);
        }
    }

    private static void a(final Project var0, final VirtualFile var1) {
        WriteCommandAction.runWriteCommandAction(var0, new Runnable() {
            public void run() {
                NewArduinoProjectWizard.doReformatMainFile(var0, var1);
            }
        });
    }

    static void doReformatMainFile(Project var0, VirtualFile var1) {
        FileDocumentManager var2 = FileDocumentManager.getInstance();
        Document var3 = var2.getDocument(var1);
        CPPLog.LOG.assertTrue(var3 != null);
        String var4 = var3.getText();
        OCCodeFragment var5 = OCElementFactory
                .codeFragment(var4, var0, (PsiElement) null, OCTokenTypes.OC_FILE, false, false, OCLanguageKind.CPP);
        var5.putUserData(OCFormattingModelBuilder.ALWAYS_CREATE_FULL_MODEL, Boolean.valueOf(true));
        CodeStyleManager.getInstance(var0).reformat(var5);
        String var6 = var5.getText();
        var3.setText(var6);
        var2.saveDocument(var3);
    }
}
