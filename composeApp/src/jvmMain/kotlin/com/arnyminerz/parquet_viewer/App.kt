package com.arnyminerz.parquet_viewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnyminerz.parquet_viewer.viewmodel.MainViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.letsPlot

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    val model = viewModel { MainViewModel() }

    val columns by model.columns.collectAsState()
    val rows by model.rows.collectAsState()

    MaterialTheme {
        val filePicker = rememberFilePickerLauncher(
            type = FileKitType.File("gof")
        ) { file ->
            if (file != null) {
                model.onFilePicked(file)
            }
        }

        Scaffold(
            topBar = {
                TextButton(
                    onClick = { filePicker.launch() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open file")
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (!columns.isNullOrEmpty()) {
                    var selectedTabIndex by remember { mutableStateOf(0) }

                    ScrollableTabRow(selectedTabIndex) {
                        for ((index, column) in columns.orEmpty().withIndex()) {
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = { selectedTabIndex = index },
                                text = { Text(column.name) },
                            )
                        }
                    }
                    val values = rows.getOrNull(selectedTabIndex)
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight().weight(1f)
                        ) {
                            items(values.orEmpty()) { value ->
                                ListItem { Text(value.toString()) }
                            }
                        }
                        if (values != null && values.size > 1) {
                            PlotPanel(
                                figure = letsPlot(
                                    data = mapOf("x" to values)
                                ) + geomDensity { x = "x" },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) { computationMessages ->
                                computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                            }
                        }
                    }
                }
            }
        }
    }
}
