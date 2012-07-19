package love.to.Aline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.util.Log;

public class ConnectDriver {
	public String result;
    public InputStream is;
    public StringBuilder sb;
    public String responseBody;
    public boolean connected = false;
    public Handler mHandler;
    public final static String ID = "ID";
    private List<NameValuePair>  sendingPara;
    
	public ConnectDriver(Handler AmHandler){
		result = null;
	    is = null;
	    sb=null;
	    responseBody = null;
	    mHandler = AmHandler;  
	    sendingPara = new ArrayList<NameValuePair>();
	}
	
	// TODO how to know if lost connection and does'nt hang
	
	public String[] getInfo(int ID){ // get the information and store as an ArrayList
		
		resetSendingData();
		sendingPara.add(new BasicNameValuePair("option", "GetInfo"));
		sendingPara.add(new BasicNameValuePair("ID", Integer.toString(ID)));
		
		Log.i("MSG","finish setting sending data" + sendingPara);
		
		String serverOut = postServer();
		String delims = "`";
		
		String[] para = serverOut.split(delims);
		
		return para;
	}
	
	public String[] getInfo(String ID){ // get the information and store as an ArrayList
		
		resetSendingData();
		sendingPara.add(new BasicNameValuePair("option", "GetInfo"));
		sendingPara.add(new BasicNameValuePair("ID", ID));
		
		Log.i("MSG","finish setting sending data" + sendingPara);
		
		String serverOut = postServer();
		String delims = "`";
		
		String[] para = serverOut.split(delims);
		
		return para;
	}
	
	public boolean insertNew(String[] stringPara){
		resetSendingData();
		sendingPara.add(new BasicNameValuePair("option", "Insert"));
		sendingPara.add(new BasicNameValuePair("ID", stringPara[0]));
		sendingPara.add(new BasicNameValuePair("password", stringPara[1]));
		sendingPara.add(new BasicNameValuePair("Name", stringPara[2]));
		sendingPara.add(new BasicNameValuePair("Latitude", stringPara[3]));
		sendingPara.add(new BasicNameValuePair("Longitude", stringPara[4]));
		
		String boolBack = postServer();
		
		if(boolBack.charAt(0) == 'P')
			return true;
		else{
			Log.e("Msg", "Inserting New Fail " + boolBack);
			return false;
		}
	}
	
	public boolean verify(String ID, String password){
		resetSendingData();
		sendingPara.add(new BasicNameValuePair("option", "Verify"));
		sendingPara.add(new BasicNameValuePair("ID", ID));
		sendingPara.add(new BasicNameValuePair("password", password));
		
		String boolBack = postServer();
		if(boolBack.charAt(0) == 'P')
			return true;
		else{
			Log.e("Msg", "Verifying " + boolBack);
			return false;
		}
	}
	
	public boolean modifyData(String option, String value, String ID){
		resetSendingData();
		
		sendingPara.add(new BasicNameValuePair("option", "Modify"));
		sendingPara.add(new BasicNameValuePair("ID", ID));
		sendingPara.add(new BasicNameValuePair("column", option));
		sendingPara.add(new BasicNameValuePair(option, value));
		Log.i("Connect", value);
		String boolBack = postServer();
		if(boolBack.charAt(0) == 'P')
			return true;
		else{
			Log.e("Msg", "Modifying Fail " + boolBack);
			return false;
			}
		}
	
	public boolean modifyData(String option, int value, String ID){
		resetSendingData();
		
		sendingPara.add(new BasicNameValuePair("option", "Modify"));
		sendingPara.add(new BasicNameValuePair("ID", ID));
		sendingPara.add(new BasicNameValuePair("column", option));
		sendingPara.add(new BasicNameValuePair(option, Integer.toString(value))); // convert to string
		
		String boolBack = postServer();
		updateDateTime(ID);
		if(boolBack.charAt(0) == 'P')
			return true;
		else{
			Log.e("Msg", "Modifying Fail " + boolBack);
			return false;
		}
		
	}
	
	public boolean updateDateTime(String ID){
		resetSendingData();
		
		sendingPara.add(new BasicNameValuePair("option", "DateTime"));
		sendingPara.add(new BasicNameValuePair("ID", ID));
		
		String boolBack = postServer();
		if(boolBack.charAt(0) == 'P')
			return true;
		else{
			Log.e("Msg", "Modifying Fail " + boolBack);
			return false;
		}
	}
	
	public void resetSendingData(){
		if(sendingPara==null)
			sendingPara = new ArrayList<NameValuePair>();
		else
			sendingPara.clear();
	}
	
	public int getSqlSize(){
		resetSendingData();
		sendingPara.add(new BasicNameValuePair("option", "Size"));
		
		String turnBack = postServer();
		String delims = "`";
		String[] para = turnBack.split(delims);
		int size = 0;
		
		try{
			size = Integer.parseInt (para[0]);
		} catch(NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		
		return size;
	}
	
	public String[] getSelfInfo(){
		try{
	    	HttpClient httpclient = new DefaultHttpClient();
	    	HttpPost httppost = new HttpPost("http://ihome.ust.hk/~fyu/cgi-bin/	.php");
	    	httppost.setEntity(new UrlEncodedFormEntity(sendingPara,HTTP.UTF_8));
	    	HttpResponse response = httpclient.execute(httppost);
	    	HttpEntity entity = response.getEntity();
	    	is = entity.getContent();
	    	//responseBody = EntityUtils.toString(entity);
	
	    }catch(Exception e){
	    	Log.e("log_tag", "Error in http connection"+e.toString());
	    }
		try{
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
	    	sb = new StringBuilder();
	    	sb.append(reader.readLine() + "\n");
	    	String line="0";
	    	
	    	while ((line = reader.readLine()) != null) {
	    		sb.append(line + "\n");
	    	}
	    	
	    	is.close();
	    	result=sb.toString();
	    	
	    	}catch(Exception e){
	    		Log.e("log_tag", "Error converting result "+e.toString());
	    	}
	    Log.i("Returning", result);
	    String serverOut = postServer();
		String delims = "`";
		
		String[] para = serverOut.split(delims);
		
		return para;
	}
	
    private String postServer(){ // send the information stored in the class
	    try{
	    	HttpClient httpclient = new DefaultHttpClient();
	    	HttpPost httppost = new HttpPost("http://ihome.ust.hk/~fyu/cgi-bin/Customer.php");
	    	httppost.setEntity(new UrlEncodedFormEntity(sendingPara,HTTP.UTF_8));
	    	HttpResponse response = httpclient.execute(httppost);
	    	HttpEntity entity = response.getEntity();
	    	is = entity.getContent();
	    	//responseBody = EntityUtils.toString(entity);
	
	    }catch(Exception e){
	    	Log.e("log_tag", "Error in http connection"+e.toString());
	    }
	    
	    try{
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
	    	sb = new StringBuilder();
	    	sb.append(reader.readLine() + "\n");
	    	String line="0";
	    	
	    	while ((line = reader.readLine()) != null) {
	    		sb.append(line + "\n");
	    	}
	    	
	    	is.close();
	    	result=sb.toString();
	    	
	    	}catch(Exception e){
	    		Log.e("log_tag", "Error converting result "+e.toString());
	    	}
	    Log.i("Returning", result);
	    
	    return result;
    }
}
