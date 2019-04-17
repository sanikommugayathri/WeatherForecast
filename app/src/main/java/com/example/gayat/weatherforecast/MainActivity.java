package com.example.gayat.weatherforecast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gayat.weatherforecast.data.SunshinePreferences;
import com.example.gayat.weatherforecast.data.WeatherContract;
import com.example.gayat.weatherforecast.utilities.FakeDataUtils;
import com.example.gayat.weatherforecast.utilities.NetworkUtils;
import com.example.gayat.weatherforecast.utilities.OpenWeatherJsonUtils;

import java.net.URL;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ForecastAdapter.ForecastAdapterOnClickHandler {

    private final String TAG = MainActivity.class.getSimpleName();

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    private static final int ID_FORECAST_LOADER = 44;

    private ForecastAdapter mForecastAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mLoadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);

        FakeDataUtils.insertFakeData(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);


        mForecastAdapter = new ForecastAdapter(this, this);

        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);

    }

    private void openPreferredLocationInMap() {
        double[] coords = SunshinePreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {


        switch (loaderId) {

//          COMPLETED (22) If the loader requested is our forecast loader, return the appropriate CursorLoader
            case ID_FORECAST_LOADER:
                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

//      COMPLETED (27) Remove the previous body of onLoadFinished

//      COMPLETED (28) Call mForecastAdapter's swapCursor method and pass in the new Cursor
        mForecastAdapter.swapCursor(data);
//      COMPLETED (29) If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
//      COMPLETED (30) Smooth scroll the RecyclerView to mPosition
        mRecyclerView.smoothScrollToPosition(mPosition);

//      COMPLETED (31) If the Cursor's size is not equal to 0, call showWeatherDataView
        if (data.getCount() != 0) showWeatherDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mForecastAdapter.swapCursor(null);
    }


    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

    private void showWeatherDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* Then, hide the weather data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
