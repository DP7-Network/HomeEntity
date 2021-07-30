import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    java
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("maven-publish")
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

publishing {
    publications {
        create<MavenPublication>("HomeEntity") {
            artifact(tasks["shadowJar"])
            pom {
                name.set("HomeEntity")
                artifactId = "homeentity"
                description.set("A Minecraft spigot server plugin for DP7 Charmless")
                url.set("https://github.com/DP7-Network/HomeEntity")
                licenses {
                    license {
                        name.set("Anti 996 License Version 1.0")
                        url.set("https://github.com/996icu/996.ICU/blob/master/LICENSE")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/DP7-Network/HomeEntity.git")
                    developerConnection.set("scm:git:git://github.com/DP7-Network/HomeEntity.git")
                    url.set("https://github.com/DP7-Network/HomeEntity")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GithubPackages"
            url = URI.create("https://maven.pkg.github.com/DP7-Network/HomeEntity")
            credentials {
                username = System.getenv("GH_ACTOR")
                password = System.getenv("GH_TOKEN")
            }
        }
    }
}