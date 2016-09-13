package com.lsy.update;

import com.lsy.namespace.R;
import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 *@author coolszy
 *@date 2012-4-26
 *@blog http://blog.92coding.com
 */
public class UpdateActivity extends Activity
{
	private TextView appName;
	private TextView versionName;
	public  static SeekArc mSeekArc;
	public static TextView mSeekArcProgress;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);
		versionName = (TextView)findViewById(R.id.versionName);
		try {
			versionName.setText(getPackageManager().getPackageInfo("com.lsy.namespace", 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		appName = (TextView)findViewById(R.id.appName);
		appName.setText(R.string.app_name_show);
		
		mSeekArc = (SeekArc) findViewById(R.id.seekArc);
		mSeekArcProgress = (TextView) findViewById(R.id.seekArcProgress);
		mSeekArc.setOnSeekArcChangeListener(new OnSeekArcChangeListener() {	
			@Override
			public void onStopTrackingTouch(SeekArc seekArc) {	
			}		
			@Override
			public void onStartTrackingTouch(SeekArc seekArc) {
			}		
			@Override
			public void onProgressChanged(SeekArc seekArc, int progress,
					boolean fromUser) {
				mSeekArcProgress.setText(String.valueOf(progress));
			}
		});
		Button updateBtn = (Button) findViewById(R.id.btnUpdate);
		updateBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				UpdateManager manager = new UpdateManager(UpdateActivity.this);
				// 检查软件更新
				manager.checkUpdate();
			}
		});
 
	}
}