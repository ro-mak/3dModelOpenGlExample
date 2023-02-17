import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.microedition.khronos.opengles.GL10

class MyGlRenderer(private val context: Context, private val resourceId: Int) :
    GLSurfaceView.Renderer {
    private lateinit var objectModel: ObjectModel
    private var angle = 0f
    private var programHandle: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig??) {
        // Set the background color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Load the object model from the raw resource file
        val objInputStream = context.resources.openRawResource(resourceId)
        val objReader = BufferedReader(InputStreamReader(objInputStream))
        objectModel = ObjectModel(context)
        objectModel.loadFromObj(objReader)
        objInputStream.close()

        // Set up the buffers
        objectModel.initBuffers()
        // Compile shaders
        val vertexShaderCode = "uniform mat4 u_MVPMatrix;\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec3 a_Normal;\n" +
                "attribute vec2 a_TexCoordinate;\n" +
                "\n" +
                "varying vec3 v_Position;\n" +
                "varying vec3 v_Normal;\n" +
                "varying vec2 v_TexCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "    v_Position = vec3(u_MVPMatrix * a_Position);\n" +
                "    v_Normal = mat3(u_MVPMatrix) * a_Normal;\n" +
                "    v_TexCoordinate = a_TexCoordinate;\n" +
                "    gl_Position = u_MVPMatrix * a_Position;\n" +
                "}\n"
        val vertexShaderHandle: Int =
            loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShaderCode = "precision mediump float;\n" +
                "\n" +
                "uniform vec4 u_Color;\n" +
                "uniform sampler2D u_Texture;\n" +
                "\n" +
                "varying vec3 v_Position;\n" +
                "varying vec3 v_Normal;\n" +
                "varying vec2 v_TexCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "    vec3 lightDirection = normalize(vec3(0.0, 1.0, 0.0));\n" +
                "    float diffuse = max(dot(v_Normal, lightDirection), 0.1);\n" +
                "    vec4 diffuseColor = texture2D(u_Texture, v_TexCoordinate) * u_Color;\n" +
                "    gl_FragColor = vec4(diffuseColor.rgb * diffuse, diffuseColor.a);\n" +
                "}\n"
        val fragmentShaderHandle: Int =
            loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Create program object
        programHandle = GLES20.glCreateProgram()

        // Attach shaders to program
        GLES20.glAttachShader(programHandle, vertexShaderHandle)
        GLES20.glAttachShader(programHandle, fragmentShaderHandle)

        // Link program
        GLES20.glLinkProgram(programHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the viewport to cover the entire surface
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear the color buffer and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set the camera position
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f
        val centerX = 0.0f
        val centerY = 0.0f
        val centerZ = 0.0f
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)

        // Set the model matrix
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f)

        // Set the projection matrix
        val ratio = objectModel.width.toFloat() / objectModel.height.toFloat()
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f
        val projectionMatrix = FloatArray(16)
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)

        // Set the combined matrix
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Render the object
        objectModel.render(mvpMatrix, programHandle)

        // Update the rotation angle
        angle += 1.0f
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        // create a new shader object
        val shader = GLES20.glCreateShader(type)

        // compile the shader code
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // check for errors
        val status = intArrayOf(0)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Log.e("TAG", "Error compiling shader: $log")
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }
}