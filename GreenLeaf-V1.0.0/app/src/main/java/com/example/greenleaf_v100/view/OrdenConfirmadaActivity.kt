package com.example.greenleaf_v100.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.R

class OrdenConfirmadaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orden_confirmada)

        val btnRegresar = findViewById<Button>(R.id.btnRegresarCatalogo)
        val linkMapa = findViewById<TextView>(R.id.linkMapa)

        // Abrir enlace al dar clic
        linkMapa.setOnClickListener {
            val uri = Uri.parse("https://maps.app.goo.gl/DVzgU8tdUb5UFSoXA")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        btnRegresar.setOnClickListener {
            val intent = Intent(this, CatalogoActivity::class.java)
            intent.putExtra("TIPO_USUARIO", "CLIENTE")
            startActivity(intent)
            finish()
        }
    }
}
