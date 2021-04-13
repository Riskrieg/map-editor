import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.compose") version "0.3.2"
}

group = "com.riskrieg"
version = "2.0.0"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.aaronjyoder:Json-Utilities:1.0.0a15")
    implementation("com.formdev:flatlaf:1.1.2")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:2.1") // temporary until isSystemDarkTheme() implemented in Desktop
    implementation("com.riskrieg:rkm:1.0.1")
    implementation("org.jgrapht:jgrapht-io:1.5.1")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation(compose.desktop.currentOs)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.useIR = true
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Riskrieg Map Editor"
            packageVersion = "2.0.0"
            description = "A map editor for Riskrieg."

            val secrets = Properties()
            secrets.load(file("secrets.properties").inputStream())

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
                upgradeUuid = secrets["guid"].toString()
            }

            macOS { // Requires a Mac in order to notarize
                iconFile.set(iconsRoot.resolve("macos.icns"))
            }

        }
    }
}