package com.example.rasd

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Objects

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatGPTFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatGPTFragment : Fragment(), TextToSpeech.OnInitListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val client = OkHttpClient()


    lateinit var button: Button
    lateinit var editText: EditText
    lateinit var question: String
    lateinit var reply: String
    lateinit var textView: TextView

    // Text to speech
    private var tts: TextToSpeech? = null
    lateinit var  buttonSpeak: Button


    // Speech to audio
    lateinit var micIV: ImageView
    private val REQUEST_CODE_SPEECH_INPUT = 1

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_chat_g_p_t, container, false)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button = view.findViewById(R.id.button)
        editText = view.findViewById(R.id.editText)
        textView = view.findViewById(R.id.textView_gpt)
        button.setOnClickListener {
            question = editText.text.toString()
            getResponse(question){response ->
                activity?.runOnUiThread {
                    textView.text = response
                    reply = response

                    speakOut()


                    buttonSpeak.visibility = View.VISIBLE;
                }
            }


        }

        // Text to speech
        buttonSpeak =  view.findViewById<Button>(R.id.button2)
//        buttonSpeak.visibility = View.GONE;

        buttonSpeak!!.isEnabled = false;
        tts = TextToSpeech(getActivity(), this)

        buttonSpeak!!.setOnClickListener { speakOut() }

        // Speech to text
        micIV = view.findViewById(R.id.idIVMic)

        micIV.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {

                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_g_p_t, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatGPTFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatGPTFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // Text to speech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                buttonSpeak!!.isEnabled = true
            }
        }
    }

    // Speech to text
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {

                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                editText.setText(
                    Objects.requireNonNull(res)[0]
                )

                reply = Objects.requireNonNull(res)[0]
            }
        }
    }
    private fun speakOut() {
        val text = reply
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    fun getResponse(question: String, callback: (String) -> Unit){

        val apiKey = "apikey"
        val url = "https://api.openai.com/v1/completions"

        val requestBody = """
            {
            "model": "gpt-3.5-turbo-instruct",
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {

                    val body=response.body?.string()

                    if(body!=null) {
                        Log.v("data", body)
                    } else {
                        Log.v("data", "empty")
                    }
                    val jsonObject= JSONObject(body)
                    val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                    val textResult=jsonArray.getJSONObject(0).getString("text")
                    callback(textResult)
                } catch (e: Exception) {
                    Log.e("error", "something with chatgpt",e)
                }
            }

        })
    }
}