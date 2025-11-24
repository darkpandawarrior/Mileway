package com.miletracker.debug

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.api.WormaCeptorInterceptor
import okhttp3.Interceptor

/**
 * Debug implementation — provides live HTTP inspection via WormaCeptor.
 *
 * Initialise once from Application.onCreate(). All OkHttp clients should add
 * [buildInterceptor()] as a network interceptor so request/response bodies are captured.
 *
 * Use [getLaunchIntent] to open the WormaCeptor traffic viewer from the Debug Menu.
 */
object WormaCeptorHelper {

    fun init(context: Context) {
        WormaCeptorApi.init(
            context = context,
            logCrashes = true,
            leakNotifications = true,
            features = Feature.ALL,
        )
    }

    fun buildInterceptor(): Interceptor =
        WormaCeptorInterceptor()
            .maxContentLength(250_000L)
            .retainDataFor(WormaCeptorInterceptor.Period.ONE_WEEK)

    fun getLaunchIntent(context: Context): Intent? =
        WormaCeptorApi.getLaunchIntent(context)
}
