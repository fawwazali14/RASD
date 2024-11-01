package com.example.rasd

import android.util.Log
import com.example.rasd.data.Data
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(url: String, token : String ) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                super.onOpen(webSocket, response)
                val temp : Data = Data("temp","none","Conection established")
                val jsonMessage = Gson().toJson(temp)
                webSocket.send(jsonMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("Received text message: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                println("Received binary message: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)

                Log.d("WebSocket is closing","${code} / ${reason}")

                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                super.onFailure(webSocket, t, response)
                Log.d("WebSocket","${t.message}")

            }
        })
    }

    fun send(t: String, sno: String,cmd : String) {
        val message : Data = Data(t,sno,cmd)
        val jsonMessage = Gson().toJson(message)
        webSocket?.send(jsonMessage)
    }

    fun close() {
        webSocket?.close(1000, "Goodbye")
    }
}
