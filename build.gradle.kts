import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import java.io.*
import java.nio.file.Paths
import java.util.zip.*
import kotlin.io.path.absolute

fun getProjectVersion():String = "0.0.5"
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
        intellijIdeaCommunity("2023.3.6")
        pluginVerifier()
        zipSigner()
        instrumentationTools()

        jetbrainsRuntime()
        bundledPlugin("org.jetbrains.plugins.textmate")
        plugin("com.redhat.devtools.lsp4ij:0.9.0")
    }
    implementation("com.google.code.gson:gson:2.11.0")
}

fun getResourcesFolder(): String
{
    return project.projectDir.toString()+"/src/main/resources/";
}

fun createZipFileOfVSCodeExtension()
{
    val outputZipFile: File = Paths.get(getResourcesFolder()+"slang-vscode-extension.zip").toFile()
    val inputDirectory: File = Paths.get(project.projectDir.toString()+"/slang-vscode-extension/").absolute().toFile()

    val hiddenRelativeDir: String = inputDirectory.toRelativeString(project.projectDir)+"\\.";
    val docRelativeDir: String = inputDirectory.toRelativeString(project.projectDir)+"\\doc";
    // println("relativeDir: "+hiddenRelativeDir)
    ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zipFile ->
        inputDirectory.walkTopDown().forEach { file ->
            val zipFileName = file.toRelativeString(project.projectDir)
            if(!zipFileName.toString().startsWith(hiddenRelativeDir)
                && !zipFileName.toString().startsWith(docRelativeDir)) {
                val entry = ZipEntry("$zipFileName${(if (file.isDirectory) "\\" else "")}")
                // println("entry: " + entry)
                zipFile.putNextEntry(entry)
                if (file.isFile) {
                    file.inputStream().use { fis -> fis.copyTo(zipFile) }
                }
                zipFile.closeEntry()
            }
        }
        zipFile.finish()
    }
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
    createZipFileOfVSCodeExtension()
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
            sinceBuild = "232.0"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2023.3.6")
            ide(IntelliJPlatformType.CLion, "2023.3.6")
        }
    }
}