package io.github.francoiscambell.clionarduinoplugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.impl.welcomeScreen.*;
import io.github.francoiscambell.clionarduinoplugin.resources.icons.*;
import io.github.francoiscambell.clionarduinoplugin.wizards.*;

/**
 * Created by francois on 15-08-14.
 */
public class NewArduinoProjectAction extends AnAction {
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.isMainMenuOrActionSearch(event.getPlace())) {
            presentation.setIcon(null);
        }

        if (NewWelcomeScreen.isNewWelcomeScreen(event)) {
            event.getPresentation().setIcon(ArduinoIcon.ARDUINO_ICON);
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        (new NewArduinoProjectWizard()).runWizard();
    }
}
