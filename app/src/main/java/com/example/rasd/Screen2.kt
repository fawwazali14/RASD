package com.example.rasd

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth


class Screen2 : Fragment() {

    private lateinit var webSocketManager: WebSocketManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_screen2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid
        Log.d("guava","${currentUser?.uid}")

        webSocketManager = WebSocketManager()

        // Connect to the WebSocket server
        webSocketManager.connect("wss://rasd-8bd8c.uc.r.appspot.com/ask", uid!!);


        val btn = view.findViewById<Button>(R.id.btndone)

        btn.setOnClickListener {
            webSocketManager.send("mobile","123","this is my command")
        }

    }

}