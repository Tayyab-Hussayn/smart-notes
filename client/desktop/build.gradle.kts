plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
kotlin {
    jvm()
    sourceSets {
        jvmTest.dependencies { implementation(kotlin("test")) }
        jvmMain.dependencies {
            implementation(project(":client:shared"))
            implementation(compose.desktop.currentOs)
            implementation(compose.material3)
            implementation("app.cash.sqldelight:sqlite-driver:2.1.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
        }
    }
}
compose.desktop { application { mainClass = "com.smartsticky.app.MainKt" } }
