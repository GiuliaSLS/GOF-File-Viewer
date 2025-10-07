package com.arnyminerz.parquet_viewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.text.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.apache.arrow.dataset.file.FileFormat
import org.apache.arrow.dataset.file.FileSystemDatasetFactory
import org.apache.arrow.dataset.jni.NativeMemoryPool
import org.apache.arrow.dataset.scanner.ScanOptions
import org.apache.arrow.memory.BufferAllocator
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.util.AutoCloseables
import org.apache.arrow.vector.Float4Vector
import org.apache.arrow.vector.Float8Vector
import org.apache.arrow.vector.VectorSchemaRoot
import org.apache.arrow.vector.VectorUnloader
import org.apache.arrow.vector.complex.ListVector
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch
import org.apache.arrow.vector.types.pojo.Field

class MainViewModel: ViewModel() {
    private val _columns = MutableStateFlow<List<Field>?>(null)
    val columns = _columns.asStateFlow()

    private val _rows = MutableStateFlow<List<List<Double>>>(emptyList())
    val rows = _rows.asStateFlow()

    fun onFilePicked(platformFile: PlatformFile) {
        val file = platformFile.file
        viewModelScope.launch(Dispatchers.IO) {
            val files = mutableMapOf<String, File>()

            _columns.value = null
            _rows.value = emptyList()

            ZipInputStream(file.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        if (entry.name.endsWith(".parquet")) {
                            val file = File.createTempFile("parquet_viewer_", ".parquet")
                            file.outputStream().use { output ->
                                zis.copyTo(output)
                            }
                            files += (entry.name to file)
                            zis.closeEntry()
                        }
                    }
                    entry = zis.nextEntry
                }
            }

            println("Got ${files.size} files from the zip")
            readParquetFile(files.values.first())
        }
    }

    private fun readParquetFile(file: File) {
        val options = ScanOptions(32768)
        RootAllocator().use { allocator ->
            FileSystemDatasetFactory(
                allocator,
                NativeMemoryPool.getDefault(),
                FileFormat.PARQUET,
                "file:${file.absolutePath}"
            ).use { datasetFactory ->
                datasetFactory.finish().use { dataset ->
                    dataset.newScan(options).use { scanner ->
                        val schema = scanner.schema()
                        if (_columns.value == null)
                            _columns.value = schema.fields

                        scanner.scanBatches().use { reader ->
                            try {
                                while (reader.loadNextBatch()) {
                                    reader.vectorSchemaRoot.use { schemaRoot ->
                                        handleParquetData(schemaRoot)
                                    }
                                }
                            } catch (e: IOException) {
                                System.err.println("Error reading Parquet file: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleParquetData(schemaRoot: VectorSchemaRoot) {
        // For each field (column) in the schemaRoot
        val rowValues = mutableListOf<List<Double>>()
        for (fieldVector in schemaRoot.fieldVectors) {
            println("Field: ${fieldVector.name} (${fieldVector.field.type})")
            if (fieldVector is ListVector) {
                val valueCount = fieldVector.valueCount
                val offsets = fieldVector.offsetBuffer
                val innerVector = fieldVector.dataVector
                if (innerVector is Float8Vector) {
                    println("  Inner vector is Float8Vector with $valueCount values.")
                    for (i in 0 until valueCount) {
                        val start = offsets.getInt(i * 4L)
                        val end = offsets.getInt((i + 1) * 4L)
                        val values = (start until end).map { innerVector.get(it) }
                        rowValues += values
                        println("  Row $i: ${values.size} values")
                    }
                } else {
                    println("  Inner vector is not Float4Vector, but ${innerVector::class.simpleName}")
                }
            } else {
                println("  Not a ListVector, skipping.")
            }
        }
        _rows.value += rowValues
    }
}
