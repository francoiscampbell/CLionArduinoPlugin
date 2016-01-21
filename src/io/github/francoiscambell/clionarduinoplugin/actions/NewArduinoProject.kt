package io.github.francoiscambell.clionarduinoplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.impl.welcomeScreen.NewWelcomeScreen
import io.github.francoiscambell.clionarduinoplugin.wizards.NewArduinoProjectWizard

/**
 * Created by francois on 15-08-14.
 */
class NewArduinoProject : AnAction() {
    override fun update(event: AnActionEvent?) {
        val presentation = event!!.presentation
        if (ActionPlaces.isMainMenuOrActionSearch(event.place)) {
            presentation.icon = null
        }

        if (NewWelcomeScreen.isNewWelcomeScreen(event)) {
            event.presentation.icon = AllIcons.Welcome.CreateNewProject
        }

    }

    override fun actionPerformed(e: AnActionEvent) {
        NewArduinoProjectWizard().runWizard()
    }
}
