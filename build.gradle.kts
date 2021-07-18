import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    val tgBotApiVersion = "0.35.2"
    val serverJarPath = files("./dependency/spigot-1.17.1.jar")

    implementation(kotlin("stdlib"))
    implementation("dev.inmo:tgbotapi:$tgBotApiVersion")
    implementation("org.mongodb:mongodb-driver-sync:4.3.0")
    compileOnly(serverJarPath)
    compileOnly(files("./dependency/Yum.jar"))
}

tasks {
    withType<ShadowJar> {
        exclude("com.comphenix.protocol:ProtocolLib:4.5.0")
        exclude {
            it?.file?.name == "spigot-1.17.1.jar" || it?.file?.name == "Yum.jar"
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}