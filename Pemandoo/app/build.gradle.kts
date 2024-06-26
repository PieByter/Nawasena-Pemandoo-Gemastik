plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.nawasena.pemandoo"
    compileSdk = 35
    compileSdkPreview = "VanillaIceCream"

    defaultConfig {
        applicationId = "com.nawasena.pemandoo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.annotation:annotation:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    // RecyclerView and ViewPager2
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Shimmer for loading effects
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Retrofit and OkHttp for networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.14"))

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.databinding:databinding-runtime:8.5.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")
    implementation("app.rive:rive-android:8.7.0")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Paging library
    implementation("androidx.paging:paging-common-ktx:3.3.0")
    implementation("androidx.paging:paging-runtime-ktx:3.3.0")
    implementation("androidx.room:room-paging:2.6.1")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // EXIF interface
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Navigation components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Dependency Injection with Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("com.loopj.android:android-async-http:1.4.11")
    testImplementation("com.google.truth:truth:1.4.2")

    // Android Test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.6.0")
    androidTestImplementation("androidx.test:rules:1.6.0")
    androidTestImplementation("androidx.test:runner:1.6.0")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("org.mockito:mockito-android:5.11.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    implementation("androidx.credentials:credentials:1.5.0-alpha02")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-alpha02")
    implementation("androidx.biometric:biometric:1.4.0-alpha01")

    implementation("com.airbnb.android:lottie:3.4.0")
}
