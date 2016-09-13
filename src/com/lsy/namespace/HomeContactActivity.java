package com.lsy.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ResideMenu.ResideMenu;
import com.ResideMenu.ResideMenuItem;
import com.lsy.bean.ContactBean;
import com.lsy.bean.GroupBean;
import com.lsy.namespace.R;
import com.lsy.sendemailtest.SendEmailActivity;
import com.lsy.ui.ContactHomeAdapter;

import com.lsy.ui.QuickAlphabeticBar;
import com.lsy.ui.SystemScreenInfo;
import com.lsy.update.UpdateActivity;
import com.melnykov.fab.FabToolbar;

import android.R.bool;
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
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeContactActivity extends Activity implements View.OnClickListener {

	private View acbuwaPage;

	public static Button menuBtn;
	private LayoutInflater inflater;

	private ContactHomeAdapter adapter;
	private ListView personList;
	private List<ContactBean> list;
	private QuickAlphabeticBar alpha;
	private Map<String, ContactBean> contactIdMap;
	private ImageButton addContactBtn;
	private TextView mDialogText;
	public static ContactBean member;
	private FabToolbar fab;

	private ResideMenu resideMenu;
	private List<GroupBean> ALLitem;
	private List<GroupBean> ALLitemR;
	private int resideMenuIndexL = -1;
	private int resideMenuIndexR = -1;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		inflater = LayoutInflater.from(this);// 从指定上下文中获取LayoutInflater（把xml变成一个view实例）

		SystemScreenInfo.getSystemInfo(HomeContactActivity.this);// 获取手机屏幕信息
		acbuwaPage = inflater.inflate(R.layout.home_contact_page, null);
		setContentView(acbuwaPage);// 侧栏xml

		menuBtn = (Button) this.acbuwaPage.findViewById(R.id.menuBtn);// 开关群组
		personList = (ListView) this.acbuwaPage.findViewById(R.id.acbuwa_list);// 联系人列表

		alpha = (QuickAlphabeticBar) this.acbuwaPage.findViewById(R.id.fast_scroller);// 字母排序
		mDialogText = (TextView) this.acbuwaPage.findViewById(R.id.fast_position);
		init();

		menuBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (resideMenu.isOpened() == false) {
					resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
				} else {
					resideMenu.closeMenu();
					// menuBtn.setBackgroundResource(R.drawable.menu_unfold);
				}
			}
		});

		addContactBtn = (ImageButton) findViewById(R.id.addSearchBtn);
		addContactBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(HomeContactActivity.this, Search.class);
				HomeContactActivity.this.startActivity(i);
			}
		});

		fab = (FabToolbar) findViewById(R.id.fab);

		/*
		 * 设置密码
		 */
		findViewById(R.id.event).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fab.hide();
				Intent i = new Intent(HomeContactActivity.this, SetPasswordActivity.class);
				HomeContactActivity.this.startActivity(i);
			}
		});

		/*
		 * 关闭程序
		 */
		findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fab.hide();
				Intent i = new Intent(HomeContactActivity.this, SendEmailActivity.class);
				HomeContactActivity.this.startActivity(i);
			}
		});

		/*
		 * 软件更新
		 */
		findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fab.hide();
				Intent i = new Intent(HomeContactActivity.this, UpdateActivity.class);
				HomeContactActivity.this.startActivity(i);
			}
		});
		fab.attachToListView(personList, null, new OnScrollListener() {
			/**
			 * 滚动状态改变时调用
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 不滚动时保存当前滚动到的位置
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (mDialogText.getVisibility() == View.VISIBLE) {
						mDialogText.setVisibility(View.INVISIBLE);
					}
				} else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					mDialogText.setVisibility(View.VISIBLE);
					fab.hide();
				}
			}

			/**
			 * 滚动时调用
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				ContactBean cb = (ContactBean) adapter.getItem(firstVisibleItem);
				String letters = adapter.getAlpha(cb.getPinyin());
				// mDialogText.setVisibility(View.VISIBLE);
				mDialogText.setText(letters);
			}
		});

	}

	private void init() {
		list = Contact8Activity.list;
		setAdapter(list);// 把联系人信息绑定到listview

		resideMenu = new ResideMenu(this);
		resideMenu.setUse3D(true);
		resideMenu.setBackground(R.drawable.group_back);
		resideMenu.attachToActivity(this);
		resideMenu.setShadowVisible(false);
		// valid scale factor is between 0.0f and 1.0f. leftmenu'width is
		// 150dip.
		resideMenu.setScaleValue(0.6f);
		ALLitem = GetGroupBean();
		for (int i = 0; i < ALLitem.size(); i++) {
			resideMenu.addMenuItem(ALLitem.get(i).getitemHome(), ResideMenu.DIRECTION_LEFT);
		}

		ALLitemR = GetGroupBean1();
		for (int i = 0; i < ALLitemR.size(); i++) {
			resideMenu.addMenuItem(ALLitemR.get(i).getitemHome(), ResideMenu.DIRECTION_RIGHT);
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (resideMenu.isOpened() == true)
				this.resideMenu.closeMenu();
			else {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.about) {
			ImageView content = (ImageView) getLayoutInflater().inflate(R.layout.about_view, null);
			new AlertDialog.Builder(this).setTitle(R.string.about).setView(content).setInverseBackgroundForced(true)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							//setDBAlph();
						}
					}).create().show();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 重写此方法将触控事件优先分发给GestureDetector，以解决滑动ListView无法切换屏幕的问题、
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return resideMenu.dispatchTouchEvent(ev);
	}

	private void setAdapter(List<ContactBean> list) {
		adapter = new ContactHomeAdapter(this, list, alpha);// 把联系人信息绑定到listview
		personList.setAdapter(adapter);
		alpha.init(HomeContactActivity.this.acbuwaPage);// 初始化字母栏所在的view
		alpha.setListView(personList);
		alpha.setHight(alpha.getHeight());
		alpha.setVisibility(View.VISIBLE);

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

	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信", "查看详细", "修改信息", "删除此人", "添加到本地" };

	// 联系人操作弹出页
	private void showContactDialog(final String[] arg, final ContactBean cb, final int position) {
		new AlertDialog.Builder(this).setTitle(cb.getDisplayName() + " " + cb.getPhoneNum())
				.setIcon(R.drawable.add_contact).setItems(arg, new DialogInterface.OnClickListener() {
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

							member = cb;
							Intent intent = new Intent(HomeContactActivity.this, MessageBoxList.class);
							HomeContactActivity.this.startActivity(intent);
							break;

						case 2:// 查看详细

							member = cb;
							Intent i = new Intent(HomeContactActivity.this, memberinfo.class);
							// i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//注意本行的FLAG设置
							HomeContactActivity.this.startActivity(i);

							break;

						case 3:// 修改联系人资料

							UpdateContact(cb, position);
							break;

						case 4:// 删除

							showDelete(cb, position);
							break;

						case 5:// 添加到本地

							uri = Uri.parse("content://com.android.contacts/raw_contacts");
							ContentResolver resolver = HomeContactActivity.this.getContentResolver();
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
							Toast.makeText(HomeContactActivity.this, "添加成功.", Toast.LENGTH_SHORT).show();

							break;

						}
					}
				}).show();
	}
	
	private void UpdateContact(final ContactBean cb, final int position) {
		final EditText text = new EditText(this);
		text.setInputType(InputType.TYPE_CLASS_PHONE);
		text.setText(cb.getPhoneNum());
		text.requestFocus();
		new AlertDialog.Builder(this).setTitle("更新联系人电话").setIcon(R.drawable.contact_app).setView(text)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						SQLiteDatabase db = Contact8Activity.dbHelper.getDatabase();
						ContentValues cv = new ContentValues();
						cv.put("telephone", text.getText().toString().trim());
						db.update("db", cv, "num = ?", new String[] { cb.getXuehao() });
						ContactBean cb_tem = cb;
						cb_tem.setPhoneNum(text.getText().toString().trim());
						adapter.update(position, cb_tem);
						adapter.notifyDataSetChanged();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
						Toast.makeText(HomeContactActivity.this, "更新成功.", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
					}
				}).show();
	}

	// 删除联系人方法
	private void showDelete(final ContactBean cb, final int position) {

		new AlertDialog.Builder(this).setIcon(R.drawable.contact_app).setTitle("是否删除此联系人")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						SQLiteDatabase db = Contact8Activity.dbHelper.getDatabase();
						db.delete("db", "num = ?", new String[] { cb.getXuehao() });

						adapter.remove(position);
						adapter.notifyDataSetChanged();
						Toast.makeText(HomeContactActivity.this, "联系人已删除.", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();
	}

	/**
	 * 
	 * 初始化群组信息 返回值List<ResideMenuItem>
	 */
	public List<GroupBean> GetGroupBean() {

		List<GroupBean> item = new ArrayList<GroupBean>();

		String[] cg_name = new String[] { "全部", "2013级", "2014级", "2015级", "2016级", "2017级", "2018级" };
		int[] cg_icon = new int[] { R.drawable.icon_home, R.drawable.icon_profile, R.drawable.icon_profile,
				R.drawable.icon_profile, R.drawable.icon_profile, R.drawable.icon_profile, R.drawable.icon_profile };

		for (int i = 0; i < cg_name.length; i++) {
			GroupBean gb = new GroupBean();
			ResideMenuItem itemHome = new ResideMenuItem(this, cg_icon[i], cg_name[i]);
			itemHome.setOnClickListener(this);
			gb.setId(cg_icon[i]);
			gb.setName(cg_name[i]);
			gb.setitemHome(itemHome);
			item.add(gb);
		}
		return item;
	}

	private void queryGroupMember(GroupBean gb, GroupBean gb1) {

		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		list = new ArrayList<ContactBean>();
		Cursor c = null;
		if ( gb !=null && gb1 != null) {
			c = database.rawQuery("SELECT * FROM db WHERE danwei like ? and sex like ?", new String[] { "%" + gb.getName() + "%", "%" + gb1.getName() + "%" });			
		}else if(gb !=null && gb1 == null){
			c = database.rawQuery("SELECT * FROM db WHERE danwei like ?", new String[] { "%" + gb.getName() + "%" });
		}else if(gb == null && gb1 != null){
			c = database.rawQuery("SELECT * FROM db WHERE sex like ?", new String[] { "%" + gb1.getName() + "%" });
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

	public List<GroupBean> GetGroupBean1() {

		List<GroupBean> item = new ArrayList<GroupBean>();

		String[] cg_name = new String[] { "男", "女", "翻牌子" };
		int[] cg_icon = new int[] { R.drawable.icon_man, R.drawable.icon_woman, R.drawable.icon_woman};

		for (int i = 0; i < cg_name.length; i++) {
			GroupBean gb = new GroupBean();
			ResideMenuItem itemHome = new ResideMenuItem(this, cg_icon[i], cg_name[i]);
			itemHome.setOnClickListener(this);
			gb.setId(cg_icon[i]);
			gb.setName(cg_name[i]);
			gb.setitemHome(itemHome);
			item.add(gb);
		}
		return item;
	}

/*	private void queryGroupMember1(GroupBean gb) {

		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		list = new ArrayList<ContactBean>();
		Cursor c = database.rawQuery("SELECT * FROM db WHERE sex like ?", new String[] { "%" + gb.getName() + "%" });
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
	}*/

	protected void onDestroy() {
		super.onDestroy();
		Contact8Activity.dbHelper.closeDatabase();
	}
	
/*	private void setDBAlph(){
		SQLiteDatabase database = Contact8Activity.dbHelper.getDatabase();
		List<ContactBean> list_tem = Contact8Activity.list;
		for(int i = 0; i < list_tem.size(); i++){
			ContactBean cb = list_tem.get(i);
			ContentValues cv = new ContentValues();
			cv.put("pinyin", cb.getPinyin());
			database.update("db", cv, "num = ?", new String[] { cb.getXuehao() });
		}

	}*/

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		for (int i = 0; i < ALLitem.size(); i++) {
			if (v == ALLitem.get(i).getitemHome()) {
				if (ALLitem.get(i).getName().equals("全部")) {
					list = Contact8Activity.list;
					setAdapter(list);// 把联系人信息绑定到listview
					resideMenu.closeMenu();
					resideMenuIndexL = resideMenuIndexR = -1;
					// menuBtn.setBackgroundResource(R.drawable.menu_unfold);
					break;
				} else {
					resideMenuIndexL = i;
					if (resideMenuIndexR != -1) {
						queryGroupMember(ALLitem.get(resideMenuIndexL),ALLitemR.get(resideMenuIndexR));	
						//resideMenuIndexR = -1;
					}else {
						queryGroupMember(ALLitem.get(resideMenuIndexL), null);
					}
					resideMenu.closeMenu();
					// menuBtn.setBackgroundResource(R.drawable.menu_unfold);
					break;
				}
			} else if (i < 2) {
				if (v == ALLitemR.get(i).getitemHome()) {
					resideMenuIndexR = i;
					if (resideMenuIndexL != -1) {
						queryGroupMember(ALLitem.get(resideMenuIndexL),ALLitemR.get(resideMenuIndexR));
						//resideMenuIndexL = -1;
					}else {
						queryGroupMember(null, ALLitemR.get(resideMenuIndexR));
					}
					//queryGroupMember1(ALLitemR.get(i));
					resideMenu.closeMenu();
					// menuBtn.setBackgroundResource(R.drawable.menu_unfold);
					break;
				}
			}
		}
	}
}
