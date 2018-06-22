package com.androidizate.clase7;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (!isNetworkConnected()) {
            createAlert(R.string.not_connected_error);
        } else {
            downloadInfo();
        }
    }

    private void createAlert(@StringRes int stringResource) {
        new AlertDialog.Builder(this)
                .setMessage(getString(stringResource))
                .setTitle(getString(R.string.error))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                }).show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void downloadInfo() {
        new GetContacts().execute();
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Hacemos una request y recibimos una respuesta
            String url = "https://jsonplaceholder.typicode.com/users";
            String jsonStr = sh.makeServiceCall(url);

            Log.e("Respuesta", "Respuesta desde la url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    // Tomamos el nodo con el JSON Array
                    JSONArray users = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < users.length(); i++) {
                        User user = new User();
                        JSONObject jsonUser = users.getJSONObject(i);
                        user.setId(jsonUser.getLong("id"));
                        user.setName(jsonUser.getString("name"));
                        user.setUsername(jsonUser.getString("username"));
                        user.setEmail(jsonUser.getString("email"));
                        user.setPhone(jsonUser.getString("phone"));
                        user.setWebsite(jsonUser.getString("website"));

                        // Tomamos el nodo Address que esta dentro de user
                        JSONObject jsonAddress = jsonUser.getJSONObject("address");
                        Address address = new Address();
                        address.setStreet(jsonAddress.getString("street"));
                        address.setSuite(jsonAddress.getString("suite"));
                        address.setCity(jsonAddress.getString("city"));
                        address.setZipcode(jsonAddress.getString("zipcode"));

                        // Tomamos el nodo Geo que esta dentro de Address
                        JSONObject jsonGeo = jsonAddress.getJSONObject("geo");
                        Geo geo = new Geo();
                        geo.setLat(jsonGeo.getString("lat"));
                        geo.setLng(jsonGeo.getString("lng"));
                        // Seteamos el nuevo objeto GEO a la Address local
                        address.setGeo(geo);

                        // Tomamos el nodo Company que esta dentro de user
                        JSONObject jsonCompany = jsonUser.getJSONObject("company");
                        Company company = new Company();
                        company.setName(jsonCompany.getString("name"));
                        company.setCatchPhrase(jsonCompany.getString("catchPhrase"));
                        company.setBs(jsonCompany.getString("bs"));

                        // Seteamos la compañia y la direccion al user
                        user.setAddress(address);
                        user.setCompany(company);

                        // Agregamos el nuevo user completo a la lista de users
                        userList.add(user);
                    }
                } catch (final JSONException e) {
                    Log.e("Error", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e("Error", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            recyclerView.setAdapter(new UserAdapter(userList));
        }
    }
}
