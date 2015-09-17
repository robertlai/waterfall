package app.waterfall.robertlai.com.waterfall;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.apache.http.Header;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CaptureActivity extends ActionBarActivity {

    private static String logtag = "Waterfall";
    private static int TAKE_PICTURE = 1;
    private static int SCALED_WIDTH = 1024;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static Date date;
    private static Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Button cameraButton = (Button)findViewById(R.id.button_camera);
        cameraButton.setOnClickListener(cameraListener);
    }

    private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View view){
            takePhoto(view);
        }
    };

    private void takePhoto(View view){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        date = new Date();
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + sdf.format(date) + ".jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
        setIntent(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);

            ImageView imageView = (ImageView)findViewById(R.id.image_camera);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                int newHeight = (int) ( bitmap.getHeight() * ((float)SCALED_WIDTH / bitmap.getWidth()) );
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, newHeight, true);
                imageView.setImageBitmap(scaledBitmap);
                Toast.makeText(CaptureActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();

                //String url = "http://localhost:3000/api";
                String url = "http://waterfallapi.herokuapp.com/api";

                File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Waterfall"), "wf_" + sdf.format(date) + ".jpg");
                RequestParams params = new RequestParams();
                try {
                    params.put("image", myFile);
                } catch(Exception e) {}

                AsyncHttpClient client = new AsyncHttpClient();
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                        System.out.println(statusCode);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                        System.out.println(statusCode);
                    }
                });
            } catch (Exception e){
                Log.e(logtag, e.toString());
            }
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
