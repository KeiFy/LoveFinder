package love.to.Aline.utils;

import java.util.ArrayList;

import love.to.Aline.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class MyItemizedOverlay extends ItemizedOverlay {

	// Items that we want to Overlay
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	
	public MyItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	public MyItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
	}
	
	// Add an item populate -> createItem(int) to retrieve OverlayItem so that system is prepared to draw them
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void removeOverlay(OverlayItem overlay) {
        mOverlays.remove(overlay);
        populate();
    }
	
	public void clear() {
        mOverlays.clear();
        populate();
    }
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
		
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
//	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//	  dialog.setTitle(item.getTitle());
//	  dialog.setContentView(R.layout.maindialog);
//	  dialog.setMessage(item.getSnippet());
//	  dialog.show();
//	  
	  Dialog dialog = new Dialog(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setCancelable(true);
	  dialog.setContentView(R.layout.maindialog);
	  
	  String snippet = item.getSnippet();
	  String delims = "`";
	  String[] para = snippet.split(delims);
	  
	  TextView text = (TextView) dialog.findViewById(R.id.TextView01);
      	text.setText(para[0]);
      TextView name = (TextView) dialog.findViewById(R.id.dialogName);
      	name.setText(para[2]);
      	
      	
      TextView addressname = (TextView) dialog.findViewById(R.id.dialogaddress);
      	addressname.setText("@ " + para[3]);
	  
      ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);

      int imageSource = Integer.parseInt(para[1]);
      img.setImageResource(imageSource);
      
      dialog.show();
      	
	  return true;
	}


}
