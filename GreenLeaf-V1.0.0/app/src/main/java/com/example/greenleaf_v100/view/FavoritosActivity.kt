package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityFavoritosBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.model.PlantasAdapter
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var adapter: PlantasAdapter
    private val favoritos = mutableListOf<ModelPlanta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar RecyclerView y adapter
        adapter = PlantasAdapter(
            plantas = favoritos,
            onItemClick = { planta ->
                // Puedes abrir una vista detallada si quieres
            },
            onDeleteClick = {}, // No aplica aquí
            onFavoritoClick = { planta, nuevoEstado ->
                toggleFavorito(planta, nuevoEstado)
            },
            isAdmin = false
        )

        val gridLayoutManager = GridLayoutManager(this, 2)
        with(binding.recyclerFavoritos) {
            layoutManager = gridLayoutManager // Asigna el GridLayoutManager
            adapter = this@FavoritosActivity.adapter

        }

        binding.recyclerFavoritos.adapter = adapter

        // Mostrar la barra de navegación
        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.CLIENTE) {
            binding.barraCliente.selectedItemId = R.id.nav_favoritos
            binding.barraCliente.visibility = View.VISIBLE
        }

        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, CatalogoActivity::class.java).apply {
                        putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    })
                    true
                }

                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java).apply {
                        putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    })
                    true
                }

                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java).apply {
                        putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    })
                    true
                }

                R.id.nav_favoritos -> true
                else -> false
            }
        }

        obtenerFavoritos()
    }

    private fun obtenerFavoritos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favoritosRef = FirebaseFirestore.getInstance()
            .collection("favoritos")
            .document(uid)
            .collection("plantasFavoritas")

        favoritosRef.get().addOnSuccessListener { docs ->
            val idsFavoritos = docs.map { it.id }

            if (idsFavoritos.isEmpty()) {
                favoritos.clear()
                adapter.notifyDataSetChanged()
                return@addOnSuccessListener
            }

            FirebaseFirestore.getInstance()
                .collection("plantas")
                .whereIn(FieldPath.documentId(), idsFavoritos)
                .get()
                .addOnSuccessListener { plantasDocs ->
                    favoritos.clear()
                    for (doc in plantasDocs) {
                        val planta = doc.toObject(ModelPlanta::class.java).apply {
                            id = doc.id
                            esFavorito = true
                        }
                        favoritos.add(planta)
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun toggleFavorito(planta: ModelPlanta, nuevoEstado: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseFirestore.getInstance()
            .collection("favoritos")
            .document(uid)
            .collection("plantasFavoritas")
            .document(planta.id)

        if (nuevoEstado) {
            // Agregar a favoritos
            favRef.set(mapOf("timestamp" to FieldValue.serverTimestamp()))
        } else {
            // Quitar de favoritos
            favRef.delete().addOnSuccessListener {
                // Eliminar de la lista local
                favoritos.removeAll { it.id == planta.id }
                adapter.notifyDataSetChanged()
            }
        }
    }
 }

