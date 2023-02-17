import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ObjectModel(private val context: Context) {

    private var vertices: FloatArray? = null
    private var normals: FloatArray? = null
    private var texCoords: FloatArray? = null
    private var vertexBuffer: FloatBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var texCoordBuffer: FloatBuffer? = null
    var width: Int = 0
    var height: Int = 0

    fun loadFromObj(objReader: BufferedReader) {
        val vertexList = ArrayList<Float>()
        val normalList = ArrayList<Float>()
        val texCoordList = ArrayList<Float>()
        val indexList = ArrayList<Int>()

        objReader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(" ")

                when (parts[0]) {
                    "v" -> for (i in 1..3) vertexList.add(parts[i].toFloat())
                    "vn" -> for (i in 1..3) normalList.add(parts[i].toFloat())
                    "vt" -> for (i in 1..2) texCoordList.add(parts[i].toFloat())
                    "f" -> parts.forEachIndexed { i, part ->
                        if (i > 0) {
                            val indices = part.split("/").map { it.toInt() - 1 }
                            indexList.addAll(indices)
                        }
                    }
                }
            }
        }

        vertices = FloatArray(indexList.size / 3 * 3)
        normals = FloatArray(indexList.size / 3 * 3)
        texCoords = FloatArray(indexList.size / 3 * 2)

        for (i in indexList.indices step 3) {
            val vi = indexList[i] * 3
            val ni = indexList[i + 2] * 3
            val ti = indexList[i + 1] * 2

            vertices!![i] = vertexList[vi]
            vertices!![i + 1] = vertexList[vi + 1]
            vertices!![i + 2] = vertexList[vi + 2]

            normals!![i] = normalList[ni]
            normals!![i + 1] = normalList[ni + 1]
            normals!![i + 2] = normalList[ni + 2]

            texCoords!![i / 3 * 2] = texCoordList[ti]
            texCoords!![i / 3 * 2 + 1] = texCoordList[ti + 1]
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices!!.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer!!.put(vertices)
        vertexBuffer!!.position(0)

        normalBuffer = ByteBuffer.allocateDirect(normals!!.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        normalBuffer!!.put(normals)
        normalBuffer!!.position(0)

        texCoordBuffer =
            ByteBuffer.allocateDirect(texCoords!!.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        texCoordBuffer!!.put(texCoords)
        texCoordBuffer!!.position(0)
    }

    var vertexBufferId: Int = 0
    var normalBufferId: Int = 0
    var texCoordBufferId: Int = 0

    fun initBuffers() {
        val buffers = IntArray(3)
        GLES20.glGenBuffers(3, buffers, 0)
        vertexBufferId = buffers[0]
        normalBufferId = buffers[1]
        texCoordBufferId = buffers[2]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)

        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertices!!.size * FLOAT_SIZE_BYTES,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            normals!!.size * FLOAT_SIZE_BYTES,
            normalBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBufferId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            texCoords!!.size * FLOAT_SIZE_BYTES,
            texCoordBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    var vertexBufferIds: IntArray = intArrayOf()

    companion object {
        const val COORDS_PER_VERTEX = 3
        const val NORMALS_PER_VERTEX = 3
        const val TEX_COORDS_PER_VERTEX = 2
        const val FLOAT_SIZE_BYTES = 4
        const val POSITION_HANDLE = 0
        const val TEX_COORD_HANDLE = 1
        const val NORMAL_HANDLE = 2
    }

    fun render(mvpMatrix: FloatArray, programHandle: Int) {
        val vertexStride =
            (COORDS_PER_VERTEX + TEX_COORDS_PER_VERTEX + NORMALS_PER_VERTEX) * FLOAT_SIZE_BYTES
        // Bind the vertex buffer object (VBO) to the OpenGL context
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferIds[0])

        // Associate the position data with the shader program attribute
        GLES20.glVertexAttribPointer(
            POSITION_HANDLE,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            0
        )
        GLES20.glEnableVertexAttribArray(POSITION_HANDLE)

        // Associate the normal data with the shader program attribute
        GLES20.glVertexAttribPointer(
            NORMAL_HANDLE,
            NORMALS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            COORDS_PER_VERTEX * FLOAT_SIZE_BYTES
        )
        GLES20.glEnableVertexAttribArray(NORMAL_HANDLE)

        // Associate the texture coordinate data with the shader program attribute
        GLES20.glVertexAttribPointer(
            TEX_COORD_HANDLE,
            TEX_COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            (COORDS_PER_VERTEX + NORMALS_PER_VERTEX) * FLOAT_SIZE_BYTES
        )
        GLES20.glEnableVertexAttribArray(TEX_COORD_HANDLE)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        // Apply the model-view-projection matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the object using the current shader program and VBO data
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices!!.size)

        // Unbind the VBO from the OpenGL context
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

}
