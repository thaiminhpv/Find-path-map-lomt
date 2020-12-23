package funix.prm.assignment.prm391x_googlemap_minhpvtfx00930funixeduvn;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private String mPermisson = Manifest.permission.ACCESS_FINE_LOCATION;
    private GPSTrackerService gpsTracker;

    private TextInputEditText startLocationInput, targetLocationInput;
    private TextView distance, timeTravel;
    private MaterialButton findPath;
    private GoogleMap mMap;
    private Geocoder mGeocoder;
    private FragmentActivity context;

    private String startLocation, targetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //check for runtime permission
        if (ActivityCompat.checkSelfPermission(this, mPermisson)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{mPermisson}, REQUEST_CODE_PERMISSION);
        }
        context = this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //move camera to current location
        gpsTracker = new GPSTrackerService(this);
        //check if GPS enabled
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitutde = gpsTracker.getLongitude();

            LatLng currentLocation = new LatLng(latitude, longitutde);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your current location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        } else {
            //can't get location
            //GPS or Network isn't enabled
            //ask user to enable GPS/network in settings
            gpsTracker.showSettingAlert();
        }

        //init
        startLocationInput = findViewById(R.id.startLocation);
        targetLocationInput = findViewById(R.id.targetLocation);
        distance = findViewById(R.id.distance);
        timeTravel = findViewById(R.id.timeTravel);
        findPath = findViewById(R.id.findPath);

        //find path
        findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocation = startLocationInput.getText().toString();
                targetLocation = targetLocationInput.getText().toString();

                //hide keyboard
                View currentFocus = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //invalid input
                if (startLocation.length() < 1 || targetLocation.length() < 1) {
                    Snackbar.make(view, getResources().getString(R.string.input_is_null), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.dismiss_button), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {}
                            })
                            .show();
                    return;
                }

                try {
                    mGeocoder = new Geocoder(MapsActivity.this);
                    List<Address> location1List = mGeocoder.getFromLocationName(startLocation, 1);
                    List<Address> location2List = mGeocoder.getFromLocationName(targetLocation, 1);

                    if (location1List.size() == 0 || location2List.size() == 0) {
                        Snackbar.make(view, getResources().getString(R.string.input_is_null), Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.dismiss_button), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {}
                                })
                                .show();
                        return;
                    }
                    double x1 = location1List.get(0).getLatitude();
                    double y1 = location1List.get(0).getLongitude();

                    double x2 = location2List.get(0).getLatitude();
                    double y2 = location2List.get(0).getLongitude();

                    LatLng location1 = new LatLng(x1, y1);
                    LatLng location2 = new LatLng(x2, y2);

                    //chuyển camera tới trung điểm toạ độ
                    LatLngBounds.Builder bc = new LatLngBounds.Builder();
                    bc.include(location1);
                    bc.include(location2);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 200));

                    //lấy tên chuẩn rồi đặt Marker
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(location1).title(location1List.get(0).getFeatureName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.addMarker(new MarkerOptions().position(location2).title(location2List.get(0).getFeatureName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    //vẽ đường đi giữa 2 vị trí
                    new DrawPolylineFromTwoPoint().drawLineBetween(location1, location2, mMap, context, getResources().getString(R.string.google_maps_key));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
