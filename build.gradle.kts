plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "games.dripdrop"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/jcenter")
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.minebench.de/")
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("bungee.yml") {
        expand(props)
    }
    filesMatching("plugin.yml") {
        expand(props)
    }
}
