import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.compose") version "1.0.0-alpha4-build348"
}

group = "com.riskrieg"
version = "2.5.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.formdev:flatlaf:1.6")
    implementation("com.formdev:flatlaf-intellij-themes:1.6")
    implementation(compose.materialIconsExtended)

    implementation("com.riskrieg:rkm:1.0.8")
    implementation("org.jgrapht:jgrapht-io:1.5.1")
    implementation("com.github.aaronjyoder:Json-Utilities:1.1.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Riskrieg Map Editor"
            packageVersion = "2.5.0"
            description = "A map editor for Riskrieg."

            val iconsRoot = project.file("src/main/resources/icon/")

            linux {
                iconFile.set(iconsRoot.resolve("linux.png"))
                menuGroup = "Riskrieg"
            }

            windows {
                iconFile.set(iconsRoot.resolve("windows.ico"))
                menuGroup = "Riskrieg"
                dirChooser = true
                perUserInstall = true
            }

            macOS { // Requires a Mac in order to notarize
                iconFile.set(iconsRoot.resolve("macos.icns"))
            }

        }
    }
}