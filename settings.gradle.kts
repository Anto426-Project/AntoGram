pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
	plugins {
		id("com.android.application") version "9.1.0"
		id("com.android.library") version "9.1.0"
		id("com.google.gms.google-services") version "4.4.2"
		id("com.google.firebase.crashlytics") version "3.0.6"
		id("org.jetbrains.kotlin.android") version "2.2.10"
	}
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":TMessagesProj")
include(":TMessagesProj_App")
include(":TMessagesProj_AppHockeyApp")
include(":TMessagesProj_AppStandalone")
include(":TMessagesProj_AppTests")
