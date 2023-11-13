plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.sds.toasterlibrary"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
}

/*
 * Al ejecutar en el terminal ./gradlew publishToMavenLocal esta tarea crea
 * el .aar y el pom.xml dentro de la carpeta .m2 (Maven local)
 */
project.afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.sds"
                artifactId = "toasterLibrary"
                version = "1.0.0"
            }

            create<MavenPublication>("ReleaseAar") {
                groupId = "com.sds"
                artifactId = "toasterLibrary"
                version = "1.0.0"
                afterEvaluate {
                    artifact(tasks.getByName("bundleReleaseAar"))
                }
            }

            //Crea el .aar en build/outputs/aar
            create<MavenPublication>("mavenJava") {
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }
}

// Al ejecutar ./gradlew withJavadocJar y ./gradlew withSourcesJar genera el .jar en build/libs

val sourceFiles = android.sourceSets.getByName("main").java.getSourceFiles()

tasks.register<Javadoc>("withJavadoc") {
    isFailOnError = false
    dependsOn(tasks.named("compileDebugSources"), tasks.named("compileReleaseSources"))

    // add Android runtime classpath
    android.bootClasspath.forEach { classpath += project.fileTree(it) }

    // add classpath for all dependencies
    android.libraryVariants.forEach { variant ->
        variant.javaCompileProvider.get().classpath.files.forEach { file ->
            classpath += project.fileTree(file)
        }
    }

    source = sourceFiles
}

tasks.register<Jar>("withJavadocJar") {
    archiveClassifier.set("javadoc")
    dependsOn(tasks.named("withJavadoc"))
    val destination = tasks.named<Javadoc>("withJavadoc").get().destinationDir
    from(destination)
}

tasks.register<Jar>("withSourcesJar") {
    archiveClassifier.set("sources")
    from(sourceFiles)
}


