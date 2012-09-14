package love.to.Aline.daos;

import love.to.Aline.LoveFinderApp;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class ImageLinkDatabaseHelper extends SQLiteOpenHelper {

	@SuppressWarnings("unused")
	private static final String TAG = ImageLinkDatabaseHelper.class.getSimpleName();
	private static final String DATABASE_NAME = "LoveFinderApp";
	private static final int DATABASE_VERSION = 1;
	
	public ImageLinkDatabaseHelper() {
		super(LoveFinderApp.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) { // Database created when initializing projectVo

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// first iteration. do nothing.
	}
}