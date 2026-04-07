import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("test-generator")
}

val APP_PACKAGE: String by project
val APP_VERSION_CODE: String by project
val APP_VERSION_NAME: String by project
val RELEASE_STORE_PASSWORD: String by project
val RELEASE_KEY_ALIAS: String by project
val RELEASE_KEY_PASSWORD: String by project

repositories {
	mavenCentral()
	google()
}

configurations.maybeCreate("compile").exclude(module = "support-v4")
configurations.all {
	exclude(group = "com.google.firebase", module = "firebase-core")
	exclude(group = "androidx.recyclerview", module = "recyclerview")
}

dependencies {
	implementation(project(":TMessagesProj"))
	coreLibraryDesugaring(libs.desugar.jdk.libs)

	androidTestImplementation(libs.junit4)
	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.test.runner)
	androidTestImplementation(libs.kotlin.test)
	androidTestImplementation(libs.kotlin.test.junit)
	androidTestImplementation(libs.fixture)
}

android {
	compileSdk = 36

	defaultConfig {
		applicationId = APP_PACKAGE
	}

	sourceSets.getByName("main").jniLibs.srcDirs("../TMessagesProj/jni/")
	testBuildType = "debug"

	lint {
		disable.addAll(listOf("MissingTranslation", "ExtraTranslation", "BlockedPrivateApi"))
		checkReleaseBuilds = false
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
		isCoreLibraryDesugaringEnabled = true
	}

	kotlinOptions {
		jvmTarget = "1.8"
	}

	signingConfigs {
		getByName("debug") {
			storeFile = file("../TMessagesProj/config/release.keystore")
			storePassword = RELEASE_STORE_PASSWORD
			keyAlias = RELEASE_KEY_ALIAS
			keyPassword = RELEASE_KEY_PASSWORD
		}
		create("release") {
			storeFile = file("../TMessagesProj/config/release.keystore")
			storePassword = RELEASE_STORE_PASSWORD
			keyAlias = RELEASE_KEY_ALIAS
			keyPassword = RELEASE_KEY_PASSWORD
		}
	}

	buildTypes {
		getByName("debug") {
			isDebuggable = true
			isJniDebuggable = true
			signingConfig = signingConfigs.getByName("debug")
			applicationIdSuffix = ".web"
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
		}
		create("standalone") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("release")
			applicationIdSuffix = ".web"
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro")
			ndk.debugSymbolLevel = "FULL"
		}
	}

	sourceSets.getByName("debug").manifest.srcFile("../TMessagesProj/config/release/AndroidManifest.xml")
	sourceSets.getByName("standalone").manifest.srcFile("../TMessagesProj/config/release/AndroidManifest.xml")

	flavorDimensions += "minApi"

	productFlavors {
		create("afat") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			extra["abiVersionCode"] = 9
		}
	}

	defaultConfig {
		versionCode = APP_VERSION_CODE.toInt()
		minSdk = 26
		targetSdk = 36
		versionName = APP_VERSION_NAME
		ndkVersion = "21.4.7075529"

		multiDexEnabled = true
		vectorDrawables.generatedDensities?.apply {
			clear()
			addAll(listOf("mdpi", "hdpi", "xhdpi", "xxhdpi"))
		}

		externalNativeBuild {
			cmake {
				version = "3.10.2"
				arguments += listOf("-DANDROID_STL=c++_static", "-DANDROID_PLATFORM=android-21")
			}
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures {
		buildConfig = true
	}
	namespace = "org.telegram.messenger.test"

	@Suppress("DEPRECATION")
	applicationVariants.all {
		outputs.all {
			(this as BaseVariantOutputImpl).outputFileName = "app.apk"
		}
	}

	@Suppress("DEPRECATION")
	variantFilter {
		val names = flavors.map { it.name }
		if (buildType?.name != "release" && !names.contains("afat")) {
			ignore = true
		}
	}
}
