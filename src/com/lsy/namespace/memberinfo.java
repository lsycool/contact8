package com.lsy.namespace;

import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class memberinfo extends Activity {
	
	private TextView name;
	private TextView phone;
	private TextView xuehao;
	private TextView jiguan;
	private TextView leibie;
	private TextView zhuanye;
	private TextView renzhi;
	private ImageView photo;
	private TextView weixing;
	private TextView danwei;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listitem_user);		
		name = (TextView) findViewById(R.id.nameText);
		name.setText(HomeContactActivity.member.getDisplayName());
		phone = (TextView) findViewById(R.id.DiaText);
		phone.setText(HomeContactActivity.member.getPhoneNum());
		xuehao = (TextView) findViewById(R.id.xuehao);
		xuehao.setText(HomeContactActivity.member.getXuehao());
		jiguan = (TextView) findViewById(R.id.jiguan);
		jiguan.setText(HomeContactActivity.member.getJiguan());
		leibie = (TextView) findViewById(R.id.leibie);
		leibie.setText(HomeContactActivity.member.getLeibie());
		zhuanye = (TextView) findViewById(R.id.zhuanye);
		zhuanye.setText(HomeContactActivity.member.getZhuanye());
		renzhi = (TextView) findViewById(R.id.renzhi);
		renzhi.setText(HomeContactActivity.member.getGugan());
		photo = (ImageView) findViewById(R.id.user_item_iv_avatar);
		weixing = (TextView) findViewById(R.id.weixing);
		weixing.setText(HomeContactActivity.member.getweixing());
		danwei = (TextView) findViewById(R.id.danwei);
		danwei.setText(HomeContactActivity.member.getdanwei());
		
		InputStream input = null;
		try {
			input = this.getResources().getAssets().open(HomeContactActivity.member.getPhotoname());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null==input){
			photo.setImageResource(R.drawable.touxiang);
		}else{
			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
			photo.setImageBitmap(contactPhoto);
		}
		
	}
	
}
