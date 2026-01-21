package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import android.widget.TextView
import java.io.IOException
import java.net.Socket
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities

class ClientThread(
    private val address: String,
    private val port: Int,
    // DE SCHIMBAT
    private val coin: String,
    private val informationTextView: TextView
) : Thread() {
    override fun run() {
        try {
            Socket(address, port).use { socket ->
                val bufferedReader = Utilities.getReader(socket)
                val printWriter = Utilities.getWriter(socket)

                printWriter.println(coin)
                printWriter.flush()

                while (true) {
                    val line = bufferedReader.readLine() ?: break
                    informationTextView.post {
                        informationTextView.text = line.replace(",", ",\n")
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e(
                Constants.TAG,
                "[CLIENT THREAD] An exception has occurred: " + ioException.message
            )
        }
    }
}