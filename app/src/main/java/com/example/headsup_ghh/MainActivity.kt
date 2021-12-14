package com.example.headsup_ghh

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.headsup_ghh.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
private lateinit var binding : ActivityMainBinding
private var isStart = false
private  var time = 60000L
 private lateinit var timer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        println("create")
        supportActionBar?.hide()

        binding.btnStart.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            object : CountDownTimer(5000, 1000) {

                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTitle.text = "Game Start in : ${millisUntilFinished / 1000}"
                }

                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    isStart = true
                    startGame()
                }
            }.start()
        }
        binding.btnRestart.setOnClickListener {
            endGame()
        }
    }

    // it will call to check if the user playing or not
    private fun startGame(){
        if (isStart) {
            binding.btnStart.isVisible = false
            binding.tvTitle.isVisible = false
            binding.tvRotate.isVisible = true
            binding.btnRestart.isVisible = true
        }else {
            binding.btnStart.isVisible = true
            binding.tvTitle.isVisible = true
            binding.tvRotate.isVisible = false
            binding.btnRestart.isVisible = false
        }
    }
    // calls if timer ends
    private fun endGame(){
        binding.btnStart.isVisible = true
        binding.tvTitle.isVisible = true
        binding.tvRotate.isVisible = false
        binding.btnRestart.isVisible = false
        isStart = false
        time = 60000L
        saveData(isStart,time)
    }
    //function takes Long time and start the timer with it
    private fun timer(start : Long){
        timer = object : CountDownTimer(start, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Time : ${millisUntilFinished / 1000}"
                time = millisUntilFinished
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.tvTimer.text = "done!"
                endGame()
                time = 60000L
            }
        }.start()

    }

    // it will retrieve all data if user playing and handle screen orientation
    override fun onStart() {
        super.onStart()
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        isStart = sharedPref.getBoolean("isPlaying", false)
        time = sharedPref.getLong("timer",60000L)
        if (isStart){timer(time)}
        startGame()
        val orientation = this.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(isStart){
                binding.tvRotate.isVisible = false
                binding.llQuestion.isVisible = true
                getRandomQuestion()
                timer(time)
            }
        } else  if (orientation == Configuration.ORIENTATION_PORTRAIT){
            if (isStart) {
                binding.tvRotate.isVisible = true
                binding.llQuestion.isVisible = false
                timer.cancel()
            }
        }
    }

// function to save sharedPreference data
    private fun saveData(isDone : Boolean,isTime:Long){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("isPlaying", isDone)
            putLong("timer",isTime)
            apply()
        }
    }
// it will call the saveData before activity destroy
    override fun onDestroy() {
        super.onDestroy()
        saveData(isStart,time)
    }

    // function request from the api data and set randomly to the elements
    private fun getRandomQuestion(){
        val api = Client().requestClient()?.create(GetAPI::class.java)
        api?.GetData()?.enqueue(object: Callback<Celebrities> {
            override fun onResponse(call: Call<Celebrities>, response: Response<Celebrities>) {
                val data = response.body()!!
                val randomCeleb = Random.nextInt(0 ,data.size - 1)
                println(":)  ${data[randomCeleb]}")
                binding.tvName.text = data[randomCeleb].name
                binding.tv1.text = data[randomCeleb].taboo1
                binding.tv2.text = data[randomCeleb].taboo2
                binding.tv3.text = data[randomCeleb].taboo3

            }

            override fun onFailure(call: Call<Celebrities>, t: Throwable) {
               Toast.makeText(this@MainActivity,"Something went wrong while fetching the data",Toast.LENGTH_LONG).show()
            }
        })

    }

}