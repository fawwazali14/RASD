package com.example.rasd

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class Home_Fragment : Fragment() {
    private lateinit var webSocketManager: WebSocketManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webSocketManager = WebSocketManager()


        // Connect to the WebSocket server
        webSocketManager.connect("wss://rasd-8bd8c.uc.r.appspot.com/ask", "deedced");


        val up = view.findViewById<Button>(R.id.button_up)
        val down = view.findViewById<Button>(R.id.down)

        val left = view.findViewById<Button>(R.id.left)

        val right = view.findViewById<Button>(R.id.right)


        up.setOnClickListener {
            webSocketManager.send("mobile","123","up")
        }
        down.setOnClickListener {
            webSocketManager.send("mobile","123","down")

        }
        left.setOnClickListener {
            webSocketManager.send("mobile","123","left")
        }
        right.setOnClickListener {
            webSocketManager.send("mobile","123","right")
        }

    }


}