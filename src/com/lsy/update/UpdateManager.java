package com.lsy.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.lsy.bean.ContactBean;
import com.lsy.namespace.Contact8Activity;
import com.lsy.namespace.DBManager;
import com.lsy.namespace.HomeContactActivity;
import com.lsy.namespace.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

/**
 * @author coolszy
 * @date 2012-4-26
 * @blog http://blog.92coding.com
 */

public class UpdateManager {
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	/* 保存解析的XML信息 */
	HashMap<String, String> mHashMap;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private String upclass = "app";

	private Context mContext;
	/* 更新进度条 */
	// private ProgressBar mProgress;
	// private Dialog mDownloadDialog;

	private String urlStr = "http://lsycool.oicp.net/download/version.xml";
	private InputStream inputStream;
	private boolean flag = false;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				// mProgress.setProgress(progress);
				UpdateActivity.mSeekArc.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 */
	public void checkUpdate() {
		if (isUpdate()) {
			// 显示提示对话框
			showNoticeDialog();
		} else {
			if (!flag) {
				flag = !flag;
				Toast.makeText(mContext, R.string.soft_update_no, Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 检查软件是否有更新版本
	 * 
	 * @return
	 */
	private boolean isUpdate() {
		// 获取当前软件版本
		int versionCode = getVersionCode(mContext);
		// 把version.xml放到网络上，然后获取文件信息
		// inputStream =
		// ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");
		getInputStreamFromHttpUrl thread = new getInputStreamFromHttpUrl();
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (null != mHashMap) {
			int serviceCode = Integer.valueOf(mHashMap.get("version"));
			// String appname = mHashMap.get("name");
			// 版本判断
			if (serviceCode > versionCode) {
				return true;
			}
		} else {
			flag = true;
			Toast.makeText(mContext, "无法连接到服务器！", Toast.LENGTH_LONG).show();
		}
		return false;
	}

	private class getInputStreamFromHttpUrl extends Thread {
		@Override
		public void run() {
			try {
				// 把网络访问的代码放在这里
				URL url = new URL(urlStr);
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setConnectTimeout(6000);
				inputStream = urlConn.getInputStream();
				if (inputStream != null) {
					ParseXmlService service = new ParseXmlService();
					mHashMap = service.parseXml(inputStream);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// Toast.makeText(mContext, "配置文件错误！",
				// Toast.LENGTH_LONG).show();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				// Toast.makeText(mContext, "无法连接到网络！",
				// Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
				// Toast.makeText(mContext, "配置文件解析错误！",
				// Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo("com.lsy.namespace", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		upclass = mHashMap.get("upclass");
		builder.setTitle(R.string.soft_update_title);
		if (upclass.equals("app")) {
			builder.setMessage(R.string.soft_update_info);
		} else if (upclass.equals("data")) {
			builder.setMessage(R.string.soft_update_info1);
		}else if (upclass.equals("dataapp")) {
			builder.setMessage(R.string.soft_update_info2);			
		}
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 显示下载对话框
				// showDownloadDialog();
				UpdateActivity.mSeekArc.setVisibility(View.VISIBLE);
				UpdateActivity.mSeekArcProgress.setVisibility(View.VISIBLE);
				if (upclass.equals("app")) {
					downloadApk();
				} else if (upclass.equals("data")) {
					downloadData();
				}else if (upclass.equals("dataapp")) {
					downloadDataApp();
				}
			}
		});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				UpdateActivity.mSeekArc.setVisibility(View.GONE);
				UpdateActivity.mSeekArcProgress.setVisibility(View.GONE);
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	/*
	 * private void showDownloadDialog() { // 构造软件下载对话框 AlertDialog.Builder
	 * builder = new Builder(mContext);
	 * builder.setTitle(R.string.soft_updating); // 给下载对话框增加进度条 final
	 * LayoutInflater inflater = LayoutInflater.from(mContext); View v =
	 * inflater.inflate(R.layout.softupdate_progress, null); mProgress =
	 * (ProgressBar) v.findViewById(R.id.update_progress); builder.setView(v);
	 * // 取消更新 builder.setNegativeButton(R.string.soft_update_cancel, new
	 * OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * dialog.dismiss(); // 设置取消状态 cancelUpdate = true; } }); mDownloadDialog =
	 * builder.create(); mDownloadDialog.show(); // 现在文件 downloadApk(); }
	 */

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					URL url = new URL(mHashMap.get("url1"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(6000);
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, mHashMap.get("name"));
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) Math.ceil(((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			// mDownloadDialog.dismiss();
		}
	};

	/*
	 * 下载数据
	 */
	private void downloadData() {
		new downloadDataThread().start();
	}

	/**
	 * 下载数据线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadDataThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String path = mContext.getFilesDir().getParent();// "/data/data/com.lsy.namespace";
					String mSavePath = path + "/mydatabase.db";
					URL url = new URL(mHashMap.get("url2"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(6000);
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File Datafile = new File(mSavePath);
					// 判断文件目录是否存在
					if (Datafile.exists()) {
						Datafile.delete();
					}
					// File apkFile = new File(mSavePath, "mydatabase.db");
					FileOutputStream fos = new FileOutputStream(Datafile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) Math.ceil(((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							updateAdapt();
							// Toast.makeText(mContext, "数据更新完成.",
							// Toast.LENGTH_LONG).show();
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			// mDownloadDialog.dismiss();
		}
	};

	/**
	 * 下载数据线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadDataAppThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String path = mContext.getFilesDir().getParent();
					String mSavePath2 = path + "/mydatabase.db";
					
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					String mSavePath1 = sdpath + "download";
					mSavePath = mSavePath1;

					URL url1 = new URL(mHashMap.get("url1"));//apk路径
					URL url2 = new URL(mHashMap.get("url2"));//数据路径
					// 创建连接
					HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
					conn1.setConnectTimeout(6000);
					conn1.connect();
					
					HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
					conn2.setConnectTimeout(6000);
					conn2.connect();

					// 获取文件大小
					int length = conn1.getContentLength() + conn2.getContentLength();
					// 创建输入流
					InputStream is1 = conn1.getInputStream();//apk
					InputStream is2 = conn2.getInputStream();//data

					File Datafile = new File(mSavePath2);
					// 判断文件目录是否存在
					if (Datafile.exists()) {
						Datafile.delete();
					}
					FileOutputStream fos2 = new FileOutputStream(Datafile);

					File file = new File(mSavePath1);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath1, mHashMap.get("name"));
					FileOutputStream fos1 = new FileOutputStream(apkFile);
					

					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is2.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) Math.ceil(((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							break;
						}
						// 写入文件
						fos2.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					
					fos2.close();
					is2.close();

					// 写入到文件中
					do {
						int numread = is1.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) Math.ceil(((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos1.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.	
					
					fos1.close();
					is1.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	/*
	 * 下载数据
	 */
	private void downloadDataApp() {
		new downloadDataAppThread().start();
	}
	
	void updateAdapt() {

		Contact8Activity.dbHelper.closeDatabase();
		Contact8Activity.dbHelper = new DBManager(mContext);
		Contact8Activity.dbHelper.openDatabase();
		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		Cursor c = database.rawQuery("SELECT * FROM db", null);
		MyTask task = new MyTask();
		task.execute(c);
	}

	private class MyTask extends AsyncTask<Cursor, Void, ArrayList<ContactBean>> {
		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {

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
			Contact8Activity.list = result;

			Intent i = new Intent(mContext, HomeContactActivity.class);
			mContext.startActivity(i);
			((Activity) mContext).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}

		// onCancelled方法用于在取消执行中的任务时更改UI
		@Override
		protected void onCancelled() {

		}
	}

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, mHashMap.get("name"));
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
