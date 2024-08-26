package com.example.rasd

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class register_info : Fragment() {
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val user = auth.currentUser
        if (user == null) {
            Log.d("imran","1ndfail")
            // Handle the case where the user is not authenticated
            return
        }





        val userRef = db.collection("users").document(user.uid)

        val submit = view.findViewById<Button>(R.id.submit)

        submit.setOnClickListener {
            val name = view.findViewById<TextInputEditText>(R.id.name).text?.toString() ?: ""
            val email = view.findViewById<TextInputEditText>(R.id.email).text?.toString() ?: ""
            val rid = view.findViewById<TextInputEditText>(R.id.rid).text?.toString() ?: ""
            val pno = view.findViewById<TextInputEditText>(R.id.pno).text?.toString() ?: ""

            // Create the document with default data and set the flag or other info
            val newUser = hashMapOf(
                "name" to name,
                "email" to email,
                "rid" to rid,
                "pno" to pno,
            )
            userRef.set(newUser).addOnSuccessListener {
                findNavController().navigate(R.id.action_register_info_to_camera_screen)
                Log.d("imran","3ndfail")
            }.addOnFailureListener { e ->
                Log.d("imran","${e}")

            }


        }

    }

}