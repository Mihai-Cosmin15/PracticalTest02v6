package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.model.Information
import java.io.IOException
import java.net.ServerSocket

class ServerThread(port: Int) : Thread() {
    var serverSocket: ServerSocket? = null

    // data class Key(x: String, y: Int)
    // val data = HashMap(Key, Information?)
    // map[Key(" ", )] = ...

    // DE SCHIMBAT CHEIE
    val data = HashMap<String, Information?>()

    init {
        try {
            serverSocket = ServerSocket(port)
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.message)
        }
    }

    // DE SCHIMBAT CITY
    @Synchronized
    fun setData(coin: String, information: Information?) {
        data[coin] = information
    }

    override fun run() {
        val server = serverSocket ?: return
        try {
            while (!isInterrupted) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...")
                val socket = server.accept()
                Log.i(
                    Constants.TAG,
                    "[SERVER THREAD] A connection request was received from " + socket.inetAddress + ":" + socket.localPort
                )
                CommunicationThread(this, socket).start()
            }
        } catch (ioException: IOException) {
            Log.e(
                Constants.TAG,
                "[SERVER THREAD] An exception has occurred: " + ioException.message
            )
        }
    }

    fun stopThread() {
        interrupt()
        try {
            serverSocket?.close()
        } catch (ioException: IOException) {
            Log.e(
                Constants.TAG,
                "[SERVER THREAD] An exception has occurred: " + ioException.message
            )
        } finally {
            serverSocket = null
        }
    }
}