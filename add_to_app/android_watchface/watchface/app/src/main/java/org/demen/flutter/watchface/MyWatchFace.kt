package org.demen.flutter.watchface

import android.os.Build
import android.support.wearable.watchface.WatchFaceService
import android.view.SurfaceHolder
import android.view.ViewConfiguration
import android.view.WindowInsets
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.renderer.FlutterRenderer
import kotlin.math.max

class MyWatchFace : WatchFaceService() {
    private lateinit var flutterViewEngine: FlutterViewEngine

    override fun onCreateEngine(): Engine {
        // TODO: create a multi-engine version after
        // https://github.com/flutter/flutter/issues/72009 is built.
        val engine = FlutterEngine(applicationContext)
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                "showClock"
            )
        )

        flutterViewEngine = FlutterViewEngine(engine)
        return Engine()
    }

    inner class Engine : WatchFaceService.Engine() {
        private val viewportMetrics: FlutterRenderer.ViewportMetrics =
            FlutterRenderer.ViewportMetrics()

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            flutterViewEngine.attachHolder(holder!!)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            flutterViewEngine.detachHolder()
            flutterViewEngine.attachHolder(holder!!)
            viewportMetrics.width = width
            viewportMetrics.height = height
            viewportMetrics.devicePixelRatio = resources.displayMetrics.density
            viewportMetrics.physicalTouchSlop =
                ViewConfiguration.get(displayContext).scaledTouchSlop
            flutterViewEngine.engine.renderer.setViewportMetrics(viewportMetrics)
        }

        override fun onApplyWindowInsets(insets: WindowInsets?) {
            super.onApplyWindowInsets(insets)

            // getSystemGestureInsets() was introduced in API 29 and immediately deprecated in 30.

            // getSystemGestureInsets() was introduced in API 29 and immediately deprecated in 30.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                val systemGestureInsets = insets!!.systemGestureInsets
                viewportMetrics.systemGestureInsetTop = systemGestureInsets.top
                viewportMetrics.systemGestureInsetRight = systemGestureInsets.right
                viewportMetrics.systemGestureInsetBottom = systemGestureInsets.bottom
                viewportMetrics.systemGestureInsetLeft = systemGestureInsets.left
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val mask = 0

                val uiInsets = insets!!.getInsets(mask)
                viewportMetrics.viewPaddingTop = uiInsets.top
                viewportMetrics.viewPaddingRight = uiInsets.right
                viewportMetrics.viewPaddingBottom = uiInsets.bottom
                viewportMetrics.viewPaddingLeft = uiInsets.left
                val imeInsets = insets.getInsets(WindowInsets.Type.ime())
                viewportMetrics.viewInsetTop = imeInsets.top
                viewportMetrics.viewInsetRight = imeInsets.right
                viewportMetrics.viewInsetBottom =
                    imeInsets.bottom // Typically, only bottom is non-zero
                viewportMetrics.viewInsetLeft = imeInsets.left
                val systemGestureInsets = insets.getInsets(WindowInsets.Type.systemGestures())
                viewportMetrics.systemGestureInsetTop = systemGestureInsets.top
                viewportMetrics.systemGestureInsetRight = systemGestureInsets.right
                viewportMetrics.systemGestureInsetBottom = systemGestureInsets.bottom
                viewportMetrics.systemGestureInsetLeft = systemGestureInsets.left

                // TODO(garyq): Expose the full rects of the display cutout.

                // Take the max of the display cutout insets and existing padding to merge them
                val cutout = insets.displayCutout
                if (cutout != null) {
                    val waterfallInsets = cutout.waterfallInsets
                    viewportMetrics.viewPaddingTop = max(
                        max(viewportMetrics.viewPaddingTop, waterfallInsets.top),
                        cutout.safeInsetTop
                    )
                    viewportMetrics.viewPaddingRight = max(
                        max(viewportMetrics.viewPaddingRight, waterfallInsets.right),
                        cutout.safeInsetRight
                    )
                    viewportMetrics.viewPaddingBottom = max(
                        max(viewportMetrics.viewPaddingBottom, waterfallInsets.bottom),
                        cutout.safeInsetBottom
                    )
                    viewportMetrics.viewPaddingLeft = max(
                        max(viewportMetrics.viewPaddingLeft, waterfallInsets.left),
                        cutout.safeInsetLeft
                    )
                }
            } else {
                // Status bar (top), navigation bar (bottom) and left/right system insets should
                // partially obscure the content (padding).
                viewportMetrics.viewPaddingTop = 0
                viewportMetrics.viewPaddingRight = insets!!.systemWindowInsetRight
                viewportMetrics.viewPaddingBottom =  0
                viewportMetrics.viewPaddingLeft = insets.systemWindowInsetLeft

                // Bottom system inset (keyboard) should adjust scrollable bottom edge (inset).
                viewportMetrics.viewInsetTop = 0
                viewportMetrics.viewInsetRight = 0
                viewportMetrics.viewInsetBottom = 0
                viewportMetrics.viewInsetLeft = 0
            }
            flutterViewEngine.engine.renderer.setViewportMetrics(viewportMetrics)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            flutterViewEngine.detachHolder()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            flutterViewEngine.onVisibilityChanged(visible)
        }
    }
}