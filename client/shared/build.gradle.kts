plugins {
    kotlin("multiplatform")
    id("app.cash.sqldelight")
}
kotlin {
    jvm()
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmMain.dependencies { implementation("app.cash.sqldelight:sqlite-driver:2.1.0") }
    }
}
sqldelight {
    databases {
        create("NotesDatabase") { packageName.set("com.smartsticky.database") }
    }
}
