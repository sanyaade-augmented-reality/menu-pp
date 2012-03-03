package srdes.menupp;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

public class Splash extends Activity{

	MediaPlayer ourSong;
	@Override
	protected void onCreate(Bundle LarryLovesCats) {
		// TODO Auto-generated method stub
		super.onCreate(LarryLovesCats);
		setContentView(R.layout.splash);
		ourSong = MediaPlayer.create(Splash.this, R.raw.menuppsplash);
		ourSong.start();
		Thread timer = new Thread(){
			public void run(){	
				try{
					sleep(3500);
				} catch (InterruptedException e){
					e.printStackTrace();
				} finally {
					Intent openStartingPoint = new Intent("android.intent.action.MENUPP");
					startActivity(openStartingPoint);
				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		ourSong.release();
		finish();
	}
}
