package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = "forecast_fragment";
    //global variables
    ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //add this line in order for this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button, so long
        //as you specify a parent activity in AndroidManifest.xml
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            FetchWeatherTask weatherTask = new FetchWeatherTask();     //get that weather
            weatherTask.execute(getResources().getString(R.string.pref_location_key));       //request the weather at zip: 15417
            return true;
        }else if(id==R.id.action_settings){
            //the settings button in the menu was clicked so let's change over with the intent class
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            Log.e(LOG_TAG, "The settings button was clicked");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] forecastArray = {
                "Today - Sunny - 88/64",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/64",
                "Fri - Heavy rain - 38/22",
                "Sat - HELP TRAPPED IN WEATHER STATION - 60/41",
                "Sun - Sunny"
        };
        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        //initialize the array adaptor to interpret the data from the forecastArray[]
        //first param just gets the current activity
        //second param retrieves the layout that the adaptor will use
        //third param retrieves the id to put the data into
        //fourth param is the data
        forecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        //we must create the view so we can search for ids in the fragment_main.xml file
        ListView v = (ListView) rootView.findViewById(R.id.listview_forecast);
        v.setAdapter(forecastAdapter);
        //add the click listener for the forecast fragment
        v.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                //start the DetailActivity by using Intents
                String intentText = forecastAdapter.getItem(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(
                        Intent.EXTRA_TEXT, intentText);
                startActivity(detailIntent);

                //code is commented out because it is replaced by the start of the activity DetailActivity
                /*Context context = getActivity().getApplicationContext();
                String toastText = forecastAdapter.getItem(position);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context,toastText,duration);
                toast.show();*/
            }
        });     //end declaration for setOnItemClickListener
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private static final String LOG_TAG = "sunshine_FetchWeather";

        /*
         * doInBackground
         * This function gets run whenever the refresh button is clicked (in the current condition)
         *  planning to do an update where it refreshes automatically
         * Also of note, this function runs in the background by extending the AsynTask class so that
         * there is processing time for the GUI drawing for the app (the google dev tutorial explained that)
         * @param String... params a variable length parameter that can be addressed like an array
         *
         */
        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";     //used for the json response from the weather api call
            String units = "metric";    //used for the measurement request in the weather api call
            int numDays = 7;            //used for day request in the weather api call
            String[] parseResult = null;    //used for parsing the string and returning it

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID = "e799a86d9e6b5baf228e3d42aea617a3";
                //time to use the URI builder to build the URL request
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter("APPID",APPID)
                        .build();
                //verbose log to display that we've built the URI correctly
                Log.v(LOG_TAG, "URI construction: " + builtUri.toString());
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();        //convert the json to a string
                Log.v(LOG_TAG, "Forecast JSON String: "+forecastJsonStr);
                } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            //time to parse the JSON and break it down into an array of Strings
            try {
                parseResult = getWeatherDataFromJson(forecastJsonStr, numDays);
                //for(int i=0;i<parseResult.length;i++)
                //    Log.v(LOG_TAG,"parseResult: " + parseResult[i]);
            } catch (JSONException e){
                Log.e(LOG_TAG, "Error parsing the JSON string");
            }
            return parseResult;
        }

        /*
         * JSON parsing functions
         */
        /*
         * The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now
         */
        private String getReadableDateString(long time){
            //because the API returns a unix timestamp (measured in seconds)
            //it must be converted to milliseconds in order to be converted to a valid date
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        @Override
        protected void onPostExecute(String[] result) {
            //update the listview UI
            if(result!=null){
                forecastAdapter.clear();
                for(String dayForecastStr : result){
                    forecastAdapter.add(dayForecastStr);
                }
            }
        }

        /*
                 * Prepare the weather high/lows for presentation
                 */
        private String formatHighLows(double high, double low){
            //for presentation, assume the user doesn't care about the tenths of a degree
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /*
         * Take the string representing the complete forecast in JSON format and
         * pull out the data we need to construct the strings needed for the wireframes.
         * Fortunately parsing is easy: constructor takes the JSON string and converts it
         * into an object hierarchy for us
         *
         * It is also of note to just put the general JSON addressing method I used to get the data
         * from the server
         * The JSON structure is set up as follows:
         * list (which is in an array if multiple days are requested)
         *      temp (holds the temperature data which are sorted by different values)
         *      the ones we're concerned about in this case is min and max
         *          min (the minimum temperature for that day)
         *          max (the maximum temperature for that day)
         * begin code reference
            public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
		    throws JSONException {
                JSONObject weatherJson = new JSONObject(weatherJsonStr);
                JSONArray weatherArray = weatherJson.getJSONArray("list");
                JSONObject dayForecast = weatherArray.getJSONObject(dayIndex);
                JSONObject tempObj = dayForecast.getJSONObject("temp");
                double high = tempObj.getDouble("max");
                double low = tempObj.getDouble("min");
                System.out.println("max: "+high+"     min: "+low);
		        return -1;
	        }
	     * end code reference
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
            //These are the names of the JSON objects that need to be extracted
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            //OWM returns daily forecasts based upon the local time of the city that is being
            //asked for, which means that we need to konw the GMT offset to translate this data
            //properly

            //Since this data is also sent in order and the first day isalways the
            //current day, we're going to take advantage of that to get a nice
            //normalized UTC date for all of our weather

            Time dayTime = new Time();
            dayTime.setToNow();

            //we start at the day returned by local time. Otherwise this is a mess
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            //now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i=0;i<weatherArray.length();i++){
                //for now, using the  format "day, description, high/low"
                String day, description, highAndLow;
                //get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //the date/time is returned as a long. We need to convert that
                //into something human-readable, since most people won't read "1400356800" as
                //"this saturday"
                long dateTime;
                //cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                //description is in a child array called weather, which is 1 element long
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                //temperatures are in a child object called temp. Try not to name variables
                //temp when working with temperature. it confuses everybody
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            for(String s:resultStrs){
                Log.v(LOG_TAG, "Forecast entry: "+s);
            }
            return resultStrs;
        }
    }   //end public class FetchWeatherTask
}
