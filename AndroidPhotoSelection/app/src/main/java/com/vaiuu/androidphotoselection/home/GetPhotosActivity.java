package com.vaiuu.androidphotoselection.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vaiuu.androidphotoselection.R;
import com.vaiuu.androidphotoselection.util.AppConstant;
import com.vaiuu.androidphotoselection.util.BitmapUtils;
import com.vaiuu.androidphotoselection.util.ImageFilePath;
import com.vaiuu.androidphotoselection.util.Tools;

import java.io.File;
import java.util.List;

public class GetPhotosActivity extends Activity {

	private Context context;
	private static final int TAKE_PICTURE = 1;
	private Uri imageUri;
	private String imageName = "";
	 private static final int REQUEST_CODE_TAKEN_PHOTO_GALLERY = 0x02;
	private File sdCard = Environment.getExternalStorageDirectory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture);
		//Create Uploaded Image Path
		File PicDirectory = new File(sdCard.getAbsolutePath() + (AppConstant.PHOTO_PATH ));
		if (!PicDirectory.exists()) {
			PicDirectory.mkdirs();
			AppConstant.PIC_DERECTORY = PicDirectory;

		}

		context = this;
		imageName = "" + Tools.TIME_MILLISECOND;
	}

	public void Gallery(View v) {
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // If I remove this line, the Google+ Photos will be opened prior.
        // It works well after I uninstalled Google+, but disable Google+ doesn't work.
        // So it seems like a trick made by Google+ teams.
        intent.setClass(this, ImagePickerActivity.class);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_TAKEN_PHOTO_GALLERY);

	}

	public void Camera(View v) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(AppConstant.PIC_DERECTORY, imageName + ".jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		imageUri = Uri.fromFile(photo);
		startActivityForResult(intent, TAKE_PICTURE);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case RESULT_OK:
			if (requestCode == TAKE_PICTURE) {
				Toast.makeText(context, "Image Saved", Toast.LENGTH_LONG).show();
				// Uri selectedImageUri = data.getData();
				Uri selectedImage = imageUri;
				String selectedImagePath = ImageFilePath.getPath(context,
						selectedImage);
				Log.w("Image Path", selectedImagePath);

			}
			break;
		case RESULT_CANCELED:
			break;
		case REQUEST_CODE_TAKEN_PHOTO_GALLERY: {
            if (data != null) {
                if (data.getParcelableArrayListExtra("uris") != null) {
                    List<Uri> uriList = data.getParcelableArrayListExtra("uris");
                    for (int i = 0; i < uriList.size(); i++) {

                        new GetBitmapFromUriTask(this, uriList.get(i), new GetBitmapFromUriTask.IOnImageTakenListener() {
                            @Override
                            public void onImageTaken(final Bitmap pBitmap) {
                                GetPhotosActivity.this.onImageTaken(pBitmap);
                            }
                        }).execute();
                    }
                } else {
                    new GetBitmapFromUriTask(this, data.getData(), new GetBitmapFromUriTask.IOnImageTakenListener() {
                        @Override
                        public void onImageTaken(final Bitmap pBitmap) {
                        	GetPhotosActivity.this.onImageTaken(pBitmap);
                        }
                    }).execute();
                }

                break;
            }
        }
		default:
			break;
		}
	}
	  private void onImageTaken(Bitmap pBitmap) {
	        if (pBitmap == null) {
	            Toast.makeText(this, "image decode error", Toast.LENGTH_LONG).show();
	            return;
	        }
	        DisplayMetrics dm = getResources().getDisplayMetrics();

	        int width = dm.widthPixels;
	        int height = pBitmap.getHeight() * dm.widthPixels / pBitmap.getWidth();

	        ImageView imageView = new ImageView(this);
	        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	        imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
	        imageView.setImageBitmap(BitmapUtils.resizeBitmap(pBitmap, width, height));

	    }

	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, 0);
	}
}
