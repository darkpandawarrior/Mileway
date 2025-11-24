package com.miletracker.debug

import android.content.Context
import android.content.Intent
import okhttp3.Interceptor

/**
 * Release no-op of WormaCeptorHelper.
 *
 * The debug source set provides the real implementation backed by
 * `com.github.azikar24.WormaCeptor:api-impl-persistence` (debugImplementation only).
 * This object compiles in release builds without the WormaCeptor dependency on the classpath.
 */
object WormaCeptorHelper {

    fun init(context: Context) = Unit

    fun buildInterceptor(): Interceptor = Interceptor { chain -> chain.proceed(chain.request()) }

    fun getLaunchIntent(context: Context): Intent? = null
}
