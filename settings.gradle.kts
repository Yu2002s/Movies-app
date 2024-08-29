pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        maven {
            setUrl("http://4thline.org/m2")
            isAllowInsecureProtocol = true
        }
        flatDir {
            dir("libs")
        }
    }
}

rootProject.name = "Movies"
include(":app")
include(":A4ijkplayer")
include(":dyplayer")
include(":screencast")
