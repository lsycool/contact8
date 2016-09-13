package com.lsy.namespace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.lsy.namespace.R;
 
public class DBManager {
     
    private final int BUFFER_SIZE = 10*1024*1024;
/*    public static final String DB_NAME = "mydatabase.db"; //��������ݿ��ļ���
    public static final String PACKAGE_NAME = "com.lsy.namespace";
    public static final String DB_PATH = "/data"+ Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME;  //���ֻ��������ݿ��λ��(/data/data/com.cssystem.activity/cssystem.db)
*/     
     
    private SQLiteDatabase database;
    private Context context;
 
    public DBManager(Context context) {
        this.context = context;
    }
 
    public SQLiteDatabase getDatabase() {
        return database;
    }
 
    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }
 
    public void openDatabase() {
//    	this.database = this.openDatabase(DB_PATH + "/" + DB_NAME); 
    	String path = context.getFilesDir().getParent() + "/mydatabase.db";
    	this.database = this.openDatabase(path);
//    	this.database = this.openDatabase(context.getDatabasePath("mydatabase.db").getPath());    		
    }
 
    private SQLiteDatabase openDatabase(String dbfile) {
    	
    	SQLiteDatabase db = null;
        try {
            if (!(new File(dbfile).exists())) {
                //�ж����ݿ��ļ��Ƿ���ڣ�����������ִ�е��룬����ֱ�Ӵ����ݿ�
                InputStream is = this.context.getResources().openRawResource(R.raw.mydatabase); //����������ݿ�
                FileOutputStream fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            db = SQLiteDatabase.openOrCreateDatabase(dbfile,null);
        } catch (FileNotFoundException e) {
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return db;
    }
    
    public void update(Person person) {
		ContentValues cv = new ContentValues();
		cv.put("name", person.name);
		cv.put("sex", person.sex);
		cv.put("num", person.xuehao);
		cv.put("danwei", person.leibie);
		cv.put("speciality", person.zhuanye);
		cv.put("telephone", person.dianhua);
		cv.put("jiguan", person.jiguan);
		cv.put("danren", person.gugan);
		//new String[]{person.name,person.sex,person.xuehao,person.leibie,person.zhuanye,person.dianhua,person.jiguan,person.gugan}
		database.update("db", cv, "num = ?", new String[]{person.xuehao});
    }

	/**
	 * delete old person
	 * @param person
	 */
	public void deleteOldPerson(Person person) {
		database.delete("db", "num = ?", new String[]{person.xuehao});
	}

	/**
	 * add persons
	 * @param persons
	 */
	public void add(List<Person> persons) {
        database.beginTransaction();	//��ʼ����
        try {
        	for (Person person : persons) {
        		database.execSQL("INSERT INTO db VALUES(null, ?, ?, ?)", new Object[]{person.name,person.sex,person.xuehao,person.leibie,person.zhuanye,person.dianhua,person.jiguan,person.gugan});
        	}
        	database.setTransactionSuccessful();	//��������ɹ����
        } finally {
        	database.endTransaction();	//��������
        }
	}
	
	/**
	 * query all persons, return list
	 * @return List<Person>
	 */
	public List<Person> query() {
		ArrayList<Person> persons = new ArrayList<Person>();
		Cursor c = queryTheCursor();
        while (c.moveToNext()) {
        	Person person = new Person();
        	person.name = c.getString(c.getColumnIndex("name"));
        	person.sex = c.getString(c.getColumnIndex("sex"));
        	person.xuehao = c.getString(c.getColumnIndex("num"));
        	person.jiguan = c.getString(c.getColumnIndex("jiguan"));
        	person.leibie = c.getString(c.getColumnIndex("danwei"));
        	person.zhuanye = c.getString(c.getColumnIndex("speciality"));
        	person.dianhua = c.getString(c.getColumnIndex("telephone"));
        	person.gugan = c.getString(c.getColumnIndex("danren"));         	
        	persons.add(person);
        }
        c.close();
        return persons;
	}
	
	/**
	 * query all persons, return cursor
	 * @return	Cursor
	 */
	public Cursor queryTheCursor() {
        Cursor c = database.rawQuery("SELECT * FROM db", null);
        return c;
	}
     
    public void closeDatabase() {
        this.database.close();
 
    }
}
