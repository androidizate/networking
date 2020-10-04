package com.androidizate.clase7

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class MainActivity : AppCompatActivity() {

    var userList: MutableList<User> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager
        if (!isNetworkConnected()) {
            createAlert(R.string.not_connected_error)
        } else {
            downloadInfo()
        }
    }

    private fun createAlert(@StringRes stringResource: Int) {
        AlertDialog.Builder(this)
                .setMessage(getString(stringResource))
                .setTitle(getString(R.string.error))
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialogInterface, i -> this@MainActivity.finish() }).show()
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun downloadInfo() {
        GetContacts().execute()
    }

    private inner class GetContacts : AsyncTask<Void?, Void?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            Toast.makeText(this@MainActivity, "Json Data is downloading", Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            val sh = HttpHandler()
            // Hacemos una request y recibimos una respuesta
            val url = "https://jsonplaceholder.typicode.com/users"
            val jsonStr = sh.makeServiceCall(url)
            Log.d("Respuesta", "Respuesta desde la url: $jsonStr")
            if (jsonStr != null) {
                try {
                    // Tomamos el nodo con el JSON Array
                    val users = JSONArray(jsonStr)

                    // looping through All Contacts
                    for (i in 0 until users.length()) {

                        val jsonUser = users.getJSONObject(i)

                        // Tomamos el nodo Address que esta dentro de user
                        val jsonAddress = jsonUser.getJSONObject("address")

                        // Tomamos el nodo Geo que esta dentro de Address
                        val jsonGeo = jsonAddress.getJSONObject("geo")
                        val geo = Geo(jsonGeo.getString("lat"),
                                jsonGeo.getString("lng"))

                        // Seteamos el nuevo objeto GEO a la Address local
                        val address = Address(jsonAddress.getString("street"),
                                jsonAddress.getString("suite"),
                                jsonAddress.getString("city"),
                                jsonAddress.getString("zipcode"),
                                geo)

                        // Tomamos el nodo Company que esta dentro de user
                        val jsonCompany = jsonUser.getJSONObject("company")
                        val company = Company(
                                jsonCompany.getString("name"),
                                jsonCompany.getString("catchPhrase"),
                                jsonCompany.getString("bs")
                        )

                        // Seteamos la compañia y la direccion al user y
                        // agregamos el nuevo user completo a la lista de users
                        val user = User(
                                jsonUser.getLong("id"),
                                jsonUser.getString("name"),
                                jsonUser.getString("username"),
                                jsonUser.getString("email"),
                                address,
                                jsonUser.getString("phone"),
                                jsonUser.getString("website"),
                                company
                        )
                        userList.add(user)
                    }
                } catch (e: JSONException) {
                    Log.e("Error", "Json parsing error: " + e.message)
                    runOnUiThread(Runnable { Toast.makeText(applicationContext, "Json parsing error: " + e.message, Toast.LENGTH_LONG).show() })
                }
            } else {
                Log.e("Error", "Couldn't get json from server.")
                runOnUiThread(Runnable { Toast.makeText(applicationContext, "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show() })
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            recycler.adapter = UserAdapter(userList)
        }
    }
}