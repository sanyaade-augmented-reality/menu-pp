package srdes.menupp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ViewFlipper;

public class UserGuide extends Activity implements OnClickListener {

	ViewFlipper flippy;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("UserGuide::onCreate");
		super.onCreate(savedInstanceState);
		
		// TODO About US
		setContentView(R.layout.user_guide);
		flippy = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flippy.setOnClickListener((OnClickListener) this);
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

	public void onClick(View v) {
		// TODO Auto-generated method stub
		flippy.showNext();
	}
}
