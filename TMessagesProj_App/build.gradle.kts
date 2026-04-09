plugins {
	id("com.android.application")
	id("com.google.gms.google-services")
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
	implementation(files("../TMessagesProj/libs/libgsaverification-client.aar"))
}

android {
	compileSdk = 36

	defaultConfig {
		applicationId = APP_PACKAGE
	}

	sourceSets.getByName("main").jniLibs.directories.add("../TMessagesProj/jni/")

	lint {
		disable.addAll(listOf("MissingTranslation", "ExtraTranslation", "BlockedPrivateApi"))
		checkReleaseBuilds = false
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
		isCoreLibraryDesugaringEnabled = true
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
			applicationIdSuffix = ".beta"
			isMinifyEnabled = false
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
		}

		create("standalone") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("release")
			applicationIdSuffix = ".web"
			isMinifyEnabled = true
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro")
			ndk.debugSymbolLevel = "FULL"
		}

		getByName("release") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("release")
			isMinifyEnabled = true
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro")
			ndk.debugSymbolLevel = "FULL"
		}
	}

	sourceSets.getByName("debug").manifest.srcFile("../TMessagesProj/config/debug/AndroidManifest.xml")
	sourceSets.getByName("standalone").manifest.srcFile("../TMessagesProj/config/release/AndroidManifest.xml")
	sourceSets.getByName("release").manifest.srcFile("../TMessagesProj/config/release/AndroidManifest.xml")

	flavorDimensions += "minApi"

	productFlavors {
		create("bundleAfat") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			extra["abiVersionCode"] = 1
			buildConfigField("boolean", "BUNDLE", "true")
		}
		create("bundleAfat_SDK23") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			minSdk = 23
			extra["abiVersionCode"] = 2
			buildConfigField("boolean", "BUNDLE", "true")
		}
		create("afat") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			extra["abiVersionCode"] = 9
			buildConfigField("boolean", "BUNDLE", "false")
		}
	}

	defaultConfig {
		versionCode = APP_VERSION_CODE.toInt()
		minSdk = 23
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
				arguments += listOf("-DANDROID_STL=c++_static", "-DANDROID_PLATFORM=android-23")
			}
		}
	}

	buildFeatures {
		buildConfig = true
	}
	namespace = "org.telegram.messenger.regular"

}

androidComponents {
	beforeVariants(selector().all()) { variantBuilder ->
		val flavorNames = variantBuilder.productFlavors.map { it.second }
		if (variantBuilder.buildType != "release" && "afat" !in flavorNames) {
			variantBuilder.enable = false
		}
	}

}
