import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.1"
}

group = "com.riskrieg"
version = "2.7.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.formdev:flatlaf:1.6.5")
    implementation("com.formdev:flatlaf-intellij-themes:1.6.5")
    implementation(compose.materialIconsExtended)

    implementation("com.github.aaronjyoder:polylabel-java-mirror:1.3.0")


    implementation("com.riskrieg:rkm:1.0.8")
    implementation("org.jgrapht:jgrapht-io:1.5.1")
    implementation("com.github.aaronjyoder:json-utilities:1.1.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
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
            }

            macOS { // Requires a Mac in order to notarize
                iconFile.set(iconsRoot.resolve("macos.icns"))
            }

        }
    }
}