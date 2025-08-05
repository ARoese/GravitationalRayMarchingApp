plugins {
    id("kotlin")
    id("com.toasttab.protokt.v1") version "1.0.0-beta.8"
}

group = "org.fufu"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "3.2.2"
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("com.google.protobuf:protobuf-java:4.31.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

protobuf {
    sourceSets {
        main {
            proto {
                srcDirs("src/GravitationalRayMarchingServer/libgrmproto/protobuf")
            }
        }
    }
}
