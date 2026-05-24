plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("be.ugent.rml:rmlmapper:8.1.0")
    implementation("be.ugent.idlab.knows:function-agent-java:1.3.0")
    implementation("org.eclipse.rdf4j:rdf4j-rio-api:5.1.0")
    implementation("org.apache.jena:jena-shacl:5.2.0")
    implementation("org.apache.jena:jena-rdfconnection:5.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation("org.yaml:snakeyaml:2.2")

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
    val runTests = gradle.startParameter.taskNames.any { taskName ->
        taskName == "test" || taskName.startsWith("test")
    }
    enabled = runTests
}

tasks.named("run") {
    dependsOn("jar")
}

tasks.named("clean") {
    delete("log.txt")
}

application {
    mainClass.set("Main")
    applicationDefaultJvmArgs = listOf("-Xmx5g")
}
