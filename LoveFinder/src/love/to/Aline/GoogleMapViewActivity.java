package love.to.Aline;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import love.to.Aline.BackgroundService.BackgroundBinder;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapViewActivity extends MapActivity {

	final public Messenger BackgroundMessager = new Messenger(new inComingHandler());
	public ConnectServer mConnectServer;
	public List<Overlay> mapOverlays;
	private boolean isBound = false;
	private boolean isStarted = false;
	public BackgroundService mBackgroundService = null;
	public int ID;
	public MapController mMapController;
	public List<GeoPoint> GeoList;

	public String account;
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.googlemapview);
	    
	    Intent passedIntent = getIntent();
	    ID = Integer.parseInt(passedIntent.getStringExtra("ID"));
	    account = passedIntent.getStringExtra("account");// get the user name of this app
	    
        mConnectServer =  new ConnectServer(new Handler(){
        	public void handleMessage(Message msg){
        		//String accnout = msg.getData().getString(ConnectServer.ACCOUNT);
        		//TODO handler
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
		Intent intent = null;
		intent = new Intent(this, BackgroundService.class);
		// Create a new Messenger for the communication back
		// From the Service to the Activity
		intent.putExtra("MESSENGER", BackgroundMessager);
		Log.i("MESSAGE", "Google to bind activity");
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	protected void onPause() {
		super.onPause();
		unbindService(mServiceConnection);
	}
	
	class inComingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			ToastString("Map Recieve Information");
			
			if(msg.getData().getBoolean("clear")){
				mapOverlays.clear();
				return;// how can I just store the overlay
			}
			String[] perInfo = msg.getData().getStringArray("info");
			Log.i("Handling","Overlaying Begins" + perInfo[0].toString());
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
		//TODO a function that directly adding up all the overlay of people in the database
		
		// id ; account ; Date ; Time ; State ; lattitude ; longtitude
		int id = m_idMap.get(perInfo[0]).intValue();
		Drawable drawable = this.getResources().getDrawable(id);
		MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable, this);	   
	    
	    GeoPoint point = new GeoPoint(Integer.parseInt(perInfo[5]),Integer.parseInt(perInfo[6]));
	    OverlayItem overlayitem = new OverlayItem(point, "Last Active Time: " + perInfo[2] + " " + perInfo[3] , perInfo[4]);
	    
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	    GeoList.set(Integer.parseInt(perInfo[0])-1,point);
	}
	
	
	private Map<String, Integer> m_idMap = new HashMap<String, Integer>() ;
	private void setUpIDs()
	{
		m_idMap.put( "1", new Integer(R.drawable.fei) ) ; // feiyu
		m_idMap.put( "2", new Integer(R.drawable.aline) ) ; // ruoyuwang
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
	

	
}