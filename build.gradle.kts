plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1" // Use the latest version
}
application {
    mainClass.set("net.thorioum.ProfileLoreArtUtil")
}

group = "net.thorioum"
version = "1.01"

repositories {
    mavenCentral()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.thorioum.ProfileLoreArtUtil"
    }
}
tasks {
    shadowJar {
        archiveClassifier.set("build")
        manifest {
            attributes["Main-Class"] = "net.thorioum.ProfileLoreArtUtil"
        }
    }
    jar {
        enabled = false
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
}