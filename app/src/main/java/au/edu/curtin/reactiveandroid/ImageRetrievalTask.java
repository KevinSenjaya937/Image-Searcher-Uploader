package au.edu.curtin.reactiveandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ImageRetrievalTask implements Callable<ArrayList<Bitmap>> {
    private Activity uiActivity;
    private String data;
    private RemoteUtilities remoteUtilities;
    public ImageRetrievalTask(Activity uiActivity) {
        remoteUtilities = RemoteUtilities.getInstance(uiActivity);
        this.uiActivity=uiActivity;
        this.data = null;
    }
    @Override
    public ArrayList<Bitmap> call() throws Exception {
        ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();
        Bitmap image = null;
        // Change this to a list of strings
        ArrayList<String> endpoint = getEndpoint(this.data);
        if(endpoint.isEmpty()){
            uiActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(uiActivity,"No image found",Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            for (String url : endpoint) {
                image = getImageFromUrl(url);
                imageList.add(image);
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }

        }
        return imageList;
    }

    // Make it return a list of strings. Strings will be the urls of the images. Take the smaller url.
    private ArrayList<String> getEndpoint(String data){
        String imageUrl = null;
        ArrayList<String> imageUrls = new ArrayList<String>();
        try {
            JSONObject jBase = new JSONObject(data);
            JSONArray jHits = jBase.getJSONArray("hits");
            if(jHits.length()>0){
                // Make a for loop to get all the urls
                for (int i=0; i < jHits.length(); i++) {
                    JSONObject jHitsItem = jHits.getJSONObject(i);
                    imageUrls.add(jHitsItem.getString("previewURL"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

    private Bitmap getImageFromUrl(String imageUrl){
        Bitmap image = null;
        Uri.Builder url = Uri.parse(imageUrl).buildUpon();
        String urlString = url.build().toString();
        HttpURLConnection connection = remoteUtilities.openConnection(urlString);
        if(connection!=null){
            if(remoteUtilities.isConnectionOkay(connection)){
                image = getBitmapFromConnection(connection);
                connection.disconnect();
            }
        }
        return image;
    }

    public Bitmap getBitmapFromConnection(HttpURLConnection conn){
        Bitmap data = null;
        try {
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = getByteArrayFromInputStream(inputStream);
            data = BitmapFactory.decodeByteArray(byteData,0,byteData.length);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public void setData(String data) {
        this.data = data;
    }
}
