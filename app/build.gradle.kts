// import com.github.megatronking.stringfog.plugin.StringFogExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // id("stringfog")
    id("com.google.devtools.ksp")
}

android {
    lint {
        abortOnError = false
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\jdy2002\\appkey\\jdy.jks")
            storePassword = "jdy200255"
            keyAlias = "jdy2002"
            keyPassword = "jdy200255"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
        create("release") {
            storeFile = file("D:\\jdy2002\\appkey\\jdy.jks")
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
        versionCode = 39
        versionName = "2.1.3-fix"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")

        multiDexEnabled = true

        ndk {
            abiFilters.add("arm64-v8a")
            // abiFilters.add("x86_64")
        }
    }

    buildTypes {

        release {
             isMinifyEnabled = true
             isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    viewBinding {
        enable = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources.excludes.add("META-INF/beans.xml")
    }
}

/*configure<StringFogExtension> {
    // 必要：加解密库的实现类路径，需和上面配置的加解密算法库一致。
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    // 可选：加密开关，默认开启。
    enable = true
    // 可选：指定需加密的代码包路径，可配置多个，未指定将默认全部加密。
    // fogPackages = arrayOf("com.dongyu.movies.utils")
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    // base64或者bytes
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}*/

dependencies {
    implementation(fileTree("libs"))
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    implementation("org.litepal.guolindev:core:3.2.3")
    // https://mvnrepository.com/artifact/com.alibaba/fastjson
    implementation("com.alibaba:fastjson:1.2.83")
    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.17.2")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.annotation:annotation:1.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    // implementation("com.github.megatronking.stringfog:xor:5.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation("com.github.ctiao:DanmakuFlameMaster:0.9.25")
    implementation("com.github.ctiao:ndkbitmap-armv7a:0.9.21")
    implementation("com.github.ctiao:ndkbitmap-x86:0.9.21")
    implementation("com.github.liangjingkanji:BRV:1.6.0")
    implementation("io.github.youth5201314:banner:2.2.3")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    // https://mvnrepository.com/artifact/com.jcraft/jzlib
    implementation(project(":dyplayer"))
    implementation(project(":A4ijkplayer"))
    implementation(project(":screencast"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}