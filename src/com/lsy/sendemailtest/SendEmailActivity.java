package com.lsy.sendemailtest;

import java.io.File;

import com.lsy.namespace.HomeContactActivity;
import com.lsy.namespace.R;
import com.lsy.update.UploadUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendEmailActivity extends Activity {
	private Button send;
	private EditText subject;
	private EditText body;
	private boolean flag;
	private String phonenum="";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendmail);

		send = (Button) findViewById(R.id.send);
		subject = (EditText) findViewById(R.id.subject);
		body = (EditText) findViewById(R.id.body);

		phonenum = ((TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		/*
		 * send.setText("提交"); 
		 * subject.setText("邮件主题，请输入...");
		 * body.setText("邮件内容，请输入...");
		 */

		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(subject.getText().toString().equals("")){
					Toast.makeText(getBaseContext(), "请填写反馈主题。", Toast.LENGTH_LONG).show();	
					return;
				}else if (body.getText().toString().equals("")) {
					Toast.makeText(getBaseContext(), "请填写详细内容。", Toast.LENGTH_LONG).show();	
					return;					
				}
				sendMail thread = new sendMail();
				thread.start();
				try {
					thread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
/*				File file = new File(getBaseContext().getFilesDir().getParent() + "/mydatabase.db");
				String requestURL = "http://lsycool.oicp.net/download";
				UploadUtil.setFile(file);
				UploadUtil.setRequestURL(requestURL);
				UploadUtil.uploadFILE();*/
				
				if (flag) {
					Toast.makeText(getBaseContext(), "提交成功，感谢您的建议。", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getBaseContext(), "无法连接服务器。", Toast.LENGTH_LONG).show();	
					return;
				}

				new Handler().postDelayed(new Runnable() {
					public void run() {
						// execute the task
						Intent i = new Intent(SendEmailActivity.this, HomeContactActivity.class);
						SendEmailActivity.this.startActivity(i);
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					}
				}, 2000);
			}
		});
	}

	private class sendMail extends Thread {
		@Override
		public void run() {
			try {
				MailSenderInfo mailInfo = new MailSenderInfo();
				mailInfo.setMailServerHost("smtp.sina.com");
				mailInfo.setMailServerPort("25");
				mailInfo.setValidate(true);
				mailInfo.setUserName("lsypc_891@sina.com"); // 你的邮箱地址
				mailInfo.setPassword("lsy5340891");// 您的邮箱密码
				mailInfo.setFromAddress("lsypc_891@sina.com");
				mailInfo.setToAddress("lsy97_cug@163.com");
				mailInfo.setSubject(subject.getText().toString());
				mailInfo.setContent(body.getText().toString()+"\n\n\n"+android.os.Build.MODEL+" + "
				+android.os.Build.VERSION.RELEASE + "  " + phonenum);

				// 这个类主要来发送邮件
				SimpleMailSender sms = new SimpleMailSender();
//				if(sms.sendTextMail(mailInfo,getBaseContext().getFilesDir().getParent() + "/mydatabase.db")){//有附件
				if(sms.sendTextMail(mailInfo)){//无附件
					flag = true;
				}else{
					flag = false;
				}// 发送文体格式
				// sms.sendHtmlMail(mailInfo);//发送html格式
			} catch (Exception e) {
				Log.e("SendMail", e.getMessage(), e);
			}
		}
	}
}