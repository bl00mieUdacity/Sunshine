package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.vo.ForecastRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<Forecast> m_forecastAdapter;
    private static final String LOG_TAG = "ForecastFragment";
    protected Context m_context;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        m_forecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, new ArrayList<Forecast>());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(m_forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Forecast forecast = (Forecast)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast.toString());
                startActivity(intent);
            }
        });

        setHasOptionsMenu(true);

        m_context = getContext();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateForecast();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Toast.makeText(getContext(), "Refreshing forecast data.", Toast.LENGTH_SHORT).show();
            updateForecast();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateForecast() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        weatherTask.execute(new ForecastRequest(location,"metric","json",7));
    }

    class FetchWeatherTask extends AsyncTask<ForecastRequest, Integer, List<Forecast>> {

        private static final String s_apiKey = "d3ece1824d6807e32666e075d3b95e9f";
        private static final String LOG_TAG = "FetchWeatherTask";

        @Override
        protected void onPostExecute(List<Forecast> forecasts) {
            super.onPostExecute(forecasts);
            m_forecastAdapter.clear();
            for (Forecast f : forecasts) {
                m_forecastAdapter.add(f);
            }
        }

        @Override
        protected List<Forecast> doInBackground(ForecastRequest... params) {
            if (params== null || params.length == 0) {
                return null;
            }
            ForecastRequest req = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try {
                URL url = new URL(req.getUri(s_apiKey).toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "doInBackground:", e);
                return null;
            }
            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "doInBackground:", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "doInBackground: ", e);
                return null;
            }
        }

        private String getReadableDateString(long time){
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private List<Forecast> getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {

            List<Forecast> forecasts = new ArrayList<>();

            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                Forecast forecast = new Forecast();
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                forecast.setDateString(getReadableDateString(dateTime));

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                forecast.setDescription(weatherObject.getString(OWM_DESCRIPTION));

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                forecast.setHigh(temperatureObject.getDouble(OWM_MAX));
                forecast.setLow(temperatureObject.getDouble(OWM_MIN));
                forecasts.add(forecast);
            }
            return forecasts;
        }
    }

    class Forecast {
        private String m_dateString;
        private String m_description;
        private double m_high;
        private double m_low;

        public String getDateString() {
            return m_dateString;
        }

        public void setDateString(String dateString) {
            m_dateString = dateString;
        }

        public String getDescription() {
            return m_description;
        }

        public void setDescription(String description) {
            m_description = description;
        }

        public double getHigh() {
            return m_high;
        }

        public void setHigh(double high) {
            m_high = high;
        }

        public double getLow() {
            return m_low;
        }

        public void setLow(double low) {
            m_low = low;
        }

        private String formatHighLows() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
            String units = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));

            double high = m_high;
            double low = m_low;

            if ("imperial".equals(units)) {
                high = high*1.8 + 32;
                low = low*1.8 + 32;
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            return roundedHigh + "/" + roundedLow;
        }

        public String toString() {
            return m_dateString + " - " + m_description + " - " + formatHighLows();
        }
    }

}
