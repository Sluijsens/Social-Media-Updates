package nl.bryan_slop.socialmediaupdates;

import android.app.Application;
import android.app.Activity;
import android.content.Context;

public class SMU_Application extends Application {
	private static Context mContext;
	public void onCreate() {
		super.onCreate();
        mContext = this;
	}

    public static Context getContext() {
        return mContext;
    }

	private Activity mCurrentActivity = null;
	public Activity getCurrentActivity(){
		return mCurrentActivity;
	}
	public void setCurrentActivity(Activity mCurrentActivity){
		this.mCurrentActivity = mCurrentActivity;
	}
	
}
