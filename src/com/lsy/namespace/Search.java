package com.lsy.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lsy.bean.ContactBean;
import com.lsy.ui.KeywordsFlow;
import com.lsy.ui.SearchHomeAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Search extends Activity implements OnClickListener {

	private static final int FEEDKEY_START = 1;
	private ImageView back_arrow;
	private Button searchbutton;
	private Animation shakeAnim;
	private EditText searchEdit;
	private KeywordsFlow keywordsFlow;
	private ListView personList;
	private int STATE = 1;
	private List<ContactBean> list;
	private Map<String, ContactBean> contactIdMap;
	private static String[] keywords;
	boolean flag = false;
	private SearchHomeAdapter adapter;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FEEDKEY_START:
				keywordsFlow.rubKeywords();// 关键字清零
				feedKeywordsFlow(keywordsFlow, keywords);// 重新获取关键字
				keywordsFlow.go2Show(KeywordsFlow.ANIMATION_OUT);
				sendEmptyMessageDelayed(FEEDKEY_START, 5000);
				break;
			}
		};
	};

	private static void feedKeywordsFlow(KeywordsFlow keywordsFlow, String[] arr) {
		Random random = new Random();
		for (int i = 0; i < KeywordsFlow.MAX; i++) {
			int ran = random.nextInt(arr.length);
			String tmp = arr[ran];
			keywordsFlow.feedKeyword(tmp);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		list = Contact8Activity.list;
		keywords = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			keywords[i] = list.get(i).getDisplayName();
		}
		shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_y);
		initView();

		searchbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				searchEdit.setText("");
				keywordsFlow.setVisibility(View.VISIBLE);
				personList.setVisibility(View.INVISIBLE);
			}
		});

		searchEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.toString().equals("")) {
					keywordsFlow.setVisibility(View.VISIBLE);
					personList.setVisibility(View.INVISIBLE);
				} else {
					keywordsFlow.setVisibility(View.INVISIBLE);
					personList.setVisibility(View.VISIBLE);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (!s.toString().equals("")) {
					queryContactMember(s.toString());
				}
			}

		});

	}

	private void queryContactMember(String sqlname) {

		String sql = null;
		if (isNumeric(sqlname)) {
			sql = "SELECT * FROM db WHERE " + "telephone like ?";
		}else if (isAlpha(sqlname)) {
			sql = "SELECT * FROM db WHERE " + "pinyin like ?";
		}else {
			sql = "SELECT * FROM db WHERE " + "name like ?";
		}
		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		list = new ArrayList<ContactBean>();
		Cursor c = database.rawQuery(sql, new String[] { "%" + sqlname + "%" });
		if (c.getCount() == 0) {
			c.close();
			return;
		}

		contactIdMap = new HashMap<String, ContactBean>();
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
				person.setPhoneNum(c.getString(c.getColumnIndex("telephone")));
				list.add(person);
				contactIdMap.put(num, person);
			}
		}
		if (list.size() > 0) {
			Collections.sort(list);
			setAdapter(list);
		}
		c.close();
	}

	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否字母
	 * 
	 * @param str
	 * @return
	 */
	public boolean isAlpha(String str) {

		char c = str.trim().substring(0, 1).charAt(0);
		
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return true; 
		} else {
			return false;
		}
	}

	
	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信", "查看详细", "修改信息", "删除此人", "添加到本地" };

	private void setAdapter(List<ContactBean> list) {
		adapter = new SearchHomeAdapter(this, list);// 把联系人信息绑定到listview
		personList.setAdapter(adapter);

		personList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
				showContactDialog(lianxiren1, cb, position);
				return true;
			}

		});

		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
				String toPhone = cb.getPhoneNum();
				Uri uri = Uri.parse("tel:" + toPhone);
				Intent it = new Intent(Intent.ACTION_CALL, uri);
				startActivity(it);
			}
		});
	}

	// 联系人操作弹出页
	private void showContactDialog(final String[] arg, final ContactBean cb, final int position) {
		new AlertDialog.Builder(this).setTitle(cb.getDisplayName()).setIcon(R.drawable.add_contact)
				.setItems(arg, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Uri uri = null;

						switch (which) {

						case 0:// 打电话
							String toPhone = cb.getPhoneNum();
							uri = Uri.parse("tel:" + toPhone);
							Intent it = new Intent(Intent.ACTION_CALL, uri);
							startActivity(it);
							break;

						case 1:// 发短息

							HomeContactActivity.member = cb;
							Intent intent = new Intent(Search.this, MessageBoxList.class);
							Search.this.startActivity(intent);

							break;

						case 2:// 查看详细

							HomeContactActivity.member = cb;
							Intent i = new Intent(Search.this, memberinfo.class);
							// i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//注意本行的FLAG设置
							Search.this.startActivity(i);
							break;

						case 3:// 修改联系人资料

							UpdateContact(cb,position);
							break;

						case 4:// 删除

							showDelete(cb, position);
							break;
							
						case 5://添加到本地
							
							uri = Uri.parse("content://com.android.contacts/raw_contacts");
							ContentResolver resolver = Search.this.getContentResolver();
							ContentValues values = new ContentValues();
							long contactId = ContentUris.parseId(resolver.insert(uri, values));
							
							/* 往 data 中添加数据（要根据前面获取的id号） */
							// 添加姓名
							uri = Uri.parse("content://com.android.contacts/data");
							values.put("raw_contact_id", contactId);
							values.put("mimetype", "vnd.android.cursor.item/name");
							values.put("data2", cb.getDisplayName());
							resolver.insert(uri, values);
							
							// 添加电话
							values.clear();
							values.put("raw_contact_id", contactId);
							values.put("mimetype", "vnd.android.cursor.item/phone_v2");
							values.put("data2", "2");
							values.put("data1", cb.getPhoneNum());
							resolver.insert(uri, values);
							
							// 添加电话
							values.clear();
							values.put("raw_contact_id", contactId);
							values.put("mimetype", "vnd.android.cursor.item/organization");
							values.put("data2", "2");
							values.put("data1", cb.getdanwei());
							resolver.insert(uri, values);
							Toast.makeText(Search.this, "添加成功.", Toast.LENGTH_SHORT).show();

							break;
						}
					}
				}).show();
	}

	private void UpdateContact(final ContactBean cb, final int position) {
		final EditText text = new EditText(this);
		text.setInputType(InputType.TYPE_CLASS_PHONE);
		text.setText(cb.getPhoneNum());
		new AlertDialog.Builder(this).setTitle("更新联系人电话").setIcon(R.drawable.contact_app).setView(text)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						if (true) {
							SQLiteDatabase db = Contact8Activity.dbHelper.getDatabase();
							ContentValues cv = new ContentValues();
							cv.put("telephone", cb.getPhoneNum());
							db.update("db", cv, "num = ?", new String[] { text.getText().toString().trim() });
						}
						ContactBean cb_tem = cb;
						cb_tem.setPhoneNum(text.getText().toString().trim());
						adapter.update(position, cb_tem);
						adapter.notifyDataSetChanged();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

						Toast.makeText(Search.this, "更新成功.", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
//						text.setInputType(InputType.TYPE_NULL);
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}).show();
	}

	// 删除联系人方法
	private void showDelete(final ContactBean cb, final int position) {

		new AlertDialog.Builder(this).setIcon(R.drawable.contact_app).setTitle("是否删除此联系人")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						if (true) {
							SQLiteDatabase db = Contact8Activity.dbHelper.getDatabase();
							db.delete("db", "num = ?", new String[] { cb.getXuehao() });
						}
						adapter.remove(position);
						adapter.notifyDataSetChanged();
						Toast.makeText(Search.this, "联系人已删除.", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();
	}

	private void initView() {
		keywordsFlow = (KeywordsFlow) findViewById(R.id.keywordsflow);
		keywordsFlow.setDuration(1000l);
		keywordsFlow.setOnItemClickListener(this);
		back_arrow = (ImageView) findViewById(R.id.back_arrow);
		back_arrow.setAnimation(shakeAnim);
		searchEdit = (EditText) findViewById(R.id.search_view);
		searchbutton = (Button) findViewById(R.id.search_button);
		personList = (ListView) findViewById(R.id.search_list);
		feedKeywordsFlow(keywordsFlow, keywords);
		keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
		handler.sendEmptyMessageDelayed(FEEDKEY_START, 5000);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof TextView) {
			String keyword = ((TextView) v).getText().toString().trim();
			searchEdit.setText(keyword);
			searchEdit.setSelection(keyword.length());
		}
	}

	@Override
	public void onBackPressed() {
		/*
		 * Intent intent = new Intent(Intent.ACTION_MAIN);
		 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * intent.addCategory(Intent.CATEGORY_HOME); startActivity(intent);
		 */
		STATE = 0;
		handler.removeMessages(FEEDKEY_START);
		// Intent i = new Intent(Search.this, HomeContactActivity.class);
		// i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//注意本行的FLAG设置
		// Search.this.startActivity(i);
		Search.this.setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		back_arrow.clearAnimation();
		handler.removeMessages(FEEDKEY_START);
		STATE = 0;
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeMessages(FEEDKEY_START);
		STATE = 0;
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeMessages(FEEDKEY_START);
		STATE = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (STATE == 0) {
			keywordsFlow.rubKeywords();
			handler.sendEmptyMessageDelayed(FEEDKEY_START, 5000);
		}

	}
	
}
