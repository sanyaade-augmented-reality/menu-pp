package srdes.menupp;

import android.app.Activity;
import android.os.Bundle;


public class AboutUs extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("AboutUs::onCreate");
		super.onCreate(savedInstanceState);
		
		// TODO: User Guide
		setContentView(R.layout.about_us);
	}

	@Override
	protected void onResume() {
		DebugLog.LOGD("AboutUs::onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		DebugLog.LOGD("AboutUs::onPause");
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		DebugLog.LOGD("AboutUs::onDestroy");
		super.onDestroy();
	}
}
