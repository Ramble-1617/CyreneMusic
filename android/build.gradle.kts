import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

allprojects {
    repositories {
        // 阿里云镜像（加速国内下载）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }

        google()
        mavenCentral()
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

// Align Kotlin bytecode with the Java 11 target used by the app.
subprojects {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

// Flutter plugins may set Java compatibility during their own evaluation.
// Apply this after all projects are configured, without JavaCompile --release
// (which the Android Gradle plugin does not support).
gradle.projectsEvaluated {
    subprojects.forEach { subproject ->
        subproject.tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
