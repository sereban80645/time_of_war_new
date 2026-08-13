}
""")

# 6. android/app/build.gradle.kts (Модуль застосунку)
with open("android/app/build.gradle.kts", "w", encoding="utf-8") as f:
    f.write("""plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.time_of_war"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.example.time_of_war"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}
""")

# 7. android/app/src/main/AndroidManifest.xml
os.makedirs("android/app/src/main", exist_ok=True)
with open("android/app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
    f.write("""<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="time_of_war"
        android:name="${applicationName}"
        android:icon="@mipmap/ic_launcher">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/LaunchTheme"
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|smallestScreenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
            android:hardwareAccelerated="true"
            android:windowSoftInputMode="adjustResize">
            <meta-data
              android:name="io.flutter.embedding.android.NormalTheme"
              android:resource="@style/NormalTheme"
              />
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <meta-data
            android:name="flutterEmbedding"
            android:value="2" />
    </application>
</manifest>
""")

print("✅ Нова чиста структура android успішно створена!")
EOF

python3 rebuild_android_clean.py
rm rebuild_android_clean.py
# Фіксуємо workmanager 0.5.2 у pubspec.yaml
python3 -c "
with open('pubspec.yaml', 'r') as f:
    lines = f.readlines()
with open('pubspec.yaml', 'w') as f:
    for line in lines:
        if line.strip().startswith('workmanager:'):
            f.write('  workmanager: 0.5.2\n')
        else:
            f.write(line)
"
git add android/ pubspec.yaml
git commit -m "Refactor: Complete modern rebuild of android folder (Kotlin DSL, Java 17, SDK 35)"
git push origin main
cat << 'EOF' > fix_evaluation_crash.py
gradle_content = """allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
    // Безпечно конфігуруємо всі плагіни без afterEvaluate
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
            compileSdk = 35
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

// Це обов'язково має бути останнім блоком!
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
"""

with open("android/build.gradle.kts", "w", encoding="utf-8") as f:
    f.write(gradle_content)

print("✅ Виправлено порядок завантаження (evaluationDependsOn) у build.gradle.kts")
EOF

python3 fix_evaluation_crash.py
rm fix_evaluation_crash.py
git add android/build.gradle.kts
git commit -m "Fix: Resolve Project.afterEvaluate crash by using safe plugin configuration"
git push origin main
cat << 'EOF' > fix_memory.py
with open("android/gradle.properties", "w", encoding="utf-8") as f:
    f.write("org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m\n")
    f.write("android.useAndroidX=true\n")
    f.write("android.enableJetifier=true\n")
print("✅ Файл gradle.properties створено (виділено 4 ГБ пам'яті)")
EOF

python3 fix_memory.py
rm fix_memory.py
git add android/gradle.properties
git commit -m "Fix: Allocate 4GB RAM in gradle.properties to prevent GC thrashing"
git push origin main
cat << 'EOF' > fix_groovy_final.py
import os, shutil

# 1. Видаляємо всі .kts файли, щоб не було конфліктів дублювання
for root, dirs, files in os.walk("android"):
    for file in files:
        if file.endswith(".kts"):
            os.remove(os.path.join(root, file))

# 2. android/gradle/wrapper/gradle-wrapper.properties
os.makedirs("android/gradle/wrapper", exist_ok=True)
with open("android/gradle/wrapper/gradle-wrapper.properties", "w", encoding="utf-8") as f:
    f.write("""distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-all.zip
""")

# 3. android/gradle.properties (виділяємо 4 ГБ RAM)
with open("android/gradle.properties", "w", encoding="utf-8") as f:
    f.write("""org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
android.useAndroidX=true
android.enableJetifier=true
""")

# 4. android/settings.gradle
with open("android/settings.gradle", "w", encoding="utf-8") as f:
    f.write("""pluginManagement {
    def flutterSdkPath = {
        def properties = new Properties()
        def file = new File(rootProject.projectDir, "local.properties")
        if (file.exists()) {
            file.withReader("UTF-8") { reader -> properties.load(reader) }
        }
        def flutterSdkPath = properties.getProperty("flutter.sdk")
        assert flutterSdkPath != null : "flutter.sdk not set in local.properties"
        return flutterSdkPath
    }()

    includeBuild("$flutterSdkPath/packages/flutter_tools/gradle")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id "dev.flutter.flutter-plugin-loader" version "1.0.0"
    id "com.android.application" version "8.3.2" apply false
    id "org.jetbrains.kotlin.android" version "1.9.24" apply false
}

include ":app"
""")

# 5. android/build.gradle
with open("android/build.gradle", "w", encoding="utf-8") as f:
    f.write("""buildscript {
    ext.kotlin_version = '1.9.24'
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.buildDir = '../build'
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
}

subprojects {
    project.evaluationDependsOn(':app')
}

subprojects { subproject ->
    subproject.afterEvaluate {
        if (subproject.hasProperty('android')) {
            subproject.android {
                compileSdkVersion 35
                buildToolsVersion "35.0.0"
            }
        }
    }
}

tasks.register("clean", Delete) {
    delete rootProject.buildDir
}
""")

# 6. android/app/build.gradle
os.makedirs("android/app", exist_ok=True)
with open("android/app/build.gradle", "w", encoding="utf-8") as f:
    f.write("""plugins {
    id "com.android.application"
    id "kotlin-android"
    id "dev.flutter.flutter-gradle-plugin"
}

android {
    namespace "com.example.time_of_war"
    compileSdkVersion 35
    buildToolsVersion "35.0.0"

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    defaultConfig {
        applicationId "com.example.time_of_war"
        minSdkVersion 21
        targetSdkVersion 35
        versionCode 1
        versionName "1.0.0"
    }

    buildTypes {
        release {
            signingConfig signingConfigs.debug
        }
    }
}

flutter {
    source "../.."
}
""")

print("✅ Перехід на стандартизований Groovy-конфіг із compileSdkVersion 35 завершено!")
EOF

python3 fix_groovy_final.py
rm fix_groovy_final.py
git add android/
git commit -m "Fix: Revert to standard Groovy build configs with explicit compileSdkVersion 35"
git push origin main
cat << 'EOF' > fix_final_groovy_safe.py
import os

build_gradle_content = """buildscript {
    ext.kotlin_version = '1.9.24'
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.buildDir = '../build'
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
}

subprojects {
    project.evaluationDependsOn(':app')
}

subprojects { subproject ->
    def setCompileSdk = {
        if (subproject.hasProperty('android')) {
            subproject.android {
                compileSdkVersion 35
                buildToolsVersion "35.0.0"
                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_17
                    targetCompatibility JavaVersion.VERSION_17
                }
            }
        }
    }

    // Безпечна перевірка: якщо проєкт вже оброблений — виконуємо одразу
    if (subproject.state.executed) {
        setCompileSdk()
    } else {
        subproject.afterEvaluate {
            setCompileSdk()
        }
    }
}

tasks.register("clean", Delete) {
    delete rootProject.buildDir
}
"""

with open("android/build.gradle", "w", encoding="utf-8") as f:
    f.write(build_gradle_content)

print("✅ android/build.gradle безпечно оновлено з перевіркою стану")
EOF

python3 fix_final_groovy_safe.py
rm fix_final_groovy_safe.py
git add android/build.gradle
git commit -m "Fix: Safely check subproject execution state to prevent evaluation exception"
git push origin main
cat << 'EOF' > fix_gradle_final.py
build_gradle_content = """buildscript {
    ext.kotlin_version = '1.9.24'
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.buildDir = '../build'
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
}

subprojects {
    project.evaluationDependsOn(':app')
}

subprojects { subproject ->
    // КЛЮЧОВИЙ ФІКС: Ігноруємо головний модуль 'app', оновлюємо ТІЛЬКИ плагіни
    if (subproject.name != 'app') {
        subproject.afterEvaluate {
            if (subproject.hasProperty('android')) {
                subproject.android {
                    compileSdkVersion 35
                    buildToolsVersion "35.0.0"
                    
                    compileOptions {
                        sourceCompatibility JavaVersion.VERSION_17
                        targetCompatibility JavaVersion.VERSION_17
                    }
                }
            }
        }
    }
}

tasks.register("clean", Delete) {
    delete rootProject.buildDir
}
"""

with open("android/build.gradle", "w", encoding="utf-8") as f:
    f.write(build_gradle_content)

print("✅ Файл build.gradle оновлено: модуль app безпечно виключено з циклу оновлення")
EOF

python3 fix_gradle_final.py
rm fix_gradle_final.py
git add android/build.gradle
git commit -m "Fix: Exclude app module from subproject compileSdkVersion override to fix evaluation error"
git push origin main
cat << 'EOF' > fix_gradle_order.py
build_gradle_content = """buildscript {
    ext.kotlin_version = '1.9.24'
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.buildDir = '../build'
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
}

// 1. СПОЧАТКУ реєструємо правило для всіх плагінів
subprojects { subproject ->
    subproject.afterEvaluate {
        if (subproject.hasProperty('android')) {
            subproject.android {
                compileSdkVersion 35
                buildToolsVersion "35.0.0"
                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_17
                    targetCompatibility JavaVersion.VERSION_17
                }
            }
        }
    }
}

// 2. І ТІЛЬКИ ПОТІМ даємо команду на завантаження (evaluation)
subprojects {
    project.evaluationDependsOn(':app')
}

tasks.register("clean", Delete) {
    delete rootProject.buildDir
}
"""

with open("android/build.gradle", "w", encoding="utf-8") as f:
    f.write(build_gradle_content)

print("✅ Порядок блоків у build.gradle виправлено!")
EOF

python3 fix_gradle_order.py
rm fix_gradle_order.py
git add android/build.gradle
git commit -m "Fix: Register afterEvaluate hooks BEFORE evaluationDependsOn to prevent crash"
git push origin main
