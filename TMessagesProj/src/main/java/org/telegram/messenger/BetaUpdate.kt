package org.telegram.messenger

import androidx.annotation.Nullable

class BetaUpdate(version: String, versionCode: Int, @Nullable changelog: String?) {
    @JvmField
    val version: String = version

    @JvmField
    val versionCode: Int = versionCode

    @JvmField
    @Nullable
    val changelog: String? = changelog

    fun higherThan(update: BetaUpdate?): Boolean {
        return update == null ||
            SharedConfig.versionBiggerOrEqual(version, update.version) && versionCode > update.versionCode
    }
}
