package app.waterfall.robertlai.com.waterfall;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    //static final String URL = "http://localhost:3000/api";
    final String URL = "http://waterfallapi.herokuapp.com/api";
    private static String logtag = "Waterfall";
    private static int TAKE_PICTURE = 1;
    private static int SCALED_WIDTH = 1161;
    private static int MAX_IMAGES = 10;
    private static int REFRESH_PERIOD = 5;
    private long currentLastFile;
    private Uri imageUri;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static final int PREFERENCE_MODE_PRIVATE = 0;
    ArrayDeque<ImageView> photos = new ArrayDeque<>();
    Timer timer;

    public void startTimer(int seconds) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new getPhotoTask(), 0, seconds * 1000);
    }

    class getPhotoTask extends TimerTask {
        public void run() {
            Log.e(logtag, "Last image: " + String.valueOf(currentLastFile));
            getPhoto();
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        cameraButton.setOnClickListener(cameraListener);

        sharedPref = getPreferences(PREFERENCE_MODE_PRIVATE);
        editor = sharedPref.edit();

        currentLastFile = sharedPref.getLong("last_image", -1);

        Log.e(logtag, String.valueOf(currentLastFile));

        //loadPhoto(41);
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

    private void loadPhoto(long imageNumber) {
        try {
            final Bitmap bitmap = BitmapFactory.decodeFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + imageNumber + ".jpg").getPath());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout imagesLayout = (LinearLayout) findViewById(R.id.linear_layout_images);
                    if (photos.size() >= MAX_IMAGES) {
                        imagesLayout.removeView(photos.getLast());
                        photos.removeLast();
                    }
                    ImageView image = new ImageView(MainActivity.this);
                    image.setImageBitmap(bitmap);
                    image.setAdjustViewBounds(true);
                    photos.addFirst(image);
                    Toast.makeText(getApplicationContext(), "no", Toast.LENGTH_LONG);
                    imagesLayout.addView(photos.getFirst());
                }
            });
        } catch (Exception e) {
            Log.e(logtag, e.toString());
        }
    }

    private void getPhoto() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    BasicHttpParams hparams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(hparams, 10 * 1000);
                    httpClient.setParams(hparams);
                    Log.e(logtag, URL + "?currentLastFile=" + currentLastFile);
                    HttpGet httpGet = new HttpGet(URL + "?currentLastFile=" + currentLastFile);
                    HttpResponse response = httpClient.execute(httpGet);

                    Log.e(logtag, response.getStatusLine().toString());

                    if (response.getStatusLine().getStatusCode() == 200) {
                        currentLastFile = Long.parseLong(response.getFirstHeader("fileName").getValue());

                        HttpEntity entity = response.getEntity();
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        int newHeight = (int) (bitmap.getHeight() * ((float) SCALED_WIDTH / bitmap.getWidth()));
                        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, newHeight, true);

                        OutputStream stream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + currentLastFile + ".jpg"));
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);

                        editor.putLong("last_image", currentLastFile);
                        editor.commit();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout imagesLayout = (LinearLayout) findViewById(R.id.linear_layout_images);
                                if (photos.size() >= MAX_IMAGES) {
                                    imagesLayout.removeView(photos.getLast());
                                    photos.removeLast();
                                }
                                ImageView image = new ImageView(MainActivity.this);
                                image.setImageBitmap(scaledBitmap);
                                image.setAdjustViewBounds(true);
                                photos.addFirst(image);
                                imagesLayout.addView(photos.getFirst());
                            }
                        });
                    } else {
                        Log.e(logtag, "Response not OK ");
                    }
                } catch (Exception e) {
                    Log.e(logtag, e.toString());
                }
                return;
            }
        });
        thread.start();
    }

    private void postPhoto() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fis = null;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg");
                byte[] buffer = new byte[(int) file.length()];
                try {
                    fis = new FileInputStream(file);
                    fis.read(buffer);
                    fis.close();

                    /*for (int i = 0; i < buffer.length; i++) {
                        System.out.print((char) buffer[i]);
                    }*/
                } catch (Exception e) {
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
                    if (response.getStatusLine().getStatusCode() != 200) {
                        Log.e(logtag, "Response not OK");
                    }
                } catch (Exception e) {
                    Log.e(logtag, e.toString());
                }
                return;
            }
        });
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            //getContentResolver().notifyChange(selectedImage, null);
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
