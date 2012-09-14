package love.to.Aline.daos;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ImageLinkDao {
	
	@SuppressWarnings("unused")
	private static final String TAG = ImageLinkDao.class.getSimpleName();

	public static String TABLE = "image"; // need to set according to project 
	public static String IMAGE_ID = "img_id";
	public static String ACCOUNT = "account";
	public static String IMAGE_LOCATION = "img_location";
	public static String CURRENT = "current";
	
	
	/*
	 * Deadline ID is the same as the taskID since that every task could only have one deadline restriction
	 */
	
	
	public ImageLinkDao(String tableName) {
		// tells which table to modify		
		TABLE = tableName;
		createTableIfnull();
	}
	
	private void createTableIfnull() {
		SQLiteDatabase db = new ImageLinkDatabaseHelper().getWritableDatabase();
//		final String Query  = "SELECT name FROM sqlite_master WHERE type='table' AND name='" +  TABLE + "'";		
//		Cursor cursor = db.rawQuery(Query, null);
		
		//Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", TABLE});
	   
		if (!exists(TABLE))
	    { 
	    	// Table doesn't exists
	    	final String CreateQuery = "CREATE TABLE " + ImageLinkDao.TABLE + "(" +
    			ImageLinkDao.IMAGE_ID + " integer primary key, " +
    			ImageLinkDao.ACCOUNT + " text," +
    			ImageLinkDao.IMAGE_LOCATION + " text," +
    			ImageLinkDao.CURRENT + " int,) ";
    		db.execSQL(CreateQuery);
    		Log.i(TAG, " Table " + TABLE + " Created");
	    }
		db.close();
	}
	
	
	public boolean exists(String table) {
		SQLiteDatabase db = new ImageLinkDatabaseHelper().getWritableDatabase();
	    try {
	         db.rawQuery("SELECT * FROM " + table, null);
	         db.close();
	         return true;
	    } catch (SQLException e) {
	    	 db.close();
	         return false;
	    }
	}
	
	public void insert(int img_id, String account, String img_location){
		//TODO
		//also need to change the current
	}
		
	public boolean imgChanged(String account)
	{
		//TODO
		SQLiteDatabase db = new ImageLinkDatabaseHelper().getWritableDatabase();
		return true;
	}
	/*
	public int getImgId(String account) {
		SQLiteDatabase db = new ImageLinkDatabaseHelper().getWritableDatabase();
		
		Cursor cursor = db.query(TABLE, null, _DEADLINEID+"=?", new String[] {Integer.toString(id)}, null, null, null);
		DeadlineVo vo = null;
		if (cursor.moveToFirst()) {
			vo = new DeadlineVo();
			vo.setId(cursor.getInt(cursor.getColumnIndex(_DEADLINEID)));
			vo.setName(cursor.getString(cursor.getColumnIndex(DEADLINENAME)));
			vo.setRow(cursor.getInt(cursor.getColumnIndex(ROW)));
			vo.setTaskOne(cursor.getInt(cursor.getColumnIndex(TASKONE)));
			vo.setTaskTwo(cursor.getInt(cursor.getColumnIndex(TASKTWO)));
			vo.setValue(cursor.getInt(cursor.getColumnIndex(TIMELIMIT)));
		}
		
		cursor.close();
		db.close();
		return vo;
		
	}
	*/
	/*
	public int update(DeadlineVo deadlineVo) {
		SQLiteDatabase db = new DeadlineDatabaseHelper().getWritableDatabase();
		ContentValues values = new ContentValues();
		if (deadlineVo.getId() > 0) values.put(_DEADLINEID, deadlineVo.getId());
		values.put(DEADLINENAME, deadlineVo.getName());
		values.put(ROW, deadlineVo.getRow());
		values.put(TASKONE, deadlineVo.getTaskOne());
		values.put(TASKTWO, deadlineVo.getTaskTwo());
		values.put(TIMELIMIT, deadlineVo.getValue());
		
		int num = db.update(TABLE, values, _DEADLINEID + "=?", new String[]{Integer.toString(deadlineVo.getId())});
		db.close();
		return num;
	}
	
	public void delete(int id) {
		SQLiteDatabase db = new DeadlineDatabaseHelper().getWritableDatabase();
		db.delete(TABLE, _DEADLINEID+"=?", new String[]{Integer.toString(id)});
		db.close();
	}
	
	public void delete(DeadlineVo deadline) {
		delete(deadline.getId());
	}
	
	public void deleteAll() {
		SQLiteDatabase db = new DeadlineDatabaseHelper().getWritableDatabase();
		db.delete(TABLE, null, null);
		Log.i(TAG, "DeadLine Table Cleared");
		db.close();
	}
	*/
	
}
