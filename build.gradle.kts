import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import java.io.*
import java.nio.file.Paths
import java.util.zip.*
import kotlin.io.path.absolute

fun getProjectVersion():String = "0.0.8"
project.version = getProjectVersion()
group = "slang"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.0.1"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1.4")
        pluginVerifier()
        zipSigner()
        instrumentationTools()

        jetbrainsRuntime()
        plugin("com.redhat.devtools.lsp4ij:0.13.0")
    }
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

fun getResourcesFolder(): String
{
    return project.projectDir.toString()+"/src/main/resources/";
}

fun createFileWithVersion()
{
    val versionFile: File = File(getResourcesFolder()+"version.txt")
    versionFile.createNewFile()
    val bw: BufferedWriter = BufferedWriter(FileWriter(versionFile))
    bw.write(getProjectVersion());
    bw.close();
}

fun mandatoryTasks()
{
    createFileWithVersion()
}

tasks {
    mandatoryTasks()

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    buildPlugin

    test {
        useJUnitPlatform()
    }

    runIde
    /*
    signPlugin {
        certificateChain.set(System.getenv("SLANG_LSP_CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("SLANG_LSP_PRIVATE_KEY"))
        password.set(System.getenv("SLANG_LSP_PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("SLANG_LSP_PUBLISH_TOKEN"))
    }
    */
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241.0"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.1.4")
            ide(IntelliJPlatformType.CLion, "2024.1.4")
        }
    }
}