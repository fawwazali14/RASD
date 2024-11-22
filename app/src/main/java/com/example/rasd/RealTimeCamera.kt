package com.example.rasd

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RealTimeCamera.newInstance] factory method to
 * create an instance of this fragment.
 */
class RealTimeCamera : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var handler: Handler
    lateinit var cameraDevice: CameraDevice
    lateinit var bitmap: Bitmap
    lateinit var secActivityBtn: Button
    lateinit var addId: Button
    lateinit var textView7: TextView
    lateinit var warningText: TextView
    var isApiCalling: Boolean = false
    var encodedImg = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_real_time_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        get_permission()

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        textureView = view.findViewById(R.id.textureView)

        secActivityBtn = view.findViewById(R.id.skipButton)

        textView7 = view.findViewById(R.id.textView7)
        warningText = view.findViewById(R.id.warningText)


        secActivityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_real_time_camera_to_chatgpt)
        }

        addId = view.findViewById(R.id.addId)
        addId.visibility = View.GONE

        addId.setOnClickListener {
            findNavController().navigate(R.id.action_real_time_camera_to_camera_screen)
        }

        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager


        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
//                TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                TODO("Not yet implemented")
                if (!isApiCalling) {
                    bitmap = textureView.bitmap!!


                    val imageView2 = view?.findViewById<ImageView>(R.id.imageView2)
                    imageView2?.setImageBitmap(bitmap)

                    encodedImg = encodeImage(bitmap).toString()

                    getFaceDetectionResponse(encodedImg) { response ->
                        activity?.runOnUiThread {

                            if (response == "Unknown") {
                                addId.visibility = View.VISIBLE
                                warningText?.setTextColor(Color.parseColor("#999999"))
                            } else if (response == "NoFace") {
                                warningText?.setTextColor(Color.parseColor("#FFA500"))
                            } else {
                                textView7?.text = "Hello, " + response
                                secActivityBtn.text = "Next"
                                warningText?.setTextColor(Color.parseColor("#999999"))
                            }
                            addId.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }



    }

    fun getFaceDetectionResponse(encodedImg: String, callback: (String) -> Unit){
        isApiCalling = true
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
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed",e)
                isApiCalling = false
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                Log.d("error", body.toString())
                val jsonObject= JSONObject(body)
                val peronName = jsonObject.getString("name")
                isApiCalling = false
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

    @SuppressLint("MissingPermission")
    fun open_camera(){
        get_permission()
        cameraManager.openCamera(cameraManager.cameraIdList[1], object:CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                var surfaceTexture = textureView.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object:CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
//                        TODO("Not yet implemented")
                    }
                }, handler)

            }

            override fun onDisconnected(camera: CameraDevice) {
//                TODO("Not yet implemented")
            }

            override fun onError(camera: CameraDevice, error: Int) {
//                TODO("Not yet implemented")
            }
        }, handler)
    }

    fun get_permission(){
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

}