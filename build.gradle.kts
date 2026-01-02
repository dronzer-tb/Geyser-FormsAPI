plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "org.formsapi"
version = "1.0.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://maven.lenni0451.net/snapshots/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    // Geyser Core (includes session access)
    compileOnly("org.geysermc.geyser:core:2.4.2-SNAPSHOT")

    // Cumulus (Forms) - usually bundled with Geyser
    compileOnly("org.geysermc.cumulus:cumulus:1.1.2")

    // Floodgate API (optional fallback)
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // YAML processing for config
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("FormsAPI.jar")
    relocate("com.google.gson", "org.formsapi.libs.gson")
    relocate("org.yaml.snakeyaml", "org.formsapi.libs.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
