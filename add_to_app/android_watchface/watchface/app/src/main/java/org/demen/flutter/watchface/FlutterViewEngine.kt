// Copyright 2019 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.demen.flutter.watchface

import android.util.Log
import android.view.SurfaceHolder
import androidx.lifecycle.LifecycleObserver
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine

/**
 *  This is an application-specific wrapper class that exists to expose the intersection of an
 *  application's active activity and an application's visible view to a [FlutterEngine] for
 *  rendering.
 *
 *  Omitted features from the [io.flutter.embedding.android.FlutterActivity] include:
 *   * **State restoration**. If you're integrating at the view level, you should handle activity
 *      state restoration yourself.
 *   * **Engine creations**. At this level of granularity, you must make an engine and attach.
 *      and all engine features like initial route etc must be configured on the engine yourself.
 *   * **Splash screens**. You must implement it yourself. Read from
 *     `addOnFirstFrameRenderedListener` as needed.
 *   * **Transparency, surface/texture**. These are just [FlutterView] level APIs. Set them on the
 *      [FlutterView] directly.
 *   * **Intents**. This doesn't do any translation of intents into actions in the [FlutterEngine].
 *      you must do them yourself.
 *   * **Back buttons**. You must decide whether to send it to Flutter via
 *      [FlutterEngine.getNavigationChannel.popRoute()], or consume it natively. Though that
 *      decision may be difficult due to https://github.com/flutter/flutter/issues/67011.
 *   * **Low memory signals**. You're strongly encouraged to pass the low memory signals (such
 *      as from the host `Activity`'s `onTrimMemory` callbacks) to the [FlutterEngine] to let
 *      Flutter and the Dart VM cull its own memory usage.
 *
 * Your own [FlutterView] integrating application may need a similar wrapper but you must decide on
 * what the appropriate intersection between the [FlutterView], the [FlutterEngine] and your
 * `Activity` should be for your own application.
 */
class FlutterViewEngine(val engine: FlutterEngine) : LifecycleObserver {
    private var holder: SurfaceHolder? = null

    /**
     * This is the intersection of an available service and of a visible holder. This is
     * where Flutter would start rendering.
     */
    private fun hookHolder() {
        // Assert state.
        holder!!.let { holder ->
            Log.d("FlutterViewEngine", "hookServiceAndHolder")

            engine.renderer.createSurfaceTexture()
            engine.renderer.startRenderingToSurface(holder.surface,true)
        }
    }

    /**
     * Lost the intersection of either an available activity or a visible
     * [FlutterView].
     */
    private fun unhookHolder() {
        Log.d("FlutterViewEngine", "unhookServiceAndHolder")

        // Set Flutter's application state to detached.
        engine.lifecycleChannel.appIsDetached()

        // Detach rendering pipeline.
        engine.renderer.stopRenderingToSurface()
    }

    /**
     * Signal that a [FlutterView] instance is created and attached to a visible Android view
     * hierarchy.
     *
     * If an `Activity` was also previously provided, this puts Flutter into the rendering state
     * for this [FlutterView]. This also connects this wrapper class to listen to the `Activity`'s
     * lifecycle to pause rendering when the activity is put into the background while the
     * view is still attached to the view hierarchy.
     */
    fun attachHolder(holder: SurfaceHolder) {
        Log.d("FlutterViewEngine", "attachHolder: holder=$holder")
        this.holder = holder
        hookHolder()
    }

    /**
     * Signal that the attached [FlutterView] instance destroyed or no longer attached to a visible
     * Android view hierarchy.
     *
     * If an `Activity` was attached, this stops Flutter from rendering. It also makes this wrapper
     * class stop listening to the `Activity`'s lifecycle since it's no longer rendering.
     */
    fun detachHolder() {
        unhookHolder()
        holder = null
    }

    fun onVisibilityChanged(visible: Boolean) {
        Log.d("FlutterViewEngine", "onVisibilityChanged: visible=$visible")
        if (visible) {
            engine.lifecycleChannel.appIsResumed()
        } else {
            engine.lifecycleChannel.appIsPaused()
        }
    }
}
