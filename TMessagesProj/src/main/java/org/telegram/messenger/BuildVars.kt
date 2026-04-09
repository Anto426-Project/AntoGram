/*
 * This is the source code of AntoGram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.messenger

import android.content.Context
import android.os.Build
import com.android.billingclient.api.ProductDetails

object BuildVars {

    @JvmField
    var DEBUG_VERSION = envBoolean("ANTOGRAM_DEBUG_VERSION", "DEBUG_VERSION", BuildConfig.DEBUG_VERSION)

    @JvmField
    var LOGS_ENABLED = envBoolean("ANTOGRAM_LOGS_ENABLED", "LOGS_ENABLED", BuildConfig.DEBUG_VERSION)

    @JvmField
    var DEBUG_PRIVATE_VERSION = envBoolean("ANTOGRAM_DEBUG_PRIVATE_VERSION", "DEBUG_PRIVATE_VERSION", BuildConfig.DEBUG_PRIVATE_VERSION)

    @JvmField
    var USE_CLOUD_STRINGS = envBoolean("ANTOGRAM_USE_CLOUD_STRINGS", "USE_CLOUD_STRINGS", true)

    @JvmField
    var CHECK_UPDATES = envBoolean("ANTOGRAM_CHECK_UPDATES", "CHECK_UPDATES", true)

    @JvmField
    var NO_SCOPED_STORAGE = Build.VERSION.SDK_INT <= 29

    @JvmField
    var BUILD_VERSION_STRING = envString("ANTOGRAM_BUILD_VERSION_STRING", "BUILD_VERSION_STRING", BuildConfig.BUILD_VERSION_STRING)

    @JvmField
    var APP_ID = envInt("ANTOGRAM_APP_ID", "APP_ID", BuildConfig.BUILDVARS_APP_ID)

    @JvmField
    var APP_HASH = envString("ANTOGRAM_APP_HASH", "APP_HASH", BuildConfig.BUILDVARS_APP_HASH)

    @JvmField
    var PLAYSTORE_APP_URL = envString("ANTOGRAM_PLAYSTORE_APP_URL", "PLAYSTORE_APP_URL", BuildConfig.BUILDVARS_PLAYSTORE_APP_URL)

    @JvmField
    var GOOGLE_AUTH_CLIENT_ID = envString("ANTOGRAM_GOOGLE_AUTH_CLIENT_ID", "GOOGLE_AUTH_CLIENT_ID", BuildConfig.BUILDVARS_GOOGLE_AUTH_CLIENT_ID)

    @JvmField
    var IS_BILLING_UNAVAILABLE = envBoolean("ANTOGRAM_IS_BILLING_UNAVAILABLE", "IS_BILLING_UNAVAILABLE", BuildConfig.BUILDVARS_IS_BILLING_UNAVAILABLE)

    @JvmField
    var SUPPORTS_PASSKEYS = envBoolean("ANTOGRAM_SUPPORTS_PASSKEYS", "SUPPORTS_PASSKEYS", BuildConfig.BUILDVARS_SUPPORTS_PASSKEYS)

    private var betaApp: Boolean? = null

    init {
        if (ApplicationLoader.applicationContext != null) {
            val sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE)
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION)
            if (LOGS_ENABLED) {
                val pastHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                    FileLog.fatal(exception, false)
                    pastHandler?.uncaughtException(thread, exception)
                }
            }
        }
    }

    @JvmStatic
    fun useInvoiceBilling(): Boolean {
        return BillingController.billingClientEmpty || DEBUG_VERSION && false || ApplicationLoader.isStandaloneBuild() || isBetaApp() && false || hasDirectCurrency()
    }

    private fun hasDirectCurrency(): Boolean {
        if (!BillingController.getInstance().isReady || BillingController.PREMIUM_PRODUCT_DETAILS == null) {
            return false
        }
        for (offerDetails in BillingController.PREMIUM_PRODUCT_DETAILS.subscriptionOfferDetails ?: emptyList<ProductDetails.SubscriptionOfferDetails>()) {
            for (phase in offerDetails.pricingPhases.pricingPhaseList) {
                for (cur in MessagesController.getInstance(UserConfig.selectedAccount).directPaymentsCurrency) {
                    if (phase.priceCurrencyCode == cur) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @JvmStatic
    fun isBetaApp(): Boolean {
        if (betaApp == null) {
            betaApp = ApplicationLoader.applicationContext != null && "org.telegram.messenger.beta" == ApplicationLoader.applicationContext.packageName
        }
        return betaApp == true
    }

    @JvmStatic
    fun getSmsHash(): String {
        return if (ApplicationLoader.isStandaloneBuild()) {
            "w0lkcmTZkKh"
        } else if (DEBUG_VERSION) {
            "O2P2z+/jBpJ"
        } else {
            "oLeq9AcOZkT"
        }
    }

    private fun envString(primaryKey: String, fallbackKey: String, defaultValue: String): String {
        return System.getenv(primaryKey)?.takeIf { it.isNotBlank() }
            ?: System.getenv(fallbackKey)?.takeIf { it.isNotBlank() }
            ?: defaultValue
    }

    private fun envBoolean(primaryKey: String, fallbackKey: String, defaultValue: Boolean): Boolean {
        val value = envString(primaryKey, fallbackKey, defaultValue.toString())
        return when (value.trim().lowercase()) {
            "1", "true", "yes", "on" -> true
            "0", "false", "no", "off" -> false
            else -> defaultValue
        }
    }

    private fun envInt(primaryKey: String, fallbackKey: String, defaultValue: Int): Int {
        return envString(primaryKey, fallbackKey, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }
}
