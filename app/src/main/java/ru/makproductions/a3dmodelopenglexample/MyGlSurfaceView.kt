import android.content.Context
import android.opengl.GLSurfaceView
import ru.makproductions.a3dmodelopenglexample.R

class MyGlSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGlRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Create a renderer and set it as the glSurfaceView's renderer
        renderer = MyGlRenderer(context, R.raw.test_object)
        setRenderer(renderer)

        // Render the view continuously
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}