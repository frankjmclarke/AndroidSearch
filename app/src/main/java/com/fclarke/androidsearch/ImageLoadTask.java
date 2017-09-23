package com.fclarke.androidsearch;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static com.fclarke.androidsearch.MainActivity.API_KEY;
import static com.fclarke.androidsearch.MainActivity.posters;

import static com.fclarke.androidsearch.MainActivity.width;

/**
 * Created by Frank Clarke on 2017-09-22.
 */

public class ImageLoadTask extends AsyncTask<Void, Void, ArrayList<String>> {

  @Override
  protected ArrayList<String> doInBackground(Void... params) {
    while(true){
      try{
        posters = new ArrayList(Arrays.asList(getPathsFromAPI(true)));
        return posters;
      }
      catch(Exception e)
      {
        continue;
      }
    }

  }
  @Override
  protected void onPostExecute(ArrayList<String> result)
  {
    /*
    if(result!=null && getActivity()!=null)
    {
      //ImageAdapter adapter = new ImageAdapter(getActivity(),result, width);
      //gridview.setAdapter(adapter);

    }
    */
  }
  public String[] getPathsFromAPI(boolean sortbypop)
  {
    while(true)
    {
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
          Log.i("IMAGELOAD",line);
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
      }catch(Exception e)
      {
        continue;
      }finally {
        if(urlConnection!=null)
        {
          urlConnection.disconnect();
        }
        if(reader!=null)
        {
          try{
            reader.close();
          }catch(final IOException e)
          {
          }
        }
      }


    }
  }
  public String[] getPathsFromJSON(String JSONStringParam) throws JSONException {

    JSONObject JSONString = new JSONObject(JSONStringParam);

    JSONArray moviesArray = JSONString.getJSONArray("results");
    String[] result = new String[moviesArray.length()];

    for(int i = 0; i<moviesArray.length();i++)
    {
      JSONObject movie = moviesArray.getJSONObject(i);
      String moviePath = movie.getString("poster_path");
      result[i] = moviePath;
    }
    return result;
  }
}
