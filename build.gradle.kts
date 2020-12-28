plugins {
    kotlin("jvm") version "1.3.72"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
    implementation("commons-codec:commons-codec:1.14")
    implementation("io.javalin:javalin:3.12.0")
    implementation("com.nimbusds:srp6a:2.1.0")
}