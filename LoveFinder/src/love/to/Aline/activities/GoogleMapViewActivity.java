package love.to.Aline.activities;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import love.to.Aline.R;
import love.to.Aline.activities.BackgroundService.BackgroundBinder;
import love.to.Aline.daos.ConnectServer;
import love.to.Aline.utils.MyItemizedOverlay;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapViewActivity extends MapActivity {
	
	
	@SuppressWarnings("unused")
	private static final String TAG = GoogleMapViewActivity.class.getSimpleName();

	final public Messenger BackgroundMessager = new Messenger(new inComingHandler());
	public ConnectServer mConnectServer;
	public List<Overlay> mapOverlays;
	private boolean isBound = false;
	private boolean isStarted = false;
	public BackgroundService mBackgroundService = null;
	
	public int ID;
	public MapController mMapController;
	public List<GeoPoint> GeoList; // Used to aim to different locations on the map

	public String account;
	private Geocoder mGeocoder = null;
	private String address = null; 
	
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.googlemapview);
	    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
	    /*
	    	Show a loading for loading time 
	    */
	    
	    
	    Log.d(TAG, "Google Map OnCreated Called");

	   
	    
	    
	    
	    Intent passedIntent = getIntent();
	    account = passedIntent.getStringExtra("account");// get the user name of this app
	    
        mConnectServer =  new ConnectServer(new Handler(){
        	public void handleMessage(Message msg){
        	}
        });
        
        new Thread (new Runnable(){
			public void run() {
				String[] para = mConnectServer.getInfo(account);
				ID = Integer.parseInt(para[0]);
			}		    
	    });  
        
        setUpIDs();
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapOverlays = mapView.getOverlays();
	    mMapController = mapView.getController();
	    
	    GeoList = new ArrayList<GeoPoint>();
	    GeoList.add(new GeoPoint(0,0));
	    GeoList.add(new GeoPoint(0,0));
	    
	    
	}
	
	protected void onResume() {
		super.onResume();
		//Toast.makeText(this, "OnResume called", Toast.LENGTH_SHORT).show();
		final Intent intent = new Intent(this, BackgroundService.class);
		// Create a new Messenger for the communication back
		// From the Service to the Activity
		intent.putExtra("MESSENGER", BackgroundMessager);
		Log.i("MESSAGE", "Google to bind activity");
		
		new Thread(new Runnable(){
			public void run() {
				bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
			}
			
		}).start();
		
		
		mGeocoder = new Geocoder(this);
	}
	
	protected void onPause() {
		super.onPause();
		unbindService(mServiceConnection);
	}
	
	class inComingHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			ToastString("Map Recieve Information");
			
			if(msg.getData().getBoolean("clear")){
				mapOverlays.clear();
				return;// how can I just store the overlay
			}
			String[] perInfo = msg.getData().getStringArray("info");
			Log.i("Handling","Overlaying Begins " + perInfo[0].toString());
			createOverlay(perInfo);		
		}
	}
	
	private void ToastString(String info){
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}
	
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater(); // popup menu
    	inflater.inflate(R.menu.menu, menu); // find the location of the menu
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.changeWords :
    		promotInput();
    		return true;
    	case R.id.aline:
    		mMapController.animateTo(GeoList.get(1));
    		mMapController.setZoom(15);
    		return true;   	
	    case R.id.feiy:
			mMapController.animateTo(GeoList.get(0));
			mMapController.setZoom(15);
			return true;
		}
    	return false;
    }
	

	
	private String promotInput() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Update Status");
		alert.setMessage("What's on your Mind? My Love");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String newState = input.getText().toString();
		  mConnectServer.modifyData("State", newState, account);
		  //ToastString(newState);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		return null;
	}

	private void createOverlay(String[] perInfo){
		
		// id ; account ; Date ; Time ; State ; lattitude ; longtitude
		int id = m_idMap.get(perInfo[0]).intValue();
		Drawable drawable = this.getResources().getDrawable(id);
		MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable, this);	
		
		List<Address> mAddress = null;
	    try {
	    	double La = (double)(Integer.parseInt(perInfo[5]))/1000000;
	    	double Lo = (double)(Integer.parseInt(perInfo[6]))/1000000;
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
	    
	    GeoPoint point = new GeoPoint(Integer.parseInt(perInfo[5]),Integer.parseInt(perInfo[6]));
	    OverlayItem overlayitem = new OverlayItem(point, "Last Active: "  + perInfo[2] + " " + perInfo[3] , perInfo[4] + '`' + id + '`' + perInfo[1] + '`' + address);
	    
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	    GeoList.set(Integer.parseInt(perInfo[0])-1,point);
	}
	
	
	private Map<String, Integer> m_idMap = new HashMap<String, Integer>() ;
	private void setUpIDs()
	{
		m_idMap.put( "1", new Integer(R.drawable.feiyu) ) ; // feiyu
		m_idMap.put( "2", new Integer(R.drawable.ruoyuwang) ) ; // ruoyuwang
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			BackgroundBinder binder = (BackgroundBinder) service;
			mBackgroundService = binder.getService();
			isBound = true;
			Log.i("MESSAGE", "Connecting Background Service");
			isStarted = mBackgroundService.getStarted();
			mBackgroundService.startRun(account);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			mBackgroundService.stopRun();
			isStarted = false;
			isBound = false;
		}
	};
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	Intent intent = new Intent();
        	setResult(Activity.RESULT_OK, intent);
        	finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
}