package com.lsy.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.lsy.bean.ContactBean;
import com.lsy.namespace.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * @author lsycool
 * 为联系人listview绑定数据
 */
public class SearchHomeAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<ContactBean> list;
	private Context ctx;
	
	public SearchHomeAdapter(Context context, List<ContactBean> list) {
		
		this.ctx = context;
		this.inflater = LayoutInflater.from(context);
		this.list = list; 	
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void remove(int position){
		list.remove(position);
	}
	
	public void update(int position,ContactBean bean){
		list.set(position,bean);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.search_home_list_item, null);
			holder = new ViewHolder();
			holder.qcb = (QuickContactBadge) convertView.findViewById(R.id.searchqcb);
			holder.name = (TextView) convertView.findViewById(R.id.searchname);
			holder.number = (TextView) convertView.findViewById(R.id.searchnumber);
			holder.call = (ImageView) convertView.findViewById(R.id.img_call);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ContactBean cb = list.get(position);
		String name = cb.getDisplayName();
		String number = cb.getPhoneNum();
		String job = cb.getGugan(); 
		holder.name.setText(name);
		holder.number.setText(number + "  " + job);
		holder.qcb.assignContactFromPhone(number, true);//系统对联系人的简要方法
		holder.call.setImageResource(R.drawable.btn_call);
		InputStream input = null;
		try {
			input = ctx.getResources().getAssets().open(cb.getPhotoname());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null==input){
			holder.qcb.setImageResource(R.drawable.touxiang);
		}else{
			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
			holder.qcb.setImageBitmap(contactPhoto);
		}		
	
		return convertView;
	}
	
	private static class ViewHolder {
		QuickContactBadge qcb;
		TextView name;
		TextView number;
		ImageView call;
	}
	
}
