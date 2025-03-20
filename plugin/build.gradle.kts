plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "me.beanes"
version = "alpha"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://repo.codemc.io/repository/maven-releases/") {
        name = "codemc-release"
    }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("it.unimi.dsi:fastutil:8.2.1")
    implementation("org.mongodb:bson:5.2.1")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation(project(":cloud-common"))
}

java {
    val targetJavaVersion = 8
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

    if (JavaVersion.current() < JavaVersion.toVersion(targetJavaVersion)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    val targetJavaVersion = 8
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.shadowJar {
    minimize()
    relocate("it.unimi.dsi.fastutil", "shadow.fastutil")
    relocate("org.bson", "shadow.bson")
    relocate("io.netty", "shadow.netty")
    relocate("net.kyori.platform.bukkit", "shadow.adventure.bukkit")

}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.runServer {
    minecraftVersion("1.8.8")

    downloadPlugins {
        modrinth("packetevents", "qsiAokbs")
    }
}
