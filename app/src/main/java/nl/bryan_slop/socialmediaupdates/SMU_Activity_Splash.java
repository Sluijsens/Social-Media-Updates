package nl.bryan_slop.socialmediaupdates;

import android.os.Bundle;
import android.content.*;


public class SMU_Activity_Splash extends SMU_Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
		
		startActivity(new Intent(this, SMU_Activity_Updates.class));
		SMU_Activity_Splash.this.finish();
    }

}
