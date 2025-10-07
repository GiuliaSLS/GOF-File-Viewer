import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.filekit.dialogs.compose)

            implementation(libs.kotlin.letsplot.compose)
            implementation(libs.kotlin.letsplot.kernel)
            implementation(libs.kotlin.letsplot.kmp)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.apache.arrow.dataset)
            implementation(libs.apache.arrow.netty)
            implementation(libs.apache.arrow.vector)
        }
    }
}

// Include --add-opens for Apache Arrow used by Kotlin DataFrame
tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
    )
}

compose.desktop {
    application {
        mainClass = "com.arnyminerz.parquet_viewer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb)
            packageName = "com.arnyminerz.parquet_viewer"
            packageVersion = "1.0.0"
        }
    }
}
