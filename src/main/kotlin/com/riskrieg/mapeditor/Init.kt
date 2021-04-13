package com.riskrieg.mapeditor

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import com.riskrieg.mapeditor.model.EditorModel
import com.riskrieg.mapeditor.ui.Editor
import com.riskrieg.mapeditor.ui.component.MyMenuBar
import javax.imageio.ImageIO
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException


class Init {

    private val themeDetector = OsThemeDetector.getDetector()

    private val model: EditorModel = EditorModel()

    fun start() {
        if (themeDetector.isDark) {
            FlatDarkLaf.install()
        } else {
            FlatLightLaf.install()
        }

        themeDetector.registerListener { isDark: Boolean ->
            if (isDark) {
                try {
                    UIManager.setLookAndFeel(FlatDarkLaf())
                    FlatLaf.updateUI()
                } catch (e: UnsupportedLookAndFeelException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    UIManager.setLookAndFeel(FlatLightLaf())
                    FlatLaf.updateUI()
                } catch (e: UnsupportedLookAndFeelException) {
                    e.printStackTrace()
                }
            }
        }

        Window(
            title = "${Constants.NAME} Map Editor v${Constants.VERSION}",
            icon = ImageIO.read(Init::class.java.classLoader.getResourceAsStream("icon/icon.png")),
            size = IntSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT),
            menuBar = MyMenuBar(model)
        ) {
            DesktopMaterialTheme {
                Editor(model).init()
            }
        }
    }

}