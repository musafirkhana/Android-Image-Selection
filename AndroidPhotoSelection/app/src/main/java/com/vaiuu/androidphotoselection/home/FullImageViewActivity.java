package com.vaiuu.androidphotoselection.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;

import com.vaiuu.androidphotoselection.R;
import com.vaiuu.androidphotoselection.util.ImageUtils;

public class FullImageViewActivity extends Activity {

	private Context context;
	private ImageUtils imageUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture_detail);
		context = this;
		imageUtils = new ImageUtils(this);
		Intent i = getIntent();

		int position = i.getIntExtra("position", 0);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		FullScreenImageAdapter adapter = new FullScreenImageAdapter(FullImageViewActivity.this, imageUtils.getFilePaths());
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(position);
	}

	public void Back(View view) {
		FullImageViewActivity.this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, 0);
	}


}
