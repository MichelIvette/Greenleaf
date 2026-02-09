package com.example.greenleaf_v100.view


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityStartBinding
import android.content.Intent
import android.os.Handler
import android.os.Looper


class StartActivity : AppCompatActivity() {

    companion object {
        private const val START_DELAY = 2000L
    }

    //Referencia a activity_login
    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_start)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            // Tras 3 s, ir al login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  // evitar volver al splash con “Atrás”
        }, START_DELAY)
    }

}