package srdes.menupp;

import android.app.Activity;
import android.os.Bundle;

public class UserGuide extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("UserGuide::onCreate");
		super.onCreate(savedInstanceState);
		
		// TODO About US
		setContentView(R.layout.user_guide);
	}
	
	@Override
	protected void onResume() {
		DebugLog.LOGD("UserGuide::onResume");
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		DebugLog.LOGD("UserGuide::onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		DebugLog.LOGD("UserGuide::onDestroy");
		super.onDestroy();
	}
}
