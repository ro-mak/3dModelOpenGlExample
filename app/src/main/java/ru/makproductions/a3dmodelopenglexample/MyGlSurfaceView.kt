import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import ru.makproductions.a3dmodelopenglexample.R

class MyGlSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGlRenderer

    init {
        // Set the custom pixel format for the surface holder
        holder.setFormat(PixelFormat.RGBA_8888)

        // Set the OpenGL context version and client version
        setEGLContextClientVersion(3)

        // Create the OpenGL renderer and set it to this view
        renderer = MyGlRenderer(context, R.raw.test_object)
        setRenderer(renderer)

        // Set the render mode to only render when there are updates
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}