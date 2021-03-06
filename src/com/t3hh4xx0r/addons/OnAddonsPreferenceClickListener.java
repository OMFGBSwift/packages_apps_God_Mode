package com.t3hh4xx0r.addons;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import com.t3hh4xx0r.addons.utils.Constants;
import com.t3hh4xx0r.addons.utils.Downloads;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;



import com.t3hh4xx0r.R;

public class OnAddonsPreferenceClickListener implements OnPreferenceClickListener {

	private boolean DBG = (false || Constants.FULL_DBG);
	private final String TAG = "OnNightlyPreferenceClick";
	private long mDManDLID;
	AddonsObject mAddons;
	int mPosition;
	Context mContext;
	private String externalStorageDir = "/mnt/sdcard/t3hh4xx0r/downloads";
	private String DOWNLOAD_DIR = externalStorageDir+ "/";
	private boolean mDownloading = false;
	
	public OnAddonsPreferenceClickListener(AddonsObject o, int position, Context context) {
		mAddons = o;
		mPosition = position;
		mContext = context;
	}
	
	@Override
	public boolean onPreferenceClick(Preference v) {
 		Log.d(TAG, v.getSummary().toString()  );
 		Log.d(TAG, v.getTitle().toString()  );

 		File check =  new File(externalStorageDir+ "/" + mAddons.getZipName());
 		if(!check.exists() && !mAddons.getIsMarketApp()) {	// Not a market app and not downloading
 		    DownloadManager dman = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
 		    File f = new File(externalStorageDir);
 		    if(!f.exists()) {
 			    f.mkdirs();
 			    Log.i(TAG, "File diretory does not exist, creating it");
 		    }
 		    f = null;
 		    f = new File(externalStorageDir+ "/" + mAddons.getZipName());
 		    Uri down = Uri.parse(mAddons.getURL());
 		    DownloadManager.Request req = new DownloadManager.Request(down);
 		    req.setShowRunningNotification(true);
 		    req.setVisibleInDownloadsUi(true);
 		    req.setDestinationUri(Uri.fromFile(f));	
 		    mDManDLID = dman.enqueue(req);
 		    mDownloading  = true;
 		    return true;
 		}
 		else if(!mAddons.getIsMarketApp()){ // Not a market app and downloading	
			URL url;
			URLConnection conexion;
			int lenghtOfFile = 0;

			try{
 				url = new URL(mAddons.getURL());
				conexion = url.openConnection();
				conexion.connect();
				if(DBG )Log.d(TAG,"Connection complete");
				lenghtOfFile = conexion.getContentLength();
			}
			catch(MalformedURLException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			if(lenghtOfFile != 0 && check.length() == lenghtOfFile){
				checkInstallable(Boolean.parseBoolean(mAddons.getInstallable()), mAddons.getZipName());
				mDownloading = false;
			}
			
			return true;
 		}
 		else if(mAddons.getIsMarketApp()){	// Market app, let the market resolve this
 			check = null;
			Log.d(TAG, "Is a market app - Launching market");
 			// If it is a market app create a broadcast that launches the 
 			// market. The string should already be a URL
 			// prefixed with market://
			Log.d(TAG, mAddons.getURL() );
			Intent marketApp = new Intent(Intent.ACTION_VIEW, Uri.parse(mAddons.getURL()));
			marketApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			try{
				mContext.startActivity(marketApp);
			}catch(ActivityNotFoundException e){
				e.printStackTrace();
				Log.d(TAG, "Maybe the market is not installed");
				noMarketAlertBox(mAddons.getName(), "Maybe you haven't installed the market");
			}

 			return true;
 			
 		}
		
		Log.d(TAG, "Could not resolve what to do");
		return false;
	}
	
	
	protected void checkInstallable(final boolean Installable, final String OUTPUT_NAME) {
	             Thread FlashThread = new Thread(){
	         	@Override
	            	public void run() {
	            	    File f = new File (DOWNLOAD_DIR + OUTPUT_NAME);
	           		if(f.exists()) {
		  		   if (Installable) {
		  			Downloads.installPackage(OUTPUT_NAME, mContext);
		  		   } else {
					Downloads.prepareFlash(mContext);
	       			   } 
	            	        }
			}
	            };
	            FlashThread.run();
        }

	protected void noMarketAlertBox(String title, String mymessage) {
	   new AlertDialog.Builder(mContext)
	      .setMessage(mymessage)
	      .setTitle(title)
	      .setCancelable(false)
	      .setPositiveButton("OK",
	      new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {}
		}) 
	    .show();
    }

    private void log(String message){		   
        if(DBG)Log.d(TAG, message);		  
	}
}  
