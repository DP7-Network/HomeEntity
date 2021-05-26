import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "cn.thelama"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo.dmulloy2.net/repository/public/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.pengrad:java-telegram-bot-api:5.1.0")
    compileOnly(files("spigot-1.16.5.jar"))
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")
}

tasks {
    withType<ShadowJar> {
        exclude("com.comphenix.protocol:ProtocolLib:4.5.0")
        exclude {
            it?.file?.name == "spigot-1.16.5.jar"
        }
    }
}