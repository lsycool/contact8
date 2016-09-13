package com.lsy.bean;

import com.lsy.uitl.HanziToPinyin;

public class ContactBean implements Comparable<ContactBean>{

	private String displayName;
	private String phoneNum;
	private int selected = 0;
	private String pinyin="";
	private String jiguan;
	private String gugan;
	private String weixing;
	private String danwei;
	private String sex;
	private String xuehao;
	private String zhuanye;
	private String leibie;
	private String photoname = "";
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		setPinyin(displayName);
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public int getSelected() {
		return selected;
	}
	public void setSelected(int selected) {
		this.selected = selected;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String zhongwen) {
		this.pinyin = HanziToPinyin.getPinYin(zhongwen);
	}
	public String getXuehao() {
		return xuehao;
	}
	public void setXuehao(String xuehao) {
		this.xuehao = xuehao;
		setPhotoname();
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getJiguan() {
		return jiguan;
	}
	public void setJiguan(String jiguan) {
		this.jiguan = jiguan;
	}
	public String getGugan() {
		return gugan;
	}
	public void setGugan(String gugan) {
		this.gugan = gugan;
	}
	public String getZhuanye() {
		return zhuanye;
	}
	public void setZhuanye(String zhuanye) {
		this.zhuanye = zhuanye;
	}
	public String getLeibie() {
		return leibie;
	}
	public void setLeibie(String leibie) {
		this.leibie = leibie;
	}
	public String getPhotoname() {
		return photoname;
	}
	public void setPhotoname() {
		this.photoname = xuehao+".jpg";
	}
	public String getweixing() {
		return weixing;
	}
	public void setweixing(String weixing) {
		this.weixing = weixing;
	}
	public String getdanwei() {
		return danwei;
	}
	public void setdanwei(String danwei) {
		this.danwei = danwei;
	}
	@Override
	public int compareTo(ContactBean another) {
		// TODO Auto-generated method stub
		return this.getPinyin().compareTo(another.getPinyin());
	}
}
