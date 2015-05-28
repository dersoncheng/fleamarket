apply plugin: 'android-library'

android {
    compileSdkVersion rootProject.ext.compileSdkVersion
    buildToolsVersion "$rootProject.ext.buildToolsVersion"

    sourceSets {
        main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src']
            aidl.srcDirs = ['src']
            res.srcDirs = ['res']
            assets.srcDirs = ['assets']
            jniLibs.srcDirs = ['libs']
        }
    }
}

dependencies {
    compile project(':framework:base')
    compile project(':external:volley')
    provided 'com.android.support:support-v4:19.0.0'
    provided 'com.jakewharton:disklrucache:2.0.2'
}