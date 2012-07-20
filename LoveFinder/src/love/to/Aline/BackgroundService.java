package love.to.Aline;

import java.io.IOException;
import java.util.List;

import love.to.Aline.dao.ConnectServer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BackgroundService extends Service { 
	
	public final long DELAY_MS = 10000 ;
	public final String Lattitude = "tagReport";
	public final String TICKER_TEXT = "Lab 6";
	public final String NOTIFICATION_TITLE = "Location Updated";
	
	public Context mContext;
	public IBinder mBinder;
	public boolean isStarted;
	public int mMsgCount;
	public int sizeOfTable = 0; 
	
	public LocationManager mLocationManager;
	private Messenger GoogleMapMessenger = null;
	private Location currentBest = null;
	public ConnectServer mConnectServer;
	public String account = null;
	
	private Geocoder mGeocoder;
	
	
	public void onCreate() {
		super.onCreate();
		mBinder = new BackgroundBinder();
		isStarted = false;
		mMsgCount = 0;
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mContext = getApplicationContext();
		
		mConnectServer =  new ConnectServer(new Handler(){
        	public void handleMessage(Message msg){
        		//String accnout = msg.getData().getString(ConnectServer.ACCOUNT);
        		//TODO handler
        	}
        });
	}
	
	private void sendClear(){
		Message msg = Message.obtain();
		try {
			Log.i("sending","clear");
			Bundle bundle = new Bundle();
			bundle.putBoolean("clear", true);
			msg.setData(bundle);
			GoogleMapMessenger.send(msg);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPerInfo(String[] perInfo) {
		Message msg = Message.obtain();
		try {
			Log.i("sending","sending user information");
			Bundle bundle = new Bundle();
			bundle.putStringArray("info", perInfo);
			bundle.putBoolean("clear", false);
			msg.setData(bundle);
			GoogleMapMessenger.send(msg);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

	private boolean checkValid(String[] perInfo){
		int i = 0;
		
		while(i<8){
			if (perInfo[i] == null)
				return false;
			i++;
		}
		return true;
	}
	
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		// Get messager from the Activity
		Log.i("MESSAGE", "binding Service");
		if (extras != null) {
			GoogleMapMessenger = (Messenger) extras.get("MESSENGER");
			Log.i("MESSAGE", "Service Binded " + GoogleMapMessenger.toString());
		}
		return mBinder;
	}
	
	public void onDestroy() {
		super.onDestroy();
		stopRun();
	}
	
	
	public void startRun(String InAccount) {
		
		/* depreciated
		
		*/
		mGeocoder = new Geocoder(this);
		if (isStarted)
			return;
		account = InAccount;
		isStarted = true;
		Log.i("Background Service","Service Running");
		sizeOfTable = mConnectServer.getSqlSize();
        for (int i = 1 ; i <=sizeOfTable; i++){
        	String[] perInfo =  mConnectServer.getInfo(i);
        	if (checkValid(perInfo))
        		sendPerInfo(perInfo);
        }
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		Location lastKnownLocation = null;
		lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (lastKnownLocation != null && isBetterLocation(lastKnownLocation,currentBest))
		{
			int intLatitude = (int) (lastKnownLocation.getLatitude()*1000000);
			int intLongitude= (int) (lastKnownLocation.getLongitude()*1000000);
			Log.i("MESSAGE", "Modifying information");		
			
			String address = null;
			List<Address> mAddress = null;
		    try {
		    	double La = lastKnownLocation.getLatitude();
		    	double Lo = lastKnownLocation.getLongitude();
				mAddress = mGeocoder.getFromLocation(La,Lo,1);
			} catch (NumberFormatException e) {
				Log.e("Location format Error", "Geocoder get the wrong format of input");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("Geocoder", "Geocoder IO Exception Error");
				e.printStackTrace();
			}
		    if (mAddress.size() != 0){
		    	address = mAddress.get(0).getAddressLine(0) + " " + mAddress.get(0).getAddressLine(1);
		    	//ToastString(address);
		    }
		    Log.d("address", address);
		    mConnectServer.modifyData("latitude", intLatitude, account);
			mConnectServer.modifyData("longitude",intLongitude, account);
			mConnectServer.modifyData("address",address, account);
			
			sendClear();
			for (int i = 1 ; i <=sizeOfTable; i++){
	        	String[] perInfo =  mConnectServer.getInfo(i);
	        	
	        	if (checkValid(perInfo))
	        		sendPerInfo(perInfo);
			}
//			String[] perInfo =  mConnectServer.getInfo(1);
//			sendPerInfo(perInfo);
		}
		
	}
	
	public void stopRun() {
		if (!isStarted)
		return;
		isStarted = false;
		mLocationManager.removeUpdates(mLocationListener);
	}
	
	public boolean getStarted() {
		return isStarted;
	}
	
	public class BackgroundBinder extends Binder {
		public BackgroundService getService() {
			Log.i("MESSAGE", "Returning Binder with Messenger " + GoogleMapMessenger.toString());
			return BackgroundService.this;
		}
	}
	
	private LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) { // How to define location change? Read Google Doc
			if(isBetterLocation(location, currentBest) && location.getAccuracy()<60){
				int intLatitude = (int) (location.getLatitude()*1000000);
				int intLongitude = (int) (location.getLongitude()*1000000);
				Log.i("MESSAGE", "Modifying information");
				
				String address = null;
				List<Address> mAddress = null;
			    try {
			    	double La = location.getLatitude();
			    	double Lo = location.getLongitude();
					mAddress = mGeocoder.getFromLocation(La,Lo,1);
				} catch (NumberFormatException e) {
					Log.e("Location format Error", "Geocoder get the wrong format of input");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("Geocoder", "Geocoder IO Exception Error");
					e.printStackTrace();
				}
			    if (mAddress.size() != 0){
			    	address = mAddress.get(0).getAddressLine(0) + " " + mAddress.get(0).getAddressLine(1);
			    	//ToastString(address);
			    }
				
				mConnectServer.modifyData("latitude", intLatitude, account);
				mConnectServer.modifyData("longitude",intLongitude, account);
				mConnectServer.modifyData("address",address, account);
				
				sendClear();
				for (int i = 1 ; i <=sizeOfTable; i++){
		        	String[] perInfo =  mConnectServer.getInfo(i);
		        	if (checkValid(perInfo))
		        		sendPerInfo(perInfo);
				}
//				String[] perInfo =  mConnectServer.getInfo(1);
//				sendPerInfo(perInfo);
				
//				Message msg = Message.obtain();
//				try {
//					Log.i("OnLocation Chnage","current location" + location.toString());
//					Bundle bundle = new Bundle();
//					//if(location.getLatitude() != null && location.getLongitude() != null){
//					int intLatitude = (int) (location.getLatitude()*1000000);
//					int intLongitude = (int) (location.getLongitude()*1000000);
//					
//						Log.i("presending Info","current Location " + Integer.toString(intLatitude) + " " + Integer.toString(intLongitude) );
//						bundle.putInt("latitude", intLatitude);
//						bundle.putInt("longitude", intLongitude);
//						msg.setData(bundle);
//						GoogleMapMessenger.send(msg);
//					//}
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
			}
		}
		public void onProviderDisabled(String provider) { }
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
	};
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	
}
