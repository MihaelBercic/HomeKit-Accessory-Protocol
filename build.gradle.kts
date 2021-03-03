plugins {
    kotlin("jvm") version "1.3.72"
}

group = "si.homeserver"
version = "1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.8.6")
}



tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "MainKt"
        )
    }

    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}