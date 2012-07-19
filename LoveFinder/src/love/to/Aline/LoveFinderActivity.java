package love.to.Aline;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoveFinderActivity extends Activity {
	
	public ConnectServer mConnectServer; // Model	
	final public Messenger BackgroundMessager = new Messenger(new inComingHandler());
	public static final String PREFS_NAME = "LoveFinder";
	private SharedPreferences settings;
	
	public String account = "None";
	private String password = "None";
	
// In this project, we will use the Controller-View-Model framework. 
// View Can only be modified by Controller
// Model can be modified by View and Controller at the same time.
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        if(!isOnline()){
        	ToastString("Please check your Internet Connection and Restart the LoveFinder", "long");
        }
        else{
	        mConnectServer = new ConnectServer(new Handler(){
	        	public void handleMessage(Message msg){}
	        });
	        
	        
	        settings = getSharedPreferences(PREFS_NAME, 0);
	        ///ToastString(settings.toString());
	        
	        account = settings.getString("account", "None");
	        
	        if (account == "None"){
	        }else{
	        	//ToastString("account not empty");
	        	password = settings.getString(account, "None");
	        	boolean AccExist = verify(account,password); // might run in background
	        	if (AccExist){
	        		startGoogleMap(account);
	        	}
	        }
	
	        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);   
	        btnConfirm.setOnClickListener(new OnClickListener(){
	        	public void onClick(View view){
	        		EditText EDacount = (EditText) findViewById(R.id.account);
	        		String accountInput = "None";
	        		if(EDacount.getText().length() != 0){
	        			
	        			accountInput = EDacount.getText().toString();
	        		
	        			EditText EDpassword = (EditText) findViewById(R.id.password);
	            		EDpassword.setTransformationMethod(new PasswordTransformationMethod());
	            		String passwordInput = "None";
	            		if(EDpassword.getText().length() != 0){
	            			passwordInput = EDpassword.getText().toString();
	            			if(verify(accountInput,passwordInput)){
	                			account = accountInput;
	                			saveAccountInfo(account,passwordInput);
	                			startGoogleMap(account);
	                		}else{
	                			ToastString("Invalid Account or password. You are currently not registered as a friend of Aline","long");
	                		}
	            			
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
    	String[] para = mConnectServer.getInfo(account);
		String ID = para[0];
		Intent intent = new Intent(LoveFinderActivity.this, GoogleMapViewActivity.class);
		intent.putExtra("ID", ID);
		intent.putExtra("account", account);
		startActivity(intent);
    }
    
    private boolean verify(String account, String password){
    	if (account.equals("None") || password.equals("None"))
    		return false;
    	else
    		ToastString("Verifying","short");
    		return mConnectServer.verify(account, password);
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
    
}