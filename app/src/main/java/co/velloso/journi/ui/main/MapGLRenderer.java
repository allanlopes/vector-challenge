package co.velloso.journi.ui.main;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MapGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MapGLRenderer";
    private final List<float[]> mPolygonsCoordinates;
    private List<Polygon> mPolygons = new ArrayList<>();

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mMapPositionMatrix = new float[16];

    private float mAngle;
    private volatile float mZoom = 0.1f;
    private volatile float mTranslateX;
    private volatile float mTranslateY;

    public MapGLRenderer(List<float[]> polygonsCoordinates) {
        super();
        mPolygonsCoordinates = polygonsCoordinates;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);

        // Start the position matrix of map as a identity matrix
        Matrix.setIdentityM(mMapPositionMatrix, 0);

        for (float[] polygonCoords2 : mPolygonsCoordinates) {
            mPolygons.add(new Polygon(polygonCoords2));
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Rotate the map
        Matrix.setRotateM(mMapPositionMatrix, 0, mAngle, 0, 0, 1.0f);

        // Zoom the map
        Matrix.scaleM(mMapPositionMatrix, 0, mZoom, mZoom, 0);

        // Translate the map
        Matrix.translateM(mMapPositionMatrix, 0, mTranslateX, mTranslateY, 0);

        // Combine the position matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mMapPositionMatrix, 0);

        // Draw polygons
        for (Polygon polygon : mPolygons) {
            polygon.draw(scratch);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MapGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setZoom(float zoom) {
        mZoom = zoom;
    }

    public float getZoom() {
        return mZoom;
    }

    public float getTranslateX() {
        return mTranslateX;
    }

    public void setTranslateX(float translateX) {
        mTranslateX = translateX;
    }

    public float getTranslateY() {
        return mTranslateY;
    }

    public void setTranslateY(float translateY) {
        mTranslateY = translateY;
    }
}