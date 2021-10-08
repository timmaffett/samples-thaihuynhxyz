package org.demen.flutter.watchface

import android.support.wearable.watchface.WatchFaceService
import android.view.SurfaceHolder
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor

class MyWatchFace : WatchFaceService() {
    private lateinit var flutterViewEngine: FlutterViewEngine

    override fun onCreateEngine(): Engine {
        // TODO: create a multi-engine version after
        // https://github.com/flutter/flutter/issues/72009 is built.
        val engine = FlutterEngine(applicationContext)
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                "showCell"
            )
        )

        flutterViewEngine = FlutterViewEngine(engine)
        // The activity and FlutterView have different lifecycles.
        // Attach the activity right away but only start rendering when the
        // view is also scrolled into the screen.
        flutterViewEngine.attachToService(this)

        return Engine()
    }

    inner class Engine : WatchFaceService.Engine() {
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            flutterViewEngine.attachHolder(holder!!)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            flutterViewEngine.detachHolder()
        }

        override fun onDestroy() {
            super.onDestroy()
            flutterViewEngine.detachService()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            flutterViewEngine.onVisibilityChanged(visible)
        }
    }
}