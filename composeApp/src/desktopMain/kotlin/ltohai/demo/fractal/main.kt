package ltohai.demo.fractal

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ltohai.demo.fractal.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FractalDAW",
    ) {
        App()
    }
}
