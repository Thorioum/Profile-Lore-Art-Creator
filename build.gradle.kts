plugins {
    id("java")
    id("application")
}
application {
    mainClass.set("net.thorioum.ProfileLoreArtUtil")
}

group = "net.thorioum"
version = "1.01"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
}