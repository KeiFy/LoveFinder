package love.to.Aline;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class LoveFinderApp extends Application {
	
	private static final String TAG = LoveFinderApp.class.getSimpleName();
	private static LoveFinderApp instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "LoveFinderApp.onCreate was called");
		instance = this;
	}
	
	public static Context getContext() {
		return instance.getApplicationContext();
	}
}
