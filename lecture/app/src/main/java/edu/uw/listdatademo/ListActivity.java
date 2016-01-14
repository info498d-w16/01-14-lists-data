package edu.uw.listdatademo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.ArrayList;
import android.os.AsyncTask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

	private ArrayAdapter<String> adapter;

	private class MovieDownloadTask extends AsyncTask<String, Void, ArrayList<String>> {

		protected ArrayList<String> doInBackground(String... params){
			String url = params[0];
			return downloadMovieData(url);
		}

		protected void onPostExecute(ArrayList<String> movies) {
			adapter.clear();
			for(String movie: movies) {
				adapter.add(movie);
			}
		}
	}

	private static ArrayList<String> downloadMovieData(String movieQuery) {

		//construct the url for the omdbapi API
		String urlString = "";
		try {
			urlString = "http://www.omdbapi.com/?s=" + URLEncoder.encode(movieQuery, "UTF-8") + "&type=movie";
		}catch(UnsupportedEncodingException uee){
			return null;
		}

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		String[] movieStrings = null;
		ArrayList<String> movies = new ArrayList<String>();

		try {

			URL url = new URL(urlString);

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

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
			String results = buffer.toString();
			results = results.replace("{\"Search\":[","");
			results = results.replace("]}","");
			results = results.replace("},", "},\n");

			movieStrings = results.split("\n");
			for (String movie: movieStrings) {
				movies.add(movie);
			}
		} 
		catch (IOException e) {
			return null;
		} 
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} 
				catch (final IOException e) {
				}
			}
		}

		return movies;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
			  adapter = new ArrayAdapter<String>(
       		this,  R.layout.list_item,  R.id.txt_item, new ArrayList<String>());

				ListView listView = (ListView) findViewById(R.id.listView);
				listView.setAdapter(adapter);
				
        MovieDownloadTask task = new MovieDownloadTask();
        task.execute("Die Hard");

    }
}
