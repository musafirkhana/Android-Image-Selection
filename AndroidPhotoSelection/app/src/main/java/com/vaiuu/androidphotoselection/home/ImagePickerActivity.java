package com.vaiuu.androidphotoselection.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.vaiuu.androidphotoselection.R;
import com.vaiuu.androidphotoselection.util.AppConstant;
import com.vaiuu.androidphotoselection.util.DisplayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Musafir Ali.
 */
public class ImagePickerActivity extends Activity implements AdapterView.OnItemClickListener {

	static final String TAG = "PHOTO_PICKER";

	Adapter adapter;
	DisplayImageOptions options;
	ArrayList<Uri> selected = new ArrayList<Uri>();
	public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png");
	GridView gridView;
	ProgressDialog mDialog;
	private Context context;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		setContentView(R.layout.ac_image_grid);
		context = this;
		setTitle("Pick Images");
		options = new DisplayImageOptions.Builder().cacheInMemory(true).displayer(new FadeInBitmapDisplayer(0))
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2).memoryCacheSize(1500000)
				// 1.5 Mb
				.denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				// Not necessary in common
				.build();
		ImageLoader.getInstance().init(config);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setOnItemClickListener(this);
		gridView.setLongClickable(true);

		adapter = new Adapter();
		gridView.setAdapter(adapter);
		getFilePaths();

		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Object obj = adapter.getItem(pos);
				Intent i = new Intent(getApplicationContext(), FullImageViewActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("position", pos);
				getApplicationContext().startActivity(i);
				return true;
			}
		});
	}

	public void btnChoosePhotosClick(View v) {
		ArrayList<String> selectedItems = new ArrayList<String>();
		for (int i = 0; i < selected.size(); i++) {
			selectedItems.add(selected.get(i).toString().replaceAll("file://", ""));
		}
		Toast.makeText(ImagePickerActivity.this, "Total photos selected: " + selectedItems.size(), Toast.LENGTH_SHORT)
				.show();
		Log.d("Image Selected ", "Selected Items: " + selectedItems.get(0));
		copyImage(selectedItems);

	}

	public void Cancel(View v) {
		ImagePickerActivity.this.finish();
	}

	public ArrayList<String> getFilePaths() {
		adapter.mContent.clear();
		ArrayList<String> it = new ArrayList<String>();
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DCIM
				+ "/Camera");
		Log.i("Environment.DIRECTORY_DCIM",
				Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DCIM);
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			Log.d("Count", file.getPath());
			if (IsSupportedFile(file.getPath())) {
				// Add image path to array list
				Uri uri = Uri.parse("file://" + file.getPath());
				it.add(file.getPath());
				adapter.mContent.add(uri);
			}

		}
		return it;
	}

	private boolean IsSupportedFile(String filePath) {
		String ext = filePath.substring((filePath.lastIndexOf(".") + 1), filePath.length());
		if (FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault())))
			return true;
		else
			return false;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Object obj = adapter.getItem(position);
		if (obj instanceof Uri) {
			if (selected.contains(obj)) {
				selected.remove(obj);
			} else {
				selected.add((Uri) obj);
			}
			adapter.notifyDataSetChanged();
		}
	}

	class Adapter extends BaseAdapter {
		ArrayList<Uri> mContent = new ArrayList<Uri>();
		@Override
		public int getCount() {
			return mContent.size();
		}
		@Override
		public Object getItem(int position) {
			if (position < mContent.size()) {
				return mContent.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object obj = getItem(position);

			if (obj instanceof Uri) {
				final String uri = obj.toString();

				FrameLayout view = (FrameLayout) (convertView == null || !"thumb".equals(convertView.getTag())
						? LayoutInflater.from(getContext()).inflate(R.layout.item_gallery_thumbnail, null)
						: convertView);
				ImageView imageView = (ImageView) view.findViewById(R.id.thumb);
				if (view.getLayoutParams() == null) {
					int size = (DisplayUtils.getScreenWidth(getContext()) - DisplayUtils.dpToPixel(getContext(), 16))
							/ 3;
					view.setLayoutParams(new AbsListView.LayoutParams(size, size));
					imageView.setMaxWidth(size);
					imageView.setMaxHeight(size);
				}
				view.setTag("thumb");

				ImageLoader.getInstance().displayImage(uri, imageView, options);
				Log.i("Uri Are ", uri);
				int order = -1;
				for (int i = 0; i < selected.size(); i++) {
					if (obj.equals(selected.get(i))) {
						order = i + 1;
						break;
					}
				}
				TextView orderText = (TextView) view.findViewById(R.id.order);
				if (order < 0) {
					orderText.setBackgroundDrawable(null);
					orderText.setText("");
				} else {
					orderText.setBackgroundDrawable(getResources().getDrawable(R.drawable.gallery_photo_selected));
					orderText.setText(order + "");
				}

				return view;
			}

			return null;
		}
	}

	public static void storeImage(Bitmap image, File pictureFile) {
		if (pictureFile == null) {
			Log.d(TAG, "Error creating media file, check storage permissions: ");
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			image.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	protected Context getContext() {
		return this;
	}

	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	private void copyImage(final ArrayList<String> sourcePath) {
		(new AsyncTask() {
			@Override
			protected void onPreExecute() {
				mDialog = new ProgressDialog(context);
				mDialog.setMessage("Uploading Photo...");
				mDialog.setCancelable(true);
				mDialog.show();
				super.onPreExecute();
			}

			@Override
			protected Object doInBackground(Object... params) {
				try {
					File sd = Environment.getExternalStorageDirectory();
					System.out.println("(sourcePath.size()) = " + sourcePath.size());
					int counter = 0;
					if (sd.canWrite()) {
						System.out.println("(sd.canWrite()) = " + (sd.canWrite()));
						do {
							counter++;
							String destinationImagePath = sourcePath.get(counter)
									.substring(sourcePath.get(counter).lastIndexOf("/") + 1);
							File source = new File(sourcePath.get(counter));
							System.out.println("(source) = " + source);
							File destination = new File(AppConstant.PIC_DERECTORY, destinationImagePath);
							if (source.exists()) {
								FileChannel src = new FileInputStream(source).getChannel();
								FileChannel dst = new FileOutputStream(destination).getChannel();
								/************************
								 * copy the first file to second.....
								 ************************/
								dst.transferFrom(src, 0, src.size());
								src.close();
								dst.close();

								File file = new File(sourcePath.get(counter));
								boolean deleted = file.delete();

								Log.w("Deleted file path ", "" + deleted);
							}
						} while (counter <= sourcePath.size());

					} else {
						Toast.makeText(getApplicationContext(), "SDCARD Not writable.", Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					System.out.println("Error :" + e.getMessage());
					// Toast.makeText(getApplicationContext(), e.getMessage(),
					// Toast.LENGTH_LONG).show();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				super.onPostExecute(result);
				if (sourcePath.size() == 0) {
					Toast.makeText(getApplicationContext(), "Please Select at least one image", Toast.LENGTH_LONG)
							.show();
				} else {
					ImagePickerActivity.this.finish();
					Toast.makeText(getApplicationContext(), "Successfully Uploaded Photos", Toast.LENGTH_LONG).show();
				}

				mDialog.dismiss();

			}

		}).execute();
	}
	
	

}
