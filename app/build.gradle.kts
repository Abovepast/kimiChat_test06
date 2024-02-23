plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.kimichat_test06"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kimichat_test06"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.alibaba:fastjson:1.2.76")
    implementation("io.noties.markwon:core:4.6.2")
    implementation ("io.noties.markwon:image:4.6.2") // 如果你需要图片支持
    implementation ("io.noties.markwon:image-glide:4.6.2") // 使用Glide加载图片

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}