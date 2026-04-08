plugins {
	id("com.android.application")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
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
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.core.v1160)
	coreLibraryDesugaring(libs.desugar.jdk.libs)
	implementation(files("../TMessagesProj/libs/libgsaverification-client.aar"))

	implementation(libs.appcenter.distribute)
	implementation(libs.appcenter.crashes)
	implementation(libs.appcenter.analytics)

	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.crashlytics)
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
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
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
		create("HA_private") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("debug")
			applicationIdSuffix = ".beta"
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
		}
		create("HA_public") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("debug")
			applicationIdSuffix = ".beta"
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
		}
		create("HA_hardcore") {
			isDebuggable = false
			isJniDebuggable = false
			signingConfig = signingConfigs.getByName("debug")
			applicationIdSuffix = ".beta"
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
		}
	}

	sourceSets.getByName("HA_private").manifest.srcFile("../TMessagesProj/config/debug/AndroidManifest.xml")
	sourceSets.getByName("HA_public").manifest.srcFile("../TMessagesProj/config/debug/AndroidManifest.xml")
	sourceSets.getByName("HA_hardcore").manifest.srcFile("../TMessagesProj/config/debug/AndroidManifest.xml")

	flavorDimensions += "minApi"

	productFlavors {
		create("bundleAfat") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			extra["abiVersionCode"] = 1
		}
		create("bundleAfat_SDK23") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			minSdk = 23
			extra["abiVersionCode"] = 2
		}
		create("afat") {
			ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
			extra["abiVersionCode"] = 9
		}
	}

	defaultConfig {
		versionCode = APP_VERSION_CODE.toInt()
		minSdk = 21
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
