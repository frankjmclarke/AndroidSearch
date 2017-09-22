package com.fclarke.androidsearch;
/*
API Key (v3 auth)

4396e889509cf7a204ee60bf1ccee4da
API Read Access Token (v4 auth)

eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0Mzk2ZTg4OTUwOWNmN2EyMDRlZTYwYmYxY2NlZTRkYSIsInN1YiI6IjU5YzU3ZjM4YzNhMzY4MTQzMDAzMmY4YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.wz-ns8QQLxhYu5o3hQrNYUE-YD9fNNyfcHB7QdcUFQM
Example API Request

https://api.themoviedb.org/3/movie/550?api_key=4396e889509cf7a204ee60bf1ccee4da
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private RecyclerView mRecyclerView;
  public SearchView search;
  private List<String> movieList = new ArrayList<String>();
  MovieAdapter mAdapter;
  static String API_KEY = "4396e889509cf7a204ee60bf1ccee4da";
  static ArrayList<String> posters;
  static boolean sortByPop;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    search = (SearchView) findViewById(R.id.search);
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
    search.setOnQueryTextListener(listener); // call the QuerytextListner.
  }

  // this method is used to create movieList of items.
  public void createlist() throws IOException {

    posters = new ArrayList(Arrays.asList(getPathsFromAPI(sortByPop)));
    movieList.add("Volkswagen Vento");
    movieList.add("Hyundai Xcent");
    movieList.add("Maruti Swift");
    movieList.add("Hyundai i20");
    movieList.add("Ford Fiesta Classic");
    movieList.add("Chevrolet Beat");
    movieList.add("Maruti Alto");
    movieList.add("Toyota Etios");
    movieList.add("Toyota Innova");
    movieList.add("Mahindra Scorpio");
    movieList.add("Maruti Wagon R");
    movieList.add("Ford Figo");
/*
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("api_key", API_KEY));
        params.add(new BasicNameValuePair("page", "3"));
        JSONObject jsonObject = jsonParser.MakeHTTPRequest(GET_MOVIES_URL, "GET", params);


    URL url = new URL("http://api.themoviedb.org/3/movie/550?api_key=7b5e30851a9285340e78c201c4e4ab99");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setDoOutput(true);
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    System.out.println(con.getResponseMessage());

    BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));

    String output;
    System.out.println("Output from Server .... \n");
    while ((output = br.readLine()) != null) {
      System.out.println(output);
    }
    */
  }

    /* this is the Seerach QuerttextListner.
       this method filter the movieList data with a matching string,
       hence provides user an easy way to find the information he needs.
     */

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

  public String[] getPathsFromAPI(boolean sortbypop) {
    while (true) {
      HttpURLConnection urlConnection = null;
      BufferedReader reader = null;
      String JSONResult;

      try {
        String urlString = null;
        if (sortbypop) {
          urlString = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + API_KEY;
        } else {
          urlString = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&vote_count.gte=500&api_key=" + API_KEY;
        }
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        //Read the input stream into a String
        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
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
        JSONResult = buffer.toString();

        try {
          return getPathsFromJSON(JSONResult);
        } catch (JSONException e) {
          return null;
        }
      } catch (Exception e) {
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

  public String[] getPathsFromJSON (String JSONStringParam)throws JSONException {

    JSONObject JSONString = new JSONObject(JSONStringParam);

    JSONArray moviesArray = JSONString.getJSONArray("results");
    String[] result = new String[moviesArray.length()];

    for (int i = 0; i < moviesArray.length(); i++) {
      JSONObject movie = moviesArray.getJSONObject(i);
      String moviePath = movie.getString("poster_path");
      result[i] = moviePath;
    }
    return result;
  }
}
