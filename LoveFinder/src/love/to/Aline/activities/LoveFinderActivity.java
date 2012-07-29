package love.to.Aline.activities;

import java.util.Observable;
import java.util.Observer;

import love.to.Aline.R;
import love.to.Aline.daos.ConnectServer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoveFinderActivity extends Activity implements Observer {
	
	public ConnectServer mConnectServer; // Model	
	final public Messenger BackgroundMessager = new Messenger(new inComingHandler());
	public static final String PREFS_NAME = "LoveFinder";
	private SharedPreferences settings;
	
	public String account = "None";
	private String password = "None";
	private final int GOOGLEMAP = 1;
	private ProgressDialog dialog;
	private boolean success = false;
	protected String passwordInput;
	protected String accountInput;
	
// In this project, we will use the Controller-View-Model framework. 
// View Can only be modified by Controller
// Model can be modified by View and Controller at the same time.
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        
        if(!isOnline()){
        	ToastString("Please check your Internet Connection and Restart the LoveFinder", "long");
        }
        else{
	        mConnectServer = new ConnectServer(new Handler(){
	        	public void handleMessage(Message msg){}
	        });
	        mConnectServer.addObserver(this);
	        
	        settings = getSharedPreferences(PREFS_NAME, 0);
	        ///ToastString(settings.toString());
	        
	        account = settings.getString("account", "None");
	        
	        if (account == "None"){
	        }else{
	        	//ToastString("account not empty");
	        	password = settings.getString(account, "None");
	        	boolean AccExist = true; //  = verify(account,password); // might run in background
	        	if (AccExist){
	        		startGoogleMap(account);
	        	}
	        }
	
	        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);   
	        btnConfirm.setOnClickListener(new OnClickListener(){
	        	public void onClick(View view){
	        		EditText EDacount = (EditText) findViewById(R.id.account);
	        		accountInput = "None";
	        		if(EDacount.getText().length() != 0){
	        			
	        			accountInput = EDacount.getText().toString();
	        		
	        			EditText EDpassword = (EditText) findViewById(R.id.password);
	            		EDpassword.setTransformationMethod(new PasswordTransformationMethod());
	            		passwordInput = "None";
	            		if(EDpassword.getText().length() != 0){
	            			passwordInput = EDpassword.getText().toString();
	            			verify(accountInput,passwordInput);
	            			runOnUiThread(new Runnable(){ 
	            				public void run() {
	            				    dialog = ProgressDialog.show(LoveFinderActivity.this, "", "Waiting Central Server to Handle", true);				
	            				}
	            		    });
	            			
	            		}else{
	            			ToastString("Please enter Password","short");
	            		}		
	        		}else{
	        				ToastString("Please enter Account","short");
	        				
	        		}
	        		
	        	}
	        });
        }
    }

    
    private void saveAccountInfo(String account, String password){
    	SharedPreferences.Editor editor = settings.edit();
    	editor.clear();
    	editor.putString("account", account);
    	editor.putString(account, password);
    	editor.commit();
    }
    
    private void ToastString(String info, String option){
    	if (option == "long")
    		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    	else
    		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}
    
    private void startGoogleMap(String account){	
		Intent intent = new Intent(LoveFinderActivity.this, GoogleMapViewActivity.class);
		intent.putExtra("account", account);
		startActivityForResult(intent,GOOGLEMAP);
    }
    
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
		//Log.i("Result Received", requestCode + data.toString());
		
		switch (requestCode) {
		case GOOGLEMAP:
			if (resultCode == Activity.RESULT_OK) {
				//ToastString("Return From getPer Activity");
				finish();
			}		
			break;
			default:
				Log.e("Result not correct"," " + requestCode);
				break;
		}
		
	}
    
    
    private boolean verify(final String account, final String password){
    	if (account.equals("None") || password.equals("None"))
    		return false;
    	else{
    		ToastString("Verifying","short");
    		new Thread (new Runnable(){

				public void run() {
					success = mConnectServer.verify(account, password);
				}
    			
    		}).start();
    		
    		return success;
    	}
    }
    
    private boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ActiveNetInfo = cm.getActiveNetworkInfo();
	    if(ActiveNetInfo == null)
	    	return false;
	    else
	    	return ActiveNetInfo.isConnectedOrConnecting();
	}
    
    
    public void onResume(){
    	super.onResume();
    }
    
    public void onPause(){
    	super.onPause();
    }
    
    
    
    class inComingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {	
		}
	}


    /**
     * Function called when Connect Server is finisheds
     */
	public void update(Observable arg0, Object arg1) {
		
		int r = (Integer) arg1;	
		if(r == 1)
		{
			// verification passed
			account = accountInput;
			saveAccountInfo(account,passwordInput);
			startGoogleMap(account);
		} else {
			ToastString("Invalid Account or password. You are currently not registered as a friend of Aline","long");
		}
		if(dialog != null && dialog.isShowing())
		{
			dialog.dismiss();
		}
		
	}
    
}