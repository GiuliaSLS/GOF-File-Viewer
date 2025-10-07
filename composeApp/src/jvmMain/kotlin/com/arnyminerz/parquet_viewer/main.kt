package com.arnyminerz.parquet_viewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Parquet Viewer",
    ) {
        App()
    }
}