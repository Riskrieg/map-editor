import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "com.riskrieg"
version = "2.8.1"
repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.formdev:flatlaf:2.3")
    implementation("com.formdev:flatlaf-intellij-themes:2.3")
    implementation(compose.materialIconsExtended)

    implementation("io.github.aaronjyoder:fill:1.0.0-0.2206")
    implementation("com.github.aaronjyoder:polylabel-java-mirror:1.3.0")

    implementation("com.riskrieg:map:1.0.0-2.2206")
    implementation("com.riskrieg:palette:1.1.0-1.2206")
    implementation("com.riskrieg:codec:1.0.0-3.2206")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3") // Needed for workaround at EditorModel.kt#343

    implementation("org.jgrapht:jgrapht-io:1.5.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
}

fun getProjectProperty(name: String) = project.properties[name] as? String

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Riskrieg Map Editor"
            packageVersion = version.toString()
            description = "The official map editor for Riskrieg."
            vendor = "Riskrieg"

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
                upgradeUuid = getProjectProperty("guid")
            }

            macOS { // Requires a Mac in order to notarize
                iconFile.set(iconsRoot.resolve("macos.icns"))
            }

        }
    }
}