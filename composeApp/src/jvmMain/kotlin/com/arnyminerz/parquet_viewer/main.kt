package com.arnyminerz.parquet_viewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() {
    // Initialize FileKit
    FileKit.init(appId = "ParquetViewer")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Giulia GOF Viewer",
        ) {
            App()
        }
    }
}