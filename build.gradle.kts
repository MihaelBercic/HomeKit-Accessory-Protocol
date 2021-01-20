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
    implementation("io.javalin:javalin:3.12.0")

    // implementation("net.i2p.crypto:eddsa:0.3.0")
    implementation("com.google.crypto.tink:tink:1.5.0")

}