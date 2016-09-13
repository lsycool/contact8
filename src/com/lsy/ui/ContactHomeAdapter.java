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
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * @author lsycool
 * 为联系人listview绑定数据
 */
public class ContactHomeAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<ContactBean> list;
	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;
	private Context ctx;
	
	public ContactHomeAdapter(Context context, List<ContactBean> list, QuickAlphabeticBar alpha) {
		
		this.ctx = context;
		this.inflater = LayoutInflater.from(context);
		this.list = list; 
		this.alphaIndexer = new HashMap<String, Integer>();//存储首字母和其在list中位置的映射
		this.sections = new String[list.size()];

		for (int i =0; i <list.size(); i++) {
			String name = getAlpha(list.get(i).getPinyin());//获取首字母第一次出现的位置
			
			if(!alphaIndexer.containsKey(name)){ 
				alphaIndexer.put(name, i);
			}
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);//对首字母进行排序
		sections = new String[sectionList.size()];
		sectionList.toArray(sections);

		alpha.setAlphaIndexer(alphaIndexer);//把首字母的值传给字母栏
	}
	
	public HashMap<String, Integer> getAlphaIndexer(){
		return alphaIndexer;
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
			convertView = inflater.inflate(R.layout.contact_home_list_item, null);
			holder = new ViewHolder();
			holder.qcb = (QuickContactBadge) convertView.findViewById(R.id.qcb);
			holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.number = (TextView) convertView.findViewById(R.id.number);
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
		
		String currentStr = getAlpha(cb.getPinyin());		
		String previewStr = (position - 1) >= 0 ? getAlpha(list.get(position - 1).getPinyin()) : " ";
	
		if (!previewStr.equals(currentStr)) {//设置字母是否显示 
			holder.alpha.setVisibility(View.VISIBLE);
			holder.alpha.setText(currentStr);
		} else {
			holder.alpha.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	private static class ViewHolder {
		QuickContactBadge qcb;
		TextView alpha;
		TextView name;
		TextView number;
	}
	
	/**
	 * 获取首字母
	 * 
	 * @param str
	 * @return
	 */
	public String getAlpha(String str) {
		if (str == null) {
			return "#";
		}
		if (str.trim().length() == 0) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase(); 
		} else {
			return "#";
		}
	}
}
