package edu.bc.cs.parisin.cs344s14.getlost;

import java.text.DecimalFormat;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	private LocationListener listener;
	private LocationManager locManager;
	private double lat, lon;
	private double mlat = Double.MAX_VALUE;
	private double mlon = Double.MAX_VALUE;
	private double distance, bearing;
	private Compass compass;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		compass = new Compass(this);
		locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		setupGPSWatcher();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch(position) {
			case 0: return new SectionFragment1();
			case 1: return new SectionFragment2();
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}
	
	public void mark(View view) {
		TextView markLat = (TextView)findViewById(R.id.markedLat);
		TextView markLon = (TextView)findViewById(R.id.markedLong);
		
		mlat = lat;
		mlon = lon;
		
		DecimalFormat numberFormat = new DecimalFormat("#.000000");
		String latitude = numberFormat.format(mlat);
		String longitude = numberFormat.format(mlon);
		
		markLat.setText(latitude);
		markLon.setText(longitude);
		
	}
	
	public double sinD(double angle) {
		double rads = angle * Math.PI / 180;
		return Math.sin(rads);
	}
	
	public double cosD(double angle) {
		double rads = angle * Math.PI / 180;
		return Math.cos(rads);
	}
	
	public void setDistance() {
		double earthRadius = 3963;
		double dLat = mlat - lat;
		double dLon = mlon - lon;
		double sdLat = sinD(dLat/2);
		double sdLon = sinD(dLon/2);
		double a = sdLat*sdLat + cosD(lat)*cosD(mlat)*sdLon*sdLon;
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		distance = earthRadius * c;
		bearing = Math.toDegrees(Math.atan2(sinD(dLon)*cosD(mlat), cosD(lat)*sinD(mlat)-sinD(lat)*cosD(mlat)*cosD(dLon)));
		if (bearing < 0) { bearing += 360; }
		
		final TextView dist = (TextView)findViewById(R.id.distance);
		final TextView bear = (TextView)findViewById(R.id.bearing);
		
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		String distString = numberFormat.format(distance);
		int bearInt = (int)Math.round(bearing);
		//String bearString = numberFormat.format(bearing);
		
		dist.setText(distString + " mi");
		bear.setText(bearInt + "\u00B0");
	}
	
	public void rotateArrow() {
		ImageView arrow = (ImageView)findViewById(R.id.arrow);
		if (arrow != null) {
			Matrix matrix = new Matrix();
			arrow.setScaleType(ScaleType.MATRIX);
			int cx = arrow.getWidth()/2;
			int cy = arrow.getHeight()/2;
			float azimuth = compass.getAzimuth();
			matrix.postRotate(-azimuth, cx, cy);
			matrix.postRotate((float)bearing, cx, cy);
			arrow.setImageMatrix(matrix);
			arrow.invalidate();
		}
	}
	
	public void setupGPSWatcher() {
		listener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					lat = location.getLatitude();
					lon = location.getLongitude();
					
					DecimalFormat numberFormat = new DecimalFormat("#.000000");
					String latitude = numberFormat.format(lat);
					String longitude = numberFormat.format(lon);
					
					TextView curLat = (TextView)findViewById(R.id.curLat);
					TextView curLong = (TextView)findViewById(R.id.curLong);
					TextView findCurLat = (TextView)findViewById(R.id.findCurLat);
					TextView findCurLong = (TextView)findViewById(R.id.findCurLong);
					
					curLat.setText(latitude);
					findCurLat.setText(latitude);
					curLong.setText(longitude);
					findCurLong.setText(longitude);
					
					if (mlat != Double.MAX_VALUE && mlon != Double.MAX_VALUE && mlat!=lat && mlon!=lon) { //check to see if a location has been marked and that you arent just standing in the same spot
						setDistance();
						rotateArrow();
					}
				}
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				//display error messages
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				//do something
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				//do something
			}
			
		};
	}
	
	@Override
	public void onResume() {
		super.onResume();
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 /* milliseconds */, 1 /* meters */, listener);
	}
	
	@Override
	public void onPause() { // Don't use CPU time or battery when paused!
		super.onPause();
		locManager.removeUpdates(listener);
	}
	
}
	
class SectionFragment1 extends Fragment {
	public SectionFragment1() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
		View rootView = inflater.inflate(R.layout.fragment1, container, false);
		return rootView;
	}
}

class SectionFragment2 extends Fragment {
	
	public SectionFragment2() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
		View rootView = inflater.inflate(R.layout.fragment2, container, false);
		return rootView;
	}
}


