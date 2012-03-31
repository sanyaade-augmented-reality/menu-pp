package srdes.menupp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 *\brief Class that displays user guide.
 */
public class UserGuide extends Activity {
    
        private ViewPager awesomePager;
        private static int NUM_AWESOME_VIEWS = 20;
        private Context cxt;
        private AwesomePagerAdapter awesomeAdapter;
        
        /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_guide);
        cxt = this;
        
        awesomeAdapter = new AwesomePagerAdapter();
        awesomePager = (ViewPager) findViewById(R.id.awesomepager);
        awesomePager.setAdapter(awesomeAdapter);
    }
    
    private class AwesomePagerAdapter extends PagerAdapter{
    	
    		 
            public int getCount() {
                return 6;
            }
     
            public Object instantiateItem(View collection, int position) {
     
                LayoutInflater inflater = (LayoutInflater) collection.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     
                int resId = 0;
                switch (position) {
                case 0:
                    resId = R.layout.slide1;
                    break;
                case 1:
                    resId = R.layout.slide2;
                    break;
                case 2:
                    resId = R.layout.slide3;
                    break;
                case 3:
                    resId = R.layout.slide4;
                    break;
                case 4:
                    resId = R.layout.slide5;
                    break;
                case 5:
                    resId = R.layout.slide6;
                    break;
                }
     
                View view = inflater.inflate(resId, null);
     
                ((ViewPager) collection).addView(view, 0);
     
                return view;
            }
     
            @Override
            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView((View) arg2);
     
            }
     
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == ((View) arg1);
     
            }
     
            @Override
            public Parcelable saveState() {
                return null;
            }
    }
}