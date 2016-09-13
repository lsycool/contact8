package com.lsy.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.lsy.bean.ContactBean;
import com.lsy.ui.MetaballView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class Contact8Activity extends Activity {

	public static DBManager dbHelper;
	public static ArrayList<ContactBean> list;
	private MetaballView metaballView;
	// private SplashScreen mSplashScreen;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		metaballView = (MetaballView) this.findViewById(R.id.metaball);

		dbHelper = new DBManager(this);
		dbHelper.openDatabase();

		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		list = new ArrayList<ContactBean>();
		Cursor c = database.rawQuery("SELECT * FROM db", null);
		MyTask task = new MyTask();
		task.execute(c);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.closeDatabase();
	}

	private class MyTask extends AsyncTask<Cursor, Void, ArrayList<ContactBean>> {
		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {
			metaballView.setPaintMode(0);
		}

		// doInBackground方法内部执行后台任务,不可在此方法内修改UI
		@Override
		protected ArrayList<ContactBean> doInBackground(Cursor... params) {
			Cursor c = params[0];
			ArrayList<ContactBean> contactlist = new ArrayList<ContactBean>();
			try {
				HashMap<String, ContactBean> contactIdMap = new HashMap<String, ContactBean>();
				while (c.moveToNext()) {
					String num = c.getString(c.getColumnIndex("num"));
					if (!contactIdMap.containsKey(num)) {

						ContactBean person = new ContactBean();
						person.setDisplayName(c.getString(c.getColumnIndex("name")));
						person.setSex(c.getString(c.getColumnIndex("sex")));
						person.setXuehao(num);
						person.setJiguan(c.getString(c.getColumnIndex("jiguan")));
						person.setLeibie(c.getString(c.getColumnIndex("danwei")));
						person.setZhuanye(c.getString(c.getColumnIndex("speciality")));
						person.setPhoneNum(c.getString(c.getColumnIndex("telephone")));
						person.setGugan(c.getString(c.getColumnIndex("danren")));
						person.setweixing(c.getString(c.getColumnIndex("wechat")));
						person.setdanwei(c.getString(c.getColumnIndex("workco")));
						contactlist.add(person);
						contactIdMap.put(num, person);
					}
				}
				if (contactlist.size() > 0) {
					Collections.sort(contactlist);
				}
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return contactlist;
		}

		// onPostExecute方法用于在执行完后台任务后更新UI,显示结果
		@Override
		protected void onPostExecute(ArrayList<ContactBean> result) {
			list = result;
			new Handler().postDelayed(new Runnable() {
				public void run() {
					// execute the task
					// mSplashScreen.removeSplashScreen();
					Intent i = new Intent(Contact8Activity.this, LoginActivity.class);
					Contact8Activity.this.startActivity(i);
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
			}, 1000);
		}

		// onCancelled方法用于在取消执行中的任务时更改UI
		@Override
		protected void onCancelled() {

		}
	}
}