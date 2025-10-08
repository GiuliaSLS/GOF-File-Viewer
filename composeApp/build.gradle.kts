import java.util.Calendar
import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

fun readProperties(fileName: String): Properties? {
    val propsFile = project.rootProject.file(fileName)
    if (!propsFile.exists()) {
        return null
    }
    if (!propsFile.canRead()) {
        throw GradleException("Cannot read $fileName")
    }
    return Properties().apply {
        propsFile.inputStream().use { load(it) }
    }
}

val versionProperties = readProperties("version.properties")!!

val appVersionName: String = versionProperties.getProperty("VERSION_NAME")
val appVersionCode: String = versionProperties.getProperty("VERSION_CODE")

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
            packageVersion = appVersionName

            description = "An application to view Giulia GOF files"
            copyright = "© ${
                Calendar.getInstance().get(Calendar.YEAR)
            } Arnau Mora Gras. All rights reserved."
            vendor = "Arnau Mora Gras"

            linux {
                modules("jdk.security.auth")

                debMaintainer = "gof.viewer@arnyminerz.com"
                menuGroup = "GOF Viewer"
                appRelease = appVersionCode
                debPackageVersion = appVersionName
                rpmPackageVersion = appVersionName
            }
            windows {
                upgradeUuid = "85187b51-b97d-49a6-b99d-ff325ef10b86"
                dirChooser = true
                perUserInstall = true
                menuGroup = "GOF Viewer"
                packageName = "GOF Viewer"
                msiPackageVersion = appVersionName
                exePackageVersion = appVersionName
            }
        }
        jvmArgs += "--add-opens=java.base/java.nio=ALL-UNNAMED"
    }
}
