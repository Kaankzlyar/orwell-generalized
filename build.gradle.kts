plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("lib/rmlmapper-8.1.0-r380-all.jar"))
    implementation("org.apache.jena:jena-shacl:5.6.0")
    implementation("org.apache.jena:jena-arq:5.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("orwell.reconciliation.log.enabled", "false")
    onlyIf {
        gradle.startParameter.taskNames.any { taskName ->
            taskName == "test" || taskName.startsWith("test")
        }
    }
}

tasks.named("run") {
    dependsOn("jar")
}

tasks.named("clean") {
    delete("log.txt")
}

application {
    mainClass.set("Main")
}
