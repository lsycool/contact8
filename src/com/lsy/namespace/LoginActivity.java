package com.lsy.namespace;

import java.io.IOException;

import com.lsy.ui.LocusPassWordView;
import com.lsy.ui.LocusPassWordView.OnCompleteListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private LocusPassWordView lpwv;
	private static final int SPLASH_SHOW_TIME = 1000;
	private int trycount = 0;

	public final String ACTION_REBOOT = "android.intent.action.REBOOT";
	public final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
	public final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
	
	Handler handler = new Handler();
	Intent intent = new Intent();

	TextView title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		title = (TextView) findViewById(R.id.login_toast);
		lpwv = (LocusPassWordView) this.findViewById(R.id.mLocusPassWordView);

		if (lpwv.isPasswordEmpty()) {
			lpwv.resetPassWord("2,4,6,7,8");
		}

		if (lpwv.isPasswordEmpty()) {
			lpwv.resetPassWord("2,4,6,7,8");
			title.setVisibility(View.GONE);
			lpwv.setVisibility(View.GONE);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					intent.setClass(getApplicationContext(), HomeContactActivity.class);
					// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//注意本行的FLAG设置
					startActivity(intent);
					finish();
				}
			}, SPLASH_SHOW_TIME);
		} else {
			lpwv.setVisibility(View.VISIBLE);
			lpwv.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(String mPassword) {
					// 如果密码正确,则进入主页面。
					if (lpwv.verifyPassword(mPassword)) {
						intent.setClass(getApplicationContext(), HomeContactActivity.class);
						// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//注意本行的FLAG设置
						startActivity(intent);
						// overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
						finish();
					} else {
						if (trycount < 5){
							Toast.makeText(LoginActivity.this, "密码输入错误,请重新输入", Toast.LENGTH_SHORT).show();
							lpwv.clearPassword();
							trycount++;							
						}else {
							Toast.makeText(LoginActivity.this, "密码输入错误次数过多将启动手机自毁程序！", Toast.LENGTH_SHORT).show();
							/*
							 * try { Runtime.getRuntime().exec(new String[] {
							 * "su", "-c", "poweroff -f" }); } catch
							 * (IOException e) { // TODO Auto-generated catch
							 * block e.printStackTrace(); }
							 */
							new Handler().postDelayed(new Runnable() {
								public void run() {
/*									Intent i = new Intent(Intent.ACTION_REBOOT);
									i.putExtra("nowait", 1);
									i.putExtra("interval", 1);
									i.putExtra("window", 0);
									sendBroadcast(i);*/
					                Intent intent = new Intent(ACTION_REQUEST_SHUTDOWN);
					                intent.putExtra(EXTRA_KEY_CONFIRM, false);
					                //其中false换成true,会弹出是否关机的确认窗口
					                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					                startActivity(intent);
								}
							}, 3000);
						}
					}
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		// this.finish();
	}
}