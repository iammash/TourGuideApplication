package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Observable;
import java.util.Observer;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;
import uk.ac.aston.wadekabs.tourguideapplication.model.User;

/**
 * An activity representing a single PlaceItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlaceItemListActivity}.
 */
public class NearbyPlaceDetailActivity extends AppCompatActivity implements Observer {

    private Place mSelectedPlace;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeitem_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String placeId = getIntent().getStringExtra(PlaceItemDetailFragment.SELECTED_PLACE_ID);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(PlaceItemDetailFragment.SELECTED_PLACE_ID, placeId);

            PlaceItemDetailFragment fragment = new PlaceItemDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.placeitem_detail_container, fragment)
                    .commit();
        }

        mSelectedPlace = PlaceContent.getPlace(placeId);
        if (mSelectedPlace != null) {
            mSelectedPlace.addObserver(this);
        }

        ViewPager mPager = (ViewPager) findViewById(R.id.place_photos_pager);
        mPagerAdapter = new PlacePhotoPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PlaceItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickFavourite(View view) {

        mSelectedPlace.setFavourite(!mSelectedPlace.isFavourite());

        if (mSelectedPlace.isFavourite())
            FirebaseDatabase.getInstance().getReference("favourites").child(User.getInstance().getUser().getUid()).child(mSelectedPlace.getPlaceId()).setValue(mSelectedPlace.isFavourite());
        else
            FirebaseDatabase.getInstance().getReference("favourites").child(User.getInstance().getUser().getUid()).child(mSelectedPlace.getPlaceId()).removeValue();
    }

    @Override
    public void update(Observable o, Object arg) {
        mPagerAdapter.notifyDataSetChanged();
    }

    private class PlacePhotoPagerAdapter extends FragmentStatePagerAdapter {

        PlacePhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = new Bundle();
            args.putInt(PhotoFragment.PHOTO_INDEX, position);
            args.putString(PhotoFragment.SELECTED_PLACE, mSelectedPlace.getPlaceId());

            PhotoFragment fragment = new PhotoFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return mSelectedPlace != null ? mSelectedPlace.getPictures().size() : 0;
        }
    }
}
