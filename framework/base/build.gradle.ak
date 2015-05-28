apply plugin: 'android-library'

android {
    compileSdkVersion rootProject.ext.compileSdkVersion
    buildToolsVersion "$rootProject.ext.buildToolsVersion"

    defaultConfig {
        ndk {
            moduleName "cipher"
            abiFilters "x86", "armeabi"
            if (project.ext.has("authKey") && project.ext.has("androidId")) {
                cFlags "-DAUTH_KEY=\\\"" + project.ext.authKey + "\\\" -DANDROID_ID=\\\"" + project.ext.androidId + "\\\""
            }
        }
    }

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
    provided 'pinyin4android:pinyin4android:1.0'
    compile project(':external:wire-runtime')
    compile 'com.wandoujia:gson:2.2.4'
}