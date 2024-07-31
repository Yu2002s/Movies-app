import com.github.megatronking.stringfog.plugin.StringFogExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("stringfog")
    id("com.google.devtools.ksp")
}

apply(plugin = "stringfog")

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\appkey\\jdy.jks")
            storePassword = "jdy200255"
            keyAlias = "jdy2002"
            keyPassword = "jdy200255"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
        create("release") {
            storeFile = file("D:\\appkey\\jdy.jks")
            storePassword = "jdy200255"
            keyAlias = "jdy2002"
            keyPassword = "jdy200255"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    namespace = "com.dongyu.movies"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dongyu.movies"
        minSdk = 24
        targetSdk = 34
        versionCode = 12
        versionName = "1.0.6-fix"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")

        ndk {
            abiFilters.add("arm64-v8a")
            // abiFilters.add("x86_64")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_HOST", "\"http://192.168.1.18\"")
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            buildConfigField("String", "API_HOST", "\"https://jdynb.xyz\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding {
            enable = true
        }
        buildConfig = true
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

configure<StringFogExtension> {
    // 必要：加解密库的实现类路径，需和上面配置的加解密算法库一致。
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    // 可选：加密开关，默认开启。
    enable = true
    // 可选：指定需加密的代码包路径，可配置多个，未指定将默认全部加密。
    // fogPackages = arrayOf("com.dongyu.movies.utils")
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    // base64或者bytes
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

dependencies {
    implementation(fileTree("libs"))
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("org.litepal.guolindev:core:3.2.3")
    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.17.2")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.github.megatronking.stringfog:xor:5.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.ctiao:DanmakuFlameMaster:0.9.25")
    implementation("com.github.ctiao:ndkbitmap-armv7a:0.9.21")
    implementation("com.github.ctiao:ndkbitmap-x86:0.9.21")
    implementation(project(":dyplayer"))
    implementation(project(":A4ijkplayer"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}