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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    //static final String URL = "http://localhost:3000/api";
    static final String URL = "http://waterfallapi.herokuapp.com/api";
    static final int PREFERENCE_MODE_PRIVATE = 0;
    static final long ZERO_TIME = 1442289600000L;
    static final String LOGTAG = "Waterfall";
    static final int TAKE_PICTURE = 1;
    static final int SCALED_WIDTH = 1161;
    static final int MAX_IMAGES = 15;
    static final int REFRESH_PERIOD = 5;
    private Uri imageUri;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    ArrayDeque<ImageView> photos = new ArrayDeque<>();
    ArrayList<Long> files = new ArrayList<>();
    Timer timer;

    public void startTimer(int seconds) {
        Log.e(LOGTAG, "Starting timer...");
        Log.e(LOGTAG, "Period: " + seconds + " seconds");
        timer = new Timer();
        timer.scheduleAtFixedRate(new getPhotoTask(), 0, seconds * 1000);
    }

    class getPhotoTask extends TimerTask {
        public void run() {
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

        File waterfallDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Waterfall");
        waterfallDir.mkdirs();

        Log.e(LOGTAG, "Loading saved images...");

        Set<String> savedImages = sharedPref.getStringSet("saved_images", null);
        if (savedImages != null) {
            for (String image : savedImages) {
                files.add(Long.parseLong(image));
            }
        }

        Collections.sort(files);

        for (int i = files.size() - 1; i >= 0; i--){
            loadPhoto(files.get(i));
        }

        Log.e(LOGTAG, files.size() + " images loaded.");

        startTimer(REFRESH_PERIOD);
    }

    private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View view) {
            takePhoto(view);
        }
    };

    private Long getLastFile() {
        Long lastImage = ZERO_TIME;
        for (Long file : files) {
            if (file > lastImage) {
                lastImage = file;
            }
        }
        return lastImage;
    }

    private void savePrefs(){
        Set<String> fileSet = new HashSet<>();
        for (Long file : files) {
            fileSet.add(file.toString());
        }
        editor.putStringSet("saved_images", fileSet);
        editor.commit();
    }

    private void takePhoto(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
        setIntent(null);
    }

    private void loadPhoto(long imageNumber) {
        final long in = imageNumber;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + in + ".jpg");
        if (file.exists()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap bitmap = BitmapFactory.decodeFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + in + ".jpg").getPath());
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
                                imagesLayout.addView(photos.getFirst(), 0);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOGTAG, e.toString());
                    }
                }
            });
            thread.start();
        }
        else{
            files.remove(files.indexOf(in));
            savePrefs();
            Log.e(LOGTAG, "running");
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

                    Log.e(LOGTAG, "Newest image: " + getLastFile());
                    Log.e(LOGTAG, "GET: " + URL + "?lastFile=" + getLastFile());

                    HttpGet httpGet = new HttpGet(URL + "?lastFile=" + getLastFile());
                    HttpResponse response = httpClient.execute(httpGet);

                    Log.e(LOGTAG, "Response: " + response.getStatusLine().toString());

                    if (response.getStatusLine().getStatusCode() == 200) {
                        long newFile = Long.parseLong(response.getFirstHeader("fileName").getValue());

                        HttpEntity entity = response.getEntity();
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        int newHeight = (int) (bitmap.getHeight() * ((float) SCALED_WIDTH / bitmap.getWidth()));
                        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, newHeight, true);

                        Log.e(LOGTAG, "Saving image...");
                        OutputStream stream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + newFile + ".jpg"));
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                        Log.e(LOGTAG, "Saved.");

                        files.add(newFile);
                        savePrefs();

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
                                imagesLayout.addView(photos.getFirst(), 0);
                            }
                        });
                    } else {
                        Log.e(LOGTAG, "Server response not OK.");
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, e.toString());
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
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg").getPath());
                    int newHeight = (int) (bitmap.getHeight() * ((float) SCALED_WIDTH / bitmap.getWidth()));
                    final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, newHeight, true);

                    Log.e(LOGTAG, "Saving image...");
                    OutputStream stream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg"));
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                    Log.e(LOGTAG, "Saved.");
                } catch (Exception e){
                    Log.e(LOGTAG, e.toString());
                }

                    FileInputStream fis = null;
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_temp.jpg");
                    byte[] buffer = new byte[(int) file.length()];

                try {
                    fis = new FileInputStream(file);
                    fis.read(buffer);
                    fis.close();
                } catch (Exception e) {
                    Log.e(LOGTAG, "Failed to load image: wf_temp.jpg");
                }

                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    BasicHttpParams hparams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(hparams, 10 * 1000);
                    httpClient.setParams(hparams);

                    Log.e(LOGTAG, "POST: " + URL);

                    HttpPost httpPost = new HttpPost(URL);
                    httpPost.setEntity(new ByteArrayEntity(buffer));
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        Log.e(LOGTAG, "Server response not OK.");
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, e.toString());
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
