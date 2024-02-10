package com.benkkstudio.beeconsent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class BeeConsent private constructor(
    val activity: Activity,
    private val debugMode: Boolean,
    private val enableLogging: Boolean,
    private val beeConsentCallback: BeeConsentCallback?
) {

    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

    companion object {
        private const val isConsentPref = "isConsentPref"

        fun isConsent(activity: Activity): Boolean {
            return activity.prefsHelper.get(isConsentPref, false)
        }

        fun isConsent(context: Context): Boolean {
            return context.prefsHelper.get(isConsentPref, false)
        }
    }

    private fun isConsentRequested(): Boolean {
        return activity.prefsHelper.contains(isConsentPref)
    }

    init {
        if (isConsentRequested()) {
            beeConsentCallback?.onRequested()
        } else request(activity)
    }


    private fun request(activity: Activity) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .setForceTesting(true)
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)

        if (debugMode) {
            params.setConsentDebugSettings(debugSettings)
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            params.build(),
            {
                if (enableLogging) logging("consentInformation.isConsentFormAvailable : " + consentInformation.isConsentFormAvailable)
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity)
                } else {
                    activity.prefsHelper.set(isConsentPref, false)
                    beeConsentCallback?.onRequested()
                }

            },
            { requestConsentError ->
                activity.prefsHelper.set(isConsentPref, false)
                beeConsentCallback?.onRequested()
                if (enableLogging) logging(
                    String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            })

    }

    private fun loadForm(activity: Activity) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm: ConsentForm ->
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) { fromError ->
                        if (fromError == null) {
                            activity.prefsHelper.set(isConsentPref, true)
                            beeConsentCallback?.onRequested()
                        } else {
                            activity.prefsHelper.set(isConsentPref, false)
                            beeConsentCallback?.onRequested()
                        }
                    }
                }
            }
        ) { formError: FormError? ->
            activity.prefsHelper.set(isConsentPref, false)
            beeConsentCallback?.onRequested()
            if (enableLogging) logging(
                String.format(
                    "%s: %s",
                    formError?.errorCode,
                    formError?.message
                )
            )
        }

    }

    private fun logging(any: Any) {
        if (debugMode) {
            Log.e("ABENK : ", any.toString())
        }
    }

    class Builder(private val activity: Activity) {
        private var debugMode: Boolean = false
        private var enableLogging: Boolean = false
        private var beeConsentCallback: BeeConsentCallback? = null
        fun enableLogging(enableLogging: Boolean) = apply { this.enableLogging = enableLogging }
        fun debugMode(debugMode: Boolean) = apply { this.debugMode = debugMode }
        fun listener(beeConsentCallback: BeeConsentCallback) = apply { this.beeConsentCallback = beeConsentCallback }
        fun request() = BeeConsent(activity, debugMode, enableLogging, beeConsentCallback)
    }
}