plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "io.github.shitamori1272"
version = "0.1.0"

// サーバー（start.command）と同じ Paper のビルドに合わせる
val minecraftVersion = "26.3"
val paperBuild = "257"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion.build.$paperBuild-stable")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    // JDK 25 以降ならどれでもビルドでき、Java 25 向けのクラスファイルを出力する
    toolchain.languageVersion.set(JavaLanguageVersion.current())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version, "apiVersion" to minecraftVersion)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.runServer {
    minecraftVersion(minecraftVersion)
}
