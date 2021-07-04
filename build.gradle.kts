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
    val tgBotApiVersion = "0.35.0"
    val serverJarPath = files("./dependency/spigot-1.16.5.jar")

    implementation(kotlin("stdlib"))
    implementation("com.github.pengrad:java-telegram-bot-api:5.1.0")
    implementation("dev.inmo:tgbotapi:$tgBotApiVersion")
    compileOnly(serverJarPath)
    compileOnly(files("./dependency/Yum.jar"))
}

tasks {
    withType<ShadowJar> {
        exclude("com.comphenix.protocol:ProtocolLib:4.5.0")
        exclude {
            it?.file?.name == "spigot-1.16.5.jar" || it?.file?.name == "Yum.jar"
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}