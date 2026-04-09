import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.plugin.compose")
}

val APP_VERSION_NAME: String by project

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
	implementation(libs.androidx.core)
	implementation(libs.androidx.palette)
	implementation(libs.androidx.exifinterface)
	implementation(libs.androidx.dynamicanimation)
	implementation(libs.androidx.sharetarget)
	implementation(libs.androidx.interpolator)
	implementation(libs.androidx.biometric)
	implementation(libs.androidx.camera.core)
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.lifecycle)
	implementation(libs.androidx.camera.view)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.runtime)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)

	implementation(libs.play.services.cast.framework)
	implementation(libs.androidx.mediarouter)
	implementation(libs.nanohttpd)

	compileOnly(libs.checker.qual)
	compileOnly(libs.checker.compat.qual.v255)
	implementation(libs.firebase.messaging)
	implementation(libs.firebase.config)
	implementation(libs.firebase.datatransport)
	implementation(libs.play.services.maps)
	implementation(libs.play.services.auth)
	implementation(libs.play.services.wearable)
	implementation(libs.play.services.location)
	implementation(libs.play.services.wallet)
	implementation(libs.mp4parser.isoparser)
	implementation(libs.stripe.android)
	implementation(libs.mlkit.language.id)
	implementation(libs.barcode.scanning.v1720)
	implementation(libs.text.recognition.v1600)
	implementation(libs.face.detection.v1615)
	implementation(libs.image.labeling.v1708)
	implementation(libs.billing)
	implementation(libs.gson)
	implementation(libs.guava.android)
	implementation(libs.jsr305)
	implementation(libs.aspectjrt)

	implementation(libs.play.services.mlkit.subject.segmentation)

	implementation(libs.androidx.credentials)
	implementation(libs.androidx.credentials.play.services.auth)
	implementation(libs.recaptcha)

	coreLibraryDesugaring(libs.desugar.jdk.libs)
	debugImplementation(libs.androidx.compose.ui.tooling)
}

val isWindows = DefaultNativePlatform.getCurrentOperatingSystem().isWindows().toString()

val envFileProps by lazy {
	val props = Properties()
	val envFile = rootProject.file(".env")
	if (envFile.exists()) {
		envFile.forEachLine { line ->
			val trimmed = line.trim()
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				return@forEachLine
			}
			val separatorIndex = trimmed.indexOf('=')
			if (separatorIndex <= 0) {
				return@forEachLine
			}
			val key = trimmed.substring(0, separatorIndex).trim()
			var value = trimmed.substring(separatorIndex + 1).trim()
			if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith('\'') && value.endsWith('\''))) {
				value = value.substring(1, value.length - 1)
			}
			props[key] = value
		}
	}
	props
}

fun getProps(propName: String): String {
	val propsFile = rootProject.file("local.properties")
	if (propsFile.exists()) {
		val props = Properties()
		FileInputStream(propsFile).use { props.load(it) }
		return props[propName]?.toString() ?: ""
	}
	return ""
}

fun getEnvOrProp(propName: String, defaultValue: String): String {
	return System.getenv(propName)?.takeIf { it.isNotBlank() }
		?: envFileProps.getProperty(propName)?.takeIf { it.isNotBlank() }
		?: getProps(propName).takeIf { it.isNotBlank() }
		?: defaultValue
}

fun asBuildConfigString(value: String): String {
	return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

fun getEnvOrPropInt(propName: String, defaultValue: Int): Int {
	return getEnvOrProp(propName, defaultValue.toString()).toIntOrNull() ?: defaultValue
}

fun getEnvOrPropBoolean(propName: String, defaultValue: Boolean): Boolean {
	return when (getEnvOrProp(propName, defaultValue.toString()).trim().lowercase()) {
		"1", "true", "yes", "on" -> true
		"0", "false", "no", "off" -> false
		else -> defaultValue
	}
}

val buildVarsAppId = getEnvOrPropInt("ANTOGRAM_APP_ID", 4)
val buildVarsAppHash = getEnvOrProp("ANTOGRAM_APP_HASH", "014b35b6184100b085b0d0572f9b5103")
val buildVarsPlaystoreUrl = getEnvOrProp("ANTOGRAM_PLAYSTORE_APP_URL", "https://play.google.com/store/apps/details?id=org.telegram.messenger")
val buildVarsGoogleAuthClientId = getEnvOrProp("ANTOGRAM_GOOGLE_AUTH_CLIENT_ID", "760348033671-81kmi3pi84p11ub8hp9a1funsv0rn2p9.apps.googleusercontent.com")
val buildVarsBillingUnavailable = getEnvOrPropBoolean("ANTOGRAM_IS_BILLING_UNAVAILABLE", false)
val buildVarsSupportsPasskeys = getEnvOrPropBoolean("ANTOGRAM_SUPPORTS_PASSKEYS", true)
val buildVarsAppIdField = buildVarsAppId.toString()
val buildVarsBillingUnavailableField = buildVarsBillingUnavailable.toString()
val buildVarsSupportsPasskeysField = buildVarsSupportsPasskeys.toString()

android {
	compileSdk = 36
	ndkVersion = "21.4.7075529"

	sourceSets.getByName("main").jniLibs.directories.add("./jni/")

	externalNativeBuild {
		cmake {
			path = file("jni/CMakeLists.txt")
		}
	}

	lint {
		disable.addAll(listOf("MissingTranslation", "ExtraTranslation", "BlockedPrivateApi"))
		targetSdk = 36
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
		isCoreLibraryDesugaringEnabled = true
	}

	kotlin {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)
		}
	}

	defaultConfig {
		minSdk = 21
		vectorDrawables.generatedDensities?.apply {
			clear()
			addAll(listOf("mdpi", "hdpi", "xhdpi", "xxhdpi"))
		}
		multiDexEnabled = true
		externalNativeBuild {
			cmake {
				version = "3.10.2"
				arguments += listOf("-DANDROID_STL=c++_static", "-DANDROID_PLATFORM=android-21")
			}
		}
	}

	buildTypes {
		getByName("debug") {
			isJniDebuggable = true
			isMinifyEnabled = false
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"\"")
			buildConfigField("String", "BETA_URL", "\"${getProps("BETA_PRIVATE_URL")}\"")
			buildConfigField("boolean", "DEBUG_VERSION", "true")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "true")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "0")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}

		create("HA_private") {
			isJniDebuggable = false
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"${getProps("APP_CENTER_HASH_PRIVATE")}\"")
			buildConfigField("String", "BETA_URL", "\"${getProps("BETA_PRIVATE_URL")}\"")
			buildConfigField("boolean", "DEBUG_VERSION", "true")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "true")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "1")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}

		create("HA_public") {
			isJniDebuggable = false
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"${getProps("APP_CENTER_HASH_PUBLIC")}\"")
			buildConfigField("String", "BETA_URL", "\"${getProps("BETA_PUBLIC_URL")}\"")
			buildConfigField("boolean", "DEBUG_VERSION", "true")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "false")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "4")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}

		create("HA_hardcore") {
			isJniDebuggable = false
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro", "../TMessagesProj/proguard-rules-beta.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"${getProps("APP_CENTER_HASH_HARDCORE")}\"")
			buildConfigField("String", "BETA_URL", "\"${getProps("BETA_HARDCORE_URL")}\"")
			buildConfigField("boolean", "DEBUG_VERSION", "true")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "true")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "5")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}

		create("standalone") {
			isJniDebuggable = false
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"\"")
			buildConfigField("String", "BETA_URL", "\"\"")
			buildConfigField("boolean", "DEBUG_VERSION", "false")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "false")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "6")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}

		getByName("release") {
			isJniDebuggable = false
			isMinifyEnabled = true
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../TMessagesProj/proguard-rules.pro")
			ndk.debugSymbolLevel = "FULL"
			buildConfigField("String", "BUILD_VERSION_STRING", "\"$APP_VERSION_NAME\"")
			buildConfigField("String", "APP_CENTER_HASH", "\"\"")
			buildConfigField("String", "BETA_URL", "\"\"")
			buildConfigField("boolean", "DEBUG_VERSION", "false")
			buildConfigField("boolean", "DEBUG_PRIVATE_VERSION", "false")
			buildConfigField("boolean", "BUNDLE", "false")
			buildConfigField("boolean", "BUILD_HOST_IS_WINDOWS", isWindows)
			buildConfigField("int", "VERSION_NUM", "7")
			buildConfigField("int", "BUILDVARS_APP_ID", buildVarsAppIdField)
			buildConfigField("String", "BUILDVARS_APP_HASH", asBuildConfigString(buildVarsAppHash))
			buildConfigField("String", "BUILDVARS_PLAYSTORE_APP_URL", asBuildConfigString(buildVarsPlaystoreUrl))
			buildConfigField("String", "BUILDVARS_GOOGLE_AUTH_CLIENT_ID", asBuildConfigString(buildVarsGoogleAuthClientId))
			buildConfigField("boolean", "BUILDVARS_IS_BILLING_UNAVAILABLE", buildVarsBillingUnavailableField)
			buildConfigField("boolean", "BUILDVARS_SUPPORTS_PASSKEYS", buildVarsSupportsPasskeysField)
		}
	}

	buildFeatures {
		buildConfig = true
		compose = true
	}

	namespace = "org.telegram.messenger"
	testOptions {
		targetSdk = 36
	}
}

val checkVisibility = tasks.register("checkVisibility") {
	doFirst {
		val isPrivateBuild = gradle.startParameter.taskNames.any {
			it.contains("HA_private") || it.contains("HA_hardcore") || it.contains("Debug") || it.contains("Release")
		}
		val isPublicAllowed = !project.hasProperty("IS_PRIVATE") || !project.property("IS_PRIVATE").toString().toBoolean()
		if (!isPrivateBuild && !isPublicAllowed) {
			throw GradleException("Building public version of private code!")
		}
	}
	doLast {
		if (gradle.startParameter.taskNames.any { it.contains("HA_public") }) {
			val privateBuild = file("${projectDir}_AppHockeyApp/afat/HA_private/Telegram-Beta.apk")
			if (privateBuild.exists()) {
				privateBuild.delete()
			}
		}
	}
}

tasks.named("preBuild") {
	dependsOn(checkVisibility)
}
