package com.example.rasd

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import androidx.navigation.fragment.findNavController


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [camera_screen.newInstance] factory method to
 * create an instance of this fragment.
 */
//class camera_screen : Fragment() {
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_camera_screen, container, false)
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment camera_screen.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            camera_screen().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}


class camera_screen : Fragment() {
    // TODO: Rename and change types of parameters
    lateinit var secActivityBtn: Button
    lateinit var personName: EditText
    val IMAGE_CAPTURE_REQUEST = 1
    var encodedImg = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Got to next page
        secActivityBtn = view.findViewById(R.id.secActivityBtn)
        secActivityBtn.visibility = View.GONE;

        val textView2 = view.findViewById<TextView>(R.id.textView2)
        textView2.visibility = View.GONE;

        val editMessage = view.findViewById<EditText>(R.id.editTextTextPersonName3)
        editMessage.visibility = View.GONE;

        val submitBtn = view.findViewById<Button>(R.id.submitBtn)
        submitBtn.visibility = View.GONE;


        secActivityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_camera_screen_to_chatgpt)
        }

        // Integrate camera
        get_permission()

        val captureButton = view.findViewById<Button>(R.id.captureButton)

        captureButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST)
            } else {
                Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_LONG)
            }
        }

        submitBtn.setOnClickListener {
            textView2.text = "Processing ... "
            textView2.visibility = View.VISIBLE;
            editMessage.visibility = View.GONE;
            submitBtn.visibility = View.GONE;

            if (editMessage.text.toString().isNotEmpty()){
                getFaceSaveResponse(encodedImg, editMessage.text.toString()) {response ->
                        activity?.runOnUiThread {
                            secActivityBtn.visibility = View.VISIBLE;
                            textView2.text = "Hello, " + response
                            textView2.visibility = View.VISIBLE;
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Enter name here...", Toast.LENGTH_LONG).show()
            }

        }
    }


    fun get_permission(){
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageView = view?.findViewById<ImageView>(R.id.imageView)
        val textView2 = view?.findViewById<TextView>(R.id.textView2)
        val editMessage = view?.findViewById<EditText>(R.id.editTextTextPersonName3)
        val submitBtn = view?.findViewById<Button>(R.id.submitBtn)


        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            imageView?.setImageBitmap(imageBitmap)

            encodedImg = encodeImage(imageBitmap).toString()

            textView2?.text = "Processing ... "
            textView2?.visibility = View.VISIBLE;

            getFaceDetectionResponse(encodedImg){response ->
                    activity?.runOnUiThread {
                        if (response == "Unknown"){
                            textView2?.text =  "Please Enter your name"

                            editMessage?.visibility = View.VISIBLE;
                            submitBtn?.visibility = View.VISIBLE;
                        } else if (response == "NoFace") {
                            textView2?.text =  "Please make sure, your face is clearly visible"
                        } else {
                            secActivityBtn.visibility = View.VISIBLE;
                            textView2?.text =  "Hello, " + response
                        }
                            textView2?.visibility = View.VISIBLE;
                    }
            }
        } else {

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getFaceDetectionResponse(encodedImg: String, callback: (String) -> Unit){


        val requestBody = """
                {
                "encodedImg": "$encodedImg"
                }
            """.trimIndent()

        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .url("http://srv599793.hstgr.cloud:8000/status/1/face_recognition/")
            .build()
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                Log.d("error", body.toString())
                val jsonObject= JSONObject(body)
                val peronName = jsonObject.getString("name")

                callback(peronName)

            }

        } )

    }

    
    fun getFaceSaveResponse(encodedImg: String, name: String, callback: (String) -> Unit){

        val requestBody = """
                {
                "encodedImg": "$encodedImg",
                "name": "$name"
                }
            """.trimIndent()

        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .url("http://srv599793.hstgr.cloud:8000/status/1/save_face/")
            .build()
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                val jsonObject= JSONObject(body)
                val peronName = jsonObject.getString("name")

                callback(peronName)

            }

        } )

    }

    fun encodeImage(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        return imageString.replace("\n", "");
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            get_permission()
        }
    }

}
