package com.ls.ui.fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ls.drupalcon.R;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.UpdateRequest;
import com.ls.drupalcon.model.UpdatesManager;
import com.ls.drupalcon.model.data.Location;
import com.ls.drupalcon.model.managers.LocationManager;
import com.ls.ui.view.TagBadgeSpannable;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LocationFragment extends Fragment implements CustomMapFragment.OnActivityCreatedListener {

    private static final int ZOOM_LEVEL = 15;
    private static final int TILT_LEVEL = 0;
    private static final int BEARING_LEVEL = 0;

    public static final String TAG = "LocationsFragment";
    private GoogleMap mGoogleMap;

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            replaceMapFragment();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_location, null);
//        final String nbspSpacing = "\u202F\u202F"; // none-breaking spaces
//        String badgeText = nbspSpacing + "Tesddsdsdsdsts" + nbspSpacing;
//        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//
//        stringBuilder.append("Tesddsdsdsdsts");
//        stringBuilder.setSpan(
//                new TagBadgeSpannable(2, Color.parseColor("#ffffff"), Color.parseColor("#1f3e98")),
//                1,
//                5,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        );
//        stringBuilder.append("  ");

        final String nbspSpacing = "\u202F\u202F"; // none-breaking spaces
        String badgeText = nbspSpacing + "Test Nazar UI" + nbspSpacing;
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(badgeText);
        stringBuilder.setSpan(
                new TagBadgeSpannable(Color.parseColor("#ffffff"), Color.parseColor("#1f3e98")),
                badgeText.length() -3,
                badgeText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        stringBuilder.append("  ");

        TextView viewById = (TextView) view.findViewById(R.id.textView12);
        viewById.setText(stringBuilder);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Model.instance().getUpdatesManager().registerUpdateListener(updateListener);
        replaceMapFragment();
    }

    @Override
    public void onActivityCreated(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        new LoadLocations().execute();
    }

    private class LoadLocations extends AsyncTask<Void, Void, List<Location>> {
        @Override
        protected List<Location> doInBackground(Void... params) {
            LocationManager locationManager = Model.instance().getLocationManager();
            return locationManager.getLocations();
        }

        @Override
        protected void onPostExecute(List<Location> locations) {
            hideProgressBar();
            fillMapViews(locations);
        }
    }

    private void fillMapViews(List<Location> locations) {
        if (mGoogleMap == null) return;

        if (locations == null || locations.isEmpty()) {
            TextView textViewAddress = (TextView) getView().findViewById(R.id.txtAddress);
            textViewAddress.setText(getString(R.string.placeholder_location));
        }

        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            LatLng position = new LatLng(location.getLat(), location.getLon());
            mGoogleMap.addMarker(new MarkerOptions().position(position));

            if (i == 0) {
                CameraPosition camPos = new CameraPosition(position, ZOOM_LEVEL, TILT_LEVEL, BEARING_LEVEL);
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                fillTextViews(location);
            }
        }

        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
    }

    private void fillTextViews(Location location) {
        if (getView() == null) {
            return;
        }

        TextView txtAmsterdam = (TextView) getView().findViewById(R.id.txtPlace);
        TextView txtAddress = (TextView) getView().findViewById(R.id.txtAddress);

        String locationName = location.getName();
        txtAmsterdam.setText(locationName);

        String address = location.getAddress();
//        address = address.replace(", ", "\n");
        address = address.replace(",", "\n");
        txtAddress.setText(address.trim());
    }

    private void replaceMapFragment() {
        CustomMapFragment mapFragment = CustomMapFragment.newInstance(LocationFragment.this);
        LocationFragment
                .this.getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentHolder, mapFragment)
                .commitAllowingStateLoss();
    }

    private void hideProgressBar() {
        if (getView() != null) {
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
