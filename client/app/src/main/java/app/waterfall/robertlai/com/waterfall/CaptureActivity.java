package app.waterfall.robertlai.com.waterfall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;


public class CaptureActivity extends ActionBarActivity {
    //static final String URL = "http://localhost:3000/api";
    static final String URL = "http://waterfallapi.herokuapp.com/api";
    private static String logtag = "Waterfall";
    private static int TAKE_PICTURE = 1;
    private static int SCALED_WIDTH = 1161;
    private static int MAX_IMAGES = 10;
    private static int REFRESH_PERIOD = 5;
    private static Uri imageUri;
    ArrayDeque<ImageView> photos = new ArrayDeque<>();
    Timer timer;

    public void startTimer(int seconds){
        timer = new Timer();
        timer.scheduleAtFixedRate(new getPhotoTask(), 0, seconds*1000);
    }

    class getPhotoTask extends TimerTask {
        public void run() {
            getPhoto();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        cameraButton.setOnClickListener(cameraListener);

        getPhoto();
        startTimer(REFRESH_PERIOD);
    }

    private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View view) {
            takePhoto(view);
        }
    };

    private void takePhoto(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
        setIntent(null);
    }

    private void getPhoto() {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            BasicHttpParams hparams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(hparams, 10 * 1000);
            httpClient.setParams(hparams);
            HttpGet httpGet = new HttpGet(URL);
            HttpResponse response = httpClient.execute(httpGet);

            System.out.println(response.getStatusLine());

            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            int newHeight = (int) (bitmap.getHeight() * ((float) SCALED_WIDTH / bitmap.getWidth()));
            final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, newHeight, true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout imagesLayout = (LinearLayout) findViewById(R.id.linear_layout_images);
                    if (photos.size() >= MAX_IMAGES) {
                        imagesLayout.removeView(photos.getLast());
                        photos.removeLast();
                    }
                    ImageView image = new ImageView(CaptureActivity.this);
                    image.setImageBitmap(scaledBitmap);
                    image.setAdjustViewBounds(true);
                    photos.addFirst(image);
                    imagesLayout.addView(photos.getFirst());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postPhoto() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                FileInputStream fis=null;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg");
                byte[] buffer = new byte[(int) file.length()];
                try {
                    fis = new FileInputStream(file);
                    fis.read(buffer);
                    fis.close();

                    /*for (int i = 0; i < buffer.length; i++) {
                        System.out.print((char) buffer[i]);
                    }*/
                }
                catch(Exception e){
                    System.out.println("Failed");
                }

                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    BasicHttpParams hparams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(hparams, 10 * 1000);
                    httpClient.setParams(hparams);
                    HttpPost httpPost = new HttpPost(URL);
                    httpPost.setEntity(new ByteArrayEntity(buffer));
                    HttpResponse response = httpClient.execute(httpPost);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);
            postPhoto();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasCamera() {
        //return hasSystemFeature(PackageManager.FEATURE_CAMERA);
        return true;
    }
}
