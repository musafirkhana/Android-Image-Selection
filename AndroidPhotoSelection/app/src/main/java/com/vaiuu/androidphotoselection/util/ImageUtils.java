package com.vaiuu.androidphotoselection.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

@SuppressLint("NewApi")
public class ImageUtils {

	private Context _context;

	// constructor
	public ImageUtils(Context context) {
		this._context = context;
	}

	/*
	 * Reading file paths from SDCard
	 */
	/*
	 * public ArrayList<String> getFilePaths() { ArrayList<String> filePaths =
	 * new ArrayList<String>();
	 * 
	 * File directory = new File(
	 * android.os.Environment.getExternalStorageDirectory() + File.separator +
	 * AppConstant.PHOTO_ALBUM);
	 * 
	 * // check for directory if (directory.isDirectory()) { // getting list of
	 * file paths File[] listFiles = directory.listFiles();
	 * 
	 * // Check for count if (listFiles.length > 0) {
	 * 
	 * // loop through all files for (int i = 0; i < listFiles.length; i++) {
	 * 
	 * // get file path String filePath = listFiles[i].getAbsolutePath();
	 * 
	 * // check for supported file extension if (IsSupportedFile(filePath)) { //
	 * Add image path to array list filePaths.add(filePath); } } } else { //
	 * image directory is empty Toast.makeText( _context,
	 * AppConstant.PHOTO_ALBUM + " is empty. Please load some images in it !",
	 * Toast.LENGTH_LONG).show(); }
	 * 
	 * } else { AlertDialog.Builder alert = new AlertDialog.Builder(_context);
	 * alert.setTitle("Error!"); alert.setMessage(AppConstant.PHOTO_ALBUM +
	 * " directory path is not valid! Please set the image directory name AppConstant.java class"
	 * ); alert.setPositiveButton("OK", null); alert.show(); }
	 * 
	 * return filePaths; }
	 */
	public ArrayList<String> getFilePaths() {
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
				it.add(file.getPath());
			}

		}
		return it;
	}

	/*
	 * Check supported file extensions
	 * 
	 * @returns boolean
	 */
	private boolean IsSupportedFile(String filePath) {
		String ext = filePath.substring((filePath.lastIndexOf(".") + 1), filePath.length());

		if (AppConstant.FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault())))
			return true;
		else
			return false;

	}

	/*
	 * getting screen width
	 */
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}

	public static Bitmap decodeFile(String path) {

		int orientation;

		try {

			if (path == null) {

				return null;
			}
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 4;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap bm = BitmapFactory.decodeFile(path, o2);

			Bitmap bitmap = bm;
			ExifInterface exif = new ExifInterface(path);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			Log.e("orientation", "" + orientation);
			Matrix m = new Matrix();
			if ((orientation == 3)) {
				m.postRotate(180);
				m.postScale((float) bm.getWidth(), (float) bm.getHeight());
				// if(m.preRotate(90)){
				Log.e("in orientation", "" + orientation);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				return bitmap;
			} else if (orientation == 6) {
				m.postRotate(90);
				Log.e("in orientation", "" + orientation);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				return bitmap;
			} else if (orientation == 8) {
				m.postRotate(270);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				return bitmap;
			}
			return bitmap;
		} catch (Exception e) {
		}
		return null;
	}
	public  void showToast(String message) {
		Toast.makeText(_context, message, Toast.LENGTH_SHORT).show();
	}
}
