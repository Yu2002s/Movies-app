// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.android.library") version "8.5.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false

}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        /*classpath("com.github.megatronking.stringfog:gradle-plugin:5.1.0")
        classpath("com.github.megatronking.stringfog:xor:5.0.0")*/
    }
}