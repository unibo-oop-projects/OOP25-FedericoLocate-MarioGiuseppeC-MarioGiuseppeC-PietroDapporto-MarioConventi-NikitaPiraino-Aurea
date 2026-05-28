plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    // You can run your app via task "run": ./gradlew run
    application

    // Plugin official of JavaFX
    id("org.openjfx.javafxplugin") version "0.1.0"

    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.gradleup.shadow") version "9.4.0"
    id("org.danilopianini.gradle-java-qa") version "1.167.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Java version used to compile and run the project
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Configuration auto of JavaFX
javafx {
    version = "23.0.2"
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics", "javafx.media")
}

dependencies {
    // Suppressions for SpotBugs
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    // Example library: Guava. Add what you need (and use the latest version where appropriate).
    // implementation("com.google.guava:guava:28.1-jre")

    // The BOM (Bill of Materials) synchronizes all the versions of Junit coherently.
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    // The annotations, assertions and other elements we want to have access when compiling our tests.
    testImplementation("org.junit.jupiter:junit-jupiter")
    // The engine that must be available at runtime to run the tests.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Needed for the use of the .yaml file
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    
    // TestFX 
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")
    
    // Hamcrest
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.withType<Test> {
    // Enables JUnit 5 Jupiter module
    useJUnitPlatform()
}

application {
    mainClass.set("it.unibo.aurea.Main")
}