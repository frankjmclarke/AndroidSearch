package com.fclarke.androidsearch;
/*
API Key (v3 auth)

4396e889509cf7a204ee60bf1ccee4da
API Read Access Token (v4 auth)

eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0Mzk2ZTg4OTUwOWNmN2EyMDRlZTYwYmYxY2NlZTRkYSIsInN1YiI6IjU5YzU3ZjM4YzNhMzY4MTQzMDAzMmY4YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.wz-ns8QQLxhYu5o3hQrNYUE-YD9fNNyfcHB7QdcUFQM
Example API Request

https://api.themoviedb.org/3/movie/550?api_key=4396e889509cf7a204ee60bf1ccee4da
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private RecyclerView mRecyclerView;
  public SearchView searchView;
  private List<String> movieList = new ArrayList<String>();
  MovieAdapter mAdapter;
  static String API_KEY = "4396e889509cf7a204ee60bf1ccee4da";
  static ArrayList<String> posters;
  static ArrayList<MovieResult> results;

  static int width;
  private Context ctx = this;
  static final String TAG ="MAIN";

  public boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    searchView = (SearchView) findViewById(R.id.search_view);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
    try {
      createlist();  // in this method, Create a movieList of items.
    } catch (IOException e) {
      e.printStackTrace();
    }
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    // call the adapter with argument movieList of items and context.
    mAdapter = new MovieAdapter(movieList, this);
    mRecyclerView.setAdapter(mAdapter);
    searchView.setOnQueryTextListener(listener); // call the QuerytextListner.
  }

  @Override
  public void onStart() {
    super.onStart();
    this.setTitle("Most Popular Movies");


  }

  // this method is used to create movieList of items.
  public void createlist() throws IOException {


    movieList.add("PlaceHolder");

    MyTask job = new MyTask();
    job.execute("Jack+Reacher");

  }

  SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextChange(String query) {
      query = query.toLowerCase();

      final List<String> filteredList = new ArrayList<>();

      for (int i = 0; i < movieList.size(); i++) {

        final String text = movieList.get(i).toLowerCase();
        if (text.contains(query)) {

          filteredList.add(movieList.get(i));
        }
      }

      mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
      mAdapter = new MovieAdapter(filteredList, MainActivity.this);
      mRecyclerView.setAdapter(mAdapter);
      mAdapter.notifyDataSetChanged();  // data set changed
      return true;

    }

    public boolean onQueryTextSubmit(String query) {
      return false;
    }
  };

  public String stringify(InputStream stream) throws IOException, UnsupportedEncodingException {
    Reader reader = null;
    reader = new InputStreamReader(stream, "UTF-8");
    BufferedReader bufferedReader = new BufferedReader(reader);
    return bufferedReader.readLine();
  }

  public ArrayList<MovieResult>  getPathsFromAPI(String query) {

    while (true) {
      HttpURLConnection urlConnection = null;
      BufferedReader reader = null;
      String JSONResult;

      try {
        String urlString = null;

        urlString = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&query="+query;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        //urlConnection.setReadTimeout(10000 );//milliseconds
        //urlConnection.setConnectTimeout(15000); // milliseconds
        //urlConnection.addRequestProperty("Accept", "application/json");
        //urlConnection.setDoInput(true);
        urlConnection.connect();
        int responseCode = urlConnection.getResponseCode();
        Log.d(TAG, "The response code is: " + responseCode + " " + urlConnection.getResponseMessage());

        //Read the input stream into a String
        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        if (inputStream == null) {
          Log.i(TAG, "inputStream null");
          return null;
        }
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        Log.i("getPathsFromAPI", line);
        while ((line = reader.readLine()) != null) {
          buffer.append(line + "\n");
        }
        if (buffer.length() == 0) {
          return null;
        }
        JSONResult = buffer.toString();
        //Log.i("getPathsFromAPI ",JSONResult);

        try {
          Log.i("getPathsFromAPI ", JSONResult);
          return getPathsFromJSON(JSONResult);
        } catch (JSONException e) {
          return null;
        }
      } catch (Exception e) {
        Log.i("getPathsFromAPI ", e.toString());
        continue;
      } finally {
        if (urlConnection != null) {
          urlConnection.disconnect();
        }
        if (reader != null) {
          try {
            reader.close();
          } catch (final IOException e) {
          }
        }
      }
    }
  }

  public ArrayList<MovieResult> getPathsFromJSON(String JSONStringParam) throws JSONException {

    String streamAsString = JSONStringParam;
    ArrayList<MovieResult> results = new ArrayList<MovieResult>();
    try {
      JSONObject jsonObject = new JSONObject(streamAsString);
      JSONArray array = (JSONArray) jsonObject.get("results");
      for (int i = 0; i < array.length(); i++) {
        JSONObject jsonMovieObject = array.getJSONObject(i);
        MovieResult.Builder movieBuilder = MovieResult.newBuilder(
            Integer.parseInt(jsonMovieObject.getString("id")),
            jsonMovieObject.getString("title"))
            .setBackdropPath(jsonMovieObject.getString("backdrop_path"))
            .setOriginalTitle(jsonMovieObject.getString("original_title"))
            .setPopularity(jsonMovieObject.getString("popularity"))
            .setPosterPath(jsonMovieObject.getString("poster_path"))
            .setReleaseDate(jsonMovieObject.getString("release_date"));
        results.add(movieBuilder.build());
      }
    } catch (JSONException e) {
      System.err.println(e);
      Log.d(TAG, "Error parsing JSON. String was: " + streamAsString);
    }
    return results;
  }

  private class MyTask extends AsyncTask<String, Void, String> {
    boolean isNetworkError = false;

    @Override
    protected String doInBackground(String[] params) {

      if (!isNetworkAvailable(ctx) || !isInternetAvailable()) {
        isNetworkError = true;
        return "";
      }

      posters = new ArrayList(Arrays.asList(getPathsFromAPI(params[0])));
      return "some message";
    }

    @Override
    protected void onPostExecute(String message) {
      if (isNetworkError)
        Toast.makeText(ctx, "No Internet connection", Toast.LENGTH_LONG).show();
      movieList.clear();
      //TODO add images and text to MovieAdapter
      movieList.add("PlaceHolder2");

    }
  }

  public boolean isNetworkAvailable(Context context) {
    final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
  }

  public boolean isInternetAvailable() {
    try {
      final InetAddress address = InetAddress.getByName("www.google.com");
      return !address.equals("");
    } catch (UnknownHostException e) {
      // Log error
    }
    return false;
  }
}
