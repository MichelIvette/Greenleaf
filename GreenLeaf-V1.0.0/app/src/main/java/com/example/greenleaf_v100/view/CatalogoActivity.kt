package com.example.greenleaf_v100.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityCatalogoBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.model.PlantasAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale
import kotlin.random.Random

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var adapter: PlantasAdapter
    private val plantasList = mutableListOf<ModelPlanta>()
    private val plantasListFull = mutableListOf<ModelPlanta>()

    private var isAscendingOrder = true // Comienza con orden A-Z

    private var plantaDestacada: ModelPlanta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }



        if (tipoUsuario == UserType.ADMIN) {
            // Mostrar opciones de admin
            binding.barraAdmin.selectedItemId = R.id.nav_inicio
            binding.barraAdmin.visibility = View.VISIBLE
            binding.barraCliente.visibility = View.INVISIBLE
        } else if (tipoUsuario == UserType.CLIENTE) {
            // Mostrar opciones para cliente
            binding.barraCliente.selectedItemId = R.id.nav_inicio
            binding.btnAgregarPlanta.visibility = View.INVISIBLE
            binding.barraCliente.visibility = View.VISIBLE
            binding.barraAdmin.visibility = View.INVISIBLE

        } else {
            // Tipo desconocido o no recibido
        }

        setupRecyclerView()
        cargarPlantas()
        setupSearchView()
        setupSortButton()
        setupFiltros()

        binding.btnAgregarPlanta.setOnClickListener{
            val intent = Intent(this, FormActivity::class.java)

            startActivity(intent)
        }

        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_carrito -> {
                    val intent = Intent(this, CarritoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true

                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.barraAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_ventas -> {
                    val intent = Intent(this, VentasActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }




        binding.cardViewRandom.setOnClickListener {
            plantaDestacada?.let { planta ->
                val intent = Intent(this, PresentacionPlantaActivity::class.java).apply {
                    putExtra("PLANTA_ID", planta.id)
                    putExtra("NOMBRE", planta.nombre)
                    putExtra("DESCRIPCION", planta.descripcion)
                    putExtra("FOTO_URL", planta.fotoUrl)
                    putExtra("TIPO", planta.tipo)
                    putExtra("ESTANCIA", planta.estancia)
                    putExtra("RIEGO", planta.riego)
                    putExtra("CONSEJO", planta.consejo)
                    putExtra("STOCK", planta.stock)
                }
                startActivity(intent)
            }
        }

    }

    private fun setupRecyclerView() {
        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }
        val esAdmin = tipoUsuario == UserType.ADMIN

        adapter = PlantasAdapter(
            plantasList,
            onItemClick = { planta ->
                val intent = Intent(this, PresentacionPlantaActivity::class.java).apply {
                    putExtra("PLANTA_ID", planta.id)
                    putExtra("NOMBRE", planta.nombre)
                    putExtra("DESCRIPCION", planta.descripcion)
                    putExtra("FOTO_URL", planta.fotoUrl)
                    putExtra("TIPO", planta.tipo)
                    putExtra("ESTANCIA", planta.estancia)
                    putExtra("RIEGO", planta.riego)
                    putExtra("CONSEJO", planta.consejo)
                    putExtra("STOCK", planta.stock)
                    putExtra("PRECIO",planta.precio)
                }
                startActivity(intent)
            },
            onDeleteClick = { planta ->
                eliminarPlanta(planta)
            },
            onFavoritoClick = { planta, nuevoEstado -> toggleFavorito(planta, nuevoEstado) },
            onEditClick = { planta -> // Nuevo listener
                editarPlanta(planta)
            },
            isAdmin = tipoUsuario == UserType.ADMIN
        )


        // GridLayoutManager con 2 columnas
        val gridLayoutManager = GridLayoutManager(this, 2) // <- Número de columnas
        with(binding.recyclerView) {
            layoutManager = gridLayoutManager // Asigna el GridLayoutManager
            adapter = this@CatalogoActivity.adapter

        }
    }

    private fun cargarPlantas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val refFavoritos = db.collection("favoritos").document(userId).collection("plantasFavoritas")
        val refPlantas = db.collection("plantas")

        // Primero obtener los favoritos del usuario
        refFavoritos.get().addOnSuccessListener { favSnapshot ->
            val favoritosIds = favSnapshot.documents.map { it.id }

            // Luego obtener todas las plantas
            refPlantas.get()
                .addOnSuccessListener { result ->
                    plantasList.clear()
                    plantasListFull.clear()

                    for (document in result) {
                        val planta = document.toObject(ModelPlanta::class.java).apply {
                            id = document.id
                            esFavorito = favoritosIds.contains(document.id)
                        }
                        plantasList.add(planta)
                        plantasListFull.add(planta)
                    }

                    sortPlantasAZ()
                    adapter.notifyDataSetChanged()

                    if (plantasList.isNotEmpty()) {
                        val indiceAleatorio = (0 until plantasList.size).random()
                        val planta = plantasList[indiceAleatorio]
                        plantaDestacada = planta
                        binding.tvNombrePC.text = planta.nombre
                        binding.tvDescripcionC.text = planta.descripcion
                        Glide.with(this).load(planta.fotoUrl).into(binding.ivPlantaC)
                    }

                    //binding.tvEmptyView.visibility = if (plantasList.isEmpty()) View.VISIBLE else View.GONE
                }


        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al cargar favoritos: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarPlanta(planta: ModelPlanta) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Estás seguro de que deseas eliminar esta planta?")
            .setPositiveButton("Sí") { _, _ ->

                // 1. Obtener referencia de la imagen desde la URL
                planta.fotoUrl?.let { url ->
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

                    // 2. Eliminar la imagen
                    storageRef.delete()
                        .addOnSuccessListener {
                            // 3. Si se elimina la imagen, eliminar el documento en Firestore
                            FirebaseFirestore.getInstance().collection("plantas")
                                .document(planta.id!!)
                                .delete()
                                .addOnSuccessListener {
                                    plantasList.remove(planta)
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(this, "Planta eliminada", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al eliminar planta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar imagen", Toast.LENGTH_SHORT)
                                .show()
                        }
                } ?: run {
                    // Si no tiene imagen (url null), solo eliminar de Firestore
                    FirebaseFirestore.getInstance().collection("plantas")
                        .document(planta.id!!)
                        .delete()
                        .addOnSuccessListener {
                            plantasList.remove(planta)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(
                                this,
                                "Planta eliminada (sin imagen)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar planta", Toast.LENGTH_SHORT)
                                .show()
                        }
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPlantas(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            if (isAscendingOrder) {
                sortPlantasZA() // Orden Z-A
                binding.btnSort.setImageResource(R.drawable.a_z) // Icono Z-A
            } else {
                sortPlantasAZ() // Orden A-Z
                binding.btnSort.setImageResource(R.drawable.z_a) // Icono A-Z
            }
            isAscendingOrder = !isAscendingOrder // Alterna el estado
        }
    }

    private fun filterPlantas(text: String) {
        val filteredList = if (text.isEmpty()) {
            plantasListFull.toList()
        } else {
            plantasListFull.filter { planta ->
                planta.nombre?.contains(text, ignoreCase = true) == true ||
                        planta.descripcion?.contains(text, ignoreCase = true) == true ||
                        planta.tipo?.contains(text, ignoreCase = true) == true
            }
        }


        plantasList.clear()
        plantasList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }




    private fun sortPlantasAZ() {
        plantasList.sortBy { it.nombre?.lowercase(Locale.getDefault()) ?: "" }
        adapter.notifyDataSetChanged()
    }

    private fun sortPlantasZA() {
        plantasList.sortByDescending { it.nombre?.lowercase(Locale.getDefault()) ?: "" }
        adapter.notifyDataSetChanged()
    }


    private fun actualizarFavorito(planta: ModelPlanta, esFavorito: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val favRef = db.collection("favoritos").document(userId).collection("plantasFavoritas")

        if (esFavorito) {
            // Agregar planta a favoritos
            favRef.document(planta.id).set(planta)
                .addOnSuccessListener {
                    Toast.makeText(this, "${planta.nombre} agregada a favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al agregar favorito", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Eliminar de favoritos
            favRef.document(planta.id).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "${planta.nombre} eliminada de favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
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

        // Actualizar estado local
        planta.esFavorito = nuevoEstado
        val index = plantasList.indexOfFirst { it.id == planta.id }
        if (index != -1) {
            adapter.notifyItemChanged(index)
        }

        if (nuevoEstado) {
            favRef.set(mapOf("timestamp" to FieldValue.serverTimestamp()))
        } else {
            favRef.delete()
        }
    }

    private fun editarPlanta(planta: ModelPlanta) {
        val intent = Intent(this, FormActivity::class.java).apply {
            putExtra("MODO_EDICION", true)
            putExtra("PLANTA_ID", planta.id)
            putExtra("NOMBRE", planta.nombre)
            putExtra("DESCRIPCION", planta.descripcion)
            putExtra("PRECIO", planta.precio)
            putExtra("FOTO_URL", planta.fotoUrl)
            putExtra("TIPO", planta.tipo)
            putExtra("ESTANCIA", planta.estancia)
            putExtra("RIEGO", planta.riego)
            putExtra("CONSEJO", planta.consejo)
            putExtra("STOCK", planta.stock)
        }
        startActivity(intent)
    }

    private fun setupFiltros() {

        binding.btnFiltroRiegoBajo.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroRiegoBajo.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.riego.equals("bajo", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()

        }

        binding.btnFiltroRiegoAlto.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroRiegoAlto.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.riego.equals("alto", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }

        binding.btnFiltroInterior.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroInterior.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.estancia.equals("interior", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }

        binding.btnFiltroExterior.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroExterior.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.estancia.equals("exterior", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }

        binding.btnFiltroSol.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroSol.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.tipo.equals("sol", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }
        binding.btnFiltroSombra.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroSombra.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.tipo.equals("sombra", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }
        binding.btnFiltroSolySombra.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroSolySombra.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))
            val filtradas = plantasListFull.filter { it.tipo.equals("sombra parcial", ignoreCase = true) }
            plantasList.clear()
            plantasList.addAll(filtradas)
            adapter.notifyDataSetChanged()
        }
        binding.btnFiltroLimpiar.setOnClickListener {
            resetearColoresFiltros()
            binding.cdFiltroLimpiar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.greenA))

            plantasList.clear()
            plantasList.addAll(plantasListFull) // Restaura la lista de plantas
            adapter.notifyDataSetChanged()
            resetearColoresFiltros()
        }
    }

    private fun resetearColoresFiltros() {
        val blanco = ContextCompat.getColor(this, R.color.white)
        binding.cdFiltroRiegoBajo.setCardBackgroundColor(blanco)
        binding.cdFiltroRiegoAlto.setCardBackgroundColor(blanco)
        binding.cdFiltroInterior.setCardBackgroundColor(blanco)
        binding.cdFiltroExterior.setCardBackgroundColor(blanco)
        binding.cdFiltroSol.setCardBackgroundColor(blanco)
        binding.cdFiltroSombra.setCardBackgroundColor(blanco)
        binding.cdFiltroSolySombra.setCardBackgroundColor(blanco)
        binding.cdFiltroLimpiar.setCardBackgroundColor(blanco)
    }


}