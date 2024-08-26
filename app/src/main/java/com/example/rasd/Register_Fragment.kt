package com.example.rasd

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class Register_Fragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val submit = view.findViewById<Button>(R.id.submit)

        submit.setOnClickListener {
            val name = view.findViewById<TextInputEditText>(R.id.name).text?.toString()
            val email = view.findViewById<TextInputEditText>(R.id.email).text?.toString()
            val password = view.findViewById<TextInputEditText>(R.id.password).text?.toString()
            val confirmPassword = view.findViewById<TextInputEditText>(R.id.confirmpass).text?.toString()
            val rid = view.findViewById<TextInputEditText>(R.id.rid).text?.toString() ?: ""
            val pno = view.findViewById<TextInputEditText>(R.id.pno).text?.toString() ?: ""

            // Create the document with default data and set the flag or other info
            val newUser = hashMapOf(
                "name" to name,
                "email" to email,
                "rid" to rid,
                "pno" to pno,
            )

            if (!isValidEmail(email!!)) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createAccount(email, password!!,newUser)
        }


    }
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun createAccount(email: String, password: String, data : HashMap<String,String?>) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userRef = Firebase.firestore.collection("users").document(user!!.uid)
                    userRef.set(data).addOnSuccessListener {
                        findNavController().navigate(R.id.action_register_Fragment_to_camera_screen)
                    }.addOnFailureListener { e ->
                        Log.d("error","${e}")

                    }
                } else {
                    // If account creation fails, display a message to the user.
                    Toast.makeText(requireContext(), "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}