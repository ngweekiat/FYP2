plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.fyp_androidapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fyp_androidapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Ensure this matches your Kotlin version
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core AndroidX dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))

    // Compose UI and Material3
    implementation ("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3:1.1.1")
    implementation ("androidx.compose.foundation:foundation:1.5.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // Navigation Compose (for NavHost and composable destinations)
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // OkHttp for Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Calendar function
    implementation ("com.kizitonwose.calendar:compose:2.5.0")

    // Kotlinx.datetime dependency
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") // Replace with the latest version

    // Add google play services dependencies
    implementation ("com.google.android.gms:play-services-auth:20.7.0") // Latest Google Sign-In SDK

    // Add firebase dependencies
    implementation ("com.google.firebase:firebase-auth:22.0.0")

    // Coil for loading google profile picture
    implementation("io.coil-kt:coil-compose:2.0.0")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")

    // Android Instrumented Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debugging and Development Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Testing Dependencies
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation ("org.mockito:mockito-core:5.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.7")

    // Room dependencies
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx")
    ksp("androidx.room:room-compiler:2.6.1")

    // gemini android sdk
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")


}
tasks.withType<Test> {
    useJUnitPlatform()
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}