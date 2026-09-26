plugins {
    kotlin("multiplatform")
    id("app.cash.sqldelight")
    id("com.android.library") apply false
}
val androidEnabled = providers.gradleProperty("enableAndroid").orNull == "true"
if (androidEnabled) apply(plugin = "com.android.library")
kotlin {
    jvm()
    if (androidEnabled) androidTarget { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    sourceSets {
        val javaMain by creating { dependsOn(commonMain.get()) }
        jvmMain.get().dependsOn(javaMain)
        commonMain.dependencies { implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmMain.dependencies { implementation("app.cash.sqldelight:sqlite-driver:2.1.0") }
        if (androidEnabled) getByName("androidMain").apply {
            dependsOn(javaMain)
            dependencies { implementation("app.cash.sqldelight:android-driver:2.1.0") }
        }
    }
}
if (androidEnabled) extensions.configure<com.android.build.gradle.LibraryExtension> {
    namespace = "com.smartsticky.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
sqldelight {
    databases {
        create("NotesDatabase") { packageName.set("com.smartsticky.database") }
    }
}
