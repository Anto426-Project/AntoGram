buildscript {
	repositories {
		google()
		mavenCentral()
		maven(url = "https://developer.huawei.com/repo/")
	}
	dependencies {
		classpath(libs.gradle)
		classpath(libs.agcp)
	}
}

repositories {
	google()
	mavenCentral()
}
