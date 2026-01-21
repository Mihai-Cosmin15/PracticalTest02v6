package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities.getReader
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities.getWriter
import ro.pub.cs.systems.eim.practicaltest02v6.model.Information
import java.io.IOException
import java.net.Socket
import java.time.Instant
import java.time.LocalDateTime
import kotlin.text.get
import kotlin.time.Duration

class CommunicationThread(
    private val serverThread: ServerThread,
    private val socket: Socket
) : Thread() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run() {
        try {
            socket.use { s ->
                val bufferedReader = getReader(s)
                val printWriter = getWriter(s)

                Log.i(
                    Constants.TAG,
                    "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type)!"
                )

                // DE SCHIMBAT
                val coin = bufferedReader.readLine()

                // DE SCHIMBAT
                if (coin.isNullOrEmpty()) {
                    Log.e(
                        Constants.TAG,
                        "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type)!"
                    )
                    return
                }

                // DE SCHIMBAT
                val cached = serverThread.data[coin]
                val information: Information
                var found = false
                if (cached != null) {
                    if (Instant.now().toEpochMilli() - cached.updated.toEpochMilli() < 10000) {
                        found = true
                    }
                }
                if (found) {
                    information = cached!!
                    Log.d(Constants.TAG, "FOUND IN CACHE")
                } else {
                    information = run {
                        Log.i(
                            Constants.TAG,
                            "[COMMUNICATION THREAD] Getting the information from the webservice..."
                        )

                        val httpClient = OkHttpClient()
                        // DE SCHIMBAT
                        val url =
                            "${Constants.WEB_SERVICE_ADDRESS}$coin"

                        // GET
                        val request = Request.Builder().url(url).build()

                        val httpResponse =
                            httpClient.newCall(request).execute().use { response ->
                                if (!response.isSuccessful || response.body == null) {
                                    // error
                                    return;
                                }
                                response.body!!.string()
                            }
                        val content = JSONObject(httpResponse)

                        // POST
//                    val formBody = FormBody.Builder()
//                        .add("attr1", "value1")
//                        .build()
//                    val request = Request.Builder().url("").post(formBody).build()

                        // DE SCHIMBAT POSIBIL
                        val value = content.getJSONObject("Data").getJSONObject("BTC-${coin}")
                            .getDouble("VALUE")

                        val info =
                            Information(value = value.toString(), updated = Instant.now())

                        // DE SCHIMBAT
                        serverThread.setData(coin.toString(), info)

                        // Return
                        info
                    }
                    Log.d(Constants.TAG, "FROM API CALL")
                }

                val result = information.value.toString()

                printWriter.println(result)
                printWriter.flush()
            }
        } catch (e: IOException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: ${e.message}")
        } catch (e: JSONException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: ${e.message}")
        }
    }
}