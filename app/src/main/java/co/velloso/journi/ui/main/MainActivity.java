package co.velloso.journi.ui.main;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.velloso.journi.R;

public class MainActivity extends Activity {

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MapGLSurfaceView(this, loadPolygonsCoordinates());
        setContentView(mGLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }

    private List<float[]> loadPolygonsCoordinates() {
        // Get json form raw resource
        InputStream inputStream = getResources().openRawResource(R.raw.countries_mock);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Parse the data into JSONObject to get original data in form of json.
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray featuresJSONArray = jObject.getJSONArray("features");

            // Map loop level
            List<float[]> polygonsCoordList = new ArrayList<>();
            for (int i = 0; i < featuresJSONArray.length(); i++) {
                JSONArray coordinatesJSONArray = featuresJSONArray.getJSONObject(i)
                        .getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);

                // Country loop level
                float polygonCoords[] = new float[coordinatesJSONArray.length() * 3];
                for (int j = 0; j < coordinatesJSONArray.length(); j++) {
                    JSONArray coordinateJSONArray = coordinatesJSONArray.getJSONArray(j);

                    // Set coordinates x, y, z
                    polygonCoords[j * 3] = (float) ((double) coordinateJSONArray.get(0));
                    polygonCoords[j * 3 + 1] = (float) ((double) coordinateJSONArray.get(1));
                    polygonCoords[j * 3 + 2] = 0.0f;
                }
                polygonsCoordList.add(polygonCoords);
            }

            return polygonsCoordList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}