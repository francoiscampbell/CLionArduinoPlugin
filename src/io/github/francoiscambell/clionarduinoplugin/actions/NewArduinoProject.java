package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.icons.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.impl.welcomeScreen.*;
import io.github.francoiscambell.clionarduinoplugin.wizards.*;

/**
 * Created by francois on 15-08-14.
 */
public class NewArduinoProject extends AnAction {
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.isMainMenuOrActionSearch(event.getPlace())) {
            presentation.setIcon(null);
        }

        if (NewWelcomeScreen.isNewWelcomeScreen(event)) {
            event.getPresentation().setIcon(AllIcons.Welcome.CreateNewProject);
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        (new NewArduinoProjectWizard()).runWizard();
    }
}
