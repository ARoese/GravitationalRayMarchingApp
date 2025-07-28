package org.fufu.grmapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Gravitational Ray Marching App",
    ) {
        App()
    }
}