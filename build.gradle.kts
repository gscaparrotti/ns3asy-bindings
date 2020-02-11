plugins {
    `java`
    `maven-publish`
    `signing`
    id ("org.danilopianini.publish-on-central") version "0.2.0"
}

group = "com.github.gscaparrotti"
version = "0.1"

sourceSets {
    main {
    	java.srcDir("src/main")     
    }
    test {
        java.srcDir("src/test")      
    }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
	implementation("net.java.dev.jna:jna:5.5.0")
	implementation("org.apache.commons:commons-lang3:3.9")
	testImplementation("junit:junit:4.12")
}

tasks.register("download", Exec::class) {
    commandLine("sh", "./ns3asy.sh")
}

tasks.test {
    dependsOn(tasks.getByName("download"))
}

tasks.withType(Test::class) {
    environment("LD_LIBRARY_PATH", "tmp/ns3/ns-allinone-3.29/ns-3.29/build/lib")
}

publishOnCentral {
    projectDescription.set("Java bindings for ns3 and ns3asy")
    projectLongName.set("ns3asy-bindings")
    projectUrl.set("https://github.com/gscaparrotti/ns3asy-bindings")
    scmConnection.set("git@github.com:gscaparrotti/ns3asy-bindings.git")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Giacomo Scaparrotti")
                        email.set("giacomo.scaparrotti@studio.unibo.it")
                        url.set("https://github.com/gscaparrotti")
                    }
                }
            }
        }
    }
}
