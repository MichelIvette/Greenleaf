package com.example.greenleaf_v100.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityPerfilBinding
import com.example.greenleaf_v100.viewmodel.PerfilViewModel
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var viewModel: PerfilViewModel

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.actualizarFotoPerfil(uri) { ok, err ->
                    if (ok) Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[PerfilViewModel::class.java]

        observeViewModel()
        setupListeners()
        setupNavBar()

        binding.btnRegisterAdmin.setOnClickListener {
            startActivity(Intent(this, RegistroAdminActivity::class.java))
        }


    }

    private fun observeViewModel() {
        viewModel.userName.observe(this) {
            binding.tvGreeting.text = getString(R.string.hola_usuario, it)
        }
        viewModel.email.observe(this) {
            binding.tvEmail.text = it
        }
        viewModel.role.observe(this) {
            binding.tvRole.text = it
        }
        viewModel.photoUri.observe(this) { uri ->
            if (uri != null) Glide.with(this).load(uri).circleCrop().into(binding.ivProfile)
            else binding.ivProfile.setImageResource(R.drawable.usuario_foto)
        }
        viewModel.passwordMasked.observe(this) {
            binding.tvPassword.text = it
        }
        // extras:
        viewModel.firstName.observe(this) {
            binding.tvFirstName.text = it
        }
        viewModel.lastNamePat.observe(this) {
            binding.tvLastNamePat.text = it
        }
        viewModel.lastNameMat.observe(this) {
            binding.tvLastNameMat.text = it
        }
        viewModel.fechaNacimiento.observe(this) {
            binding.tvFechaNacimiento.text = it
        }
        viewModel.domicilioFiscal.observe(this) {
            binding.tvDomicilioFiscal.text = it
        }
        viewModel.errorMessage.observe(this) {
            it?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        // cambiar foto
        binding.fabEditPhoto.setOnClickListener {
            pickImageLauncher.launch(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            )
        }
        // cambiar correo
        binding.btnEditEmail.setOnClickListener {
            showFieldDialog(
                title = "Cambiar correo",
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                current = viewModel.email.value.orEmpty()
            ) { new ->
                viewModel.actualizarCorreo(new) { ok, err ->
                    if (ok) Toast.makeText(this, "Correo actualizado", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // cambiar contraseña
        binding.btnEditPwd.setOnClickListener {
            showPasswordDialog()
        }
        // cambiar nombre
        binding.btnEditFirstName.setOnClickListener {
            showFieldDialog("Cambiar nombre", InputType.TYPE_CLASS_TEXT, viewModel.firstName.value.orEmpty()) { new ->
                viewModel.actualizarCampo("nombre", new) { ok, err ->
                    if (ok) Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // apellido paterno
        binding.btnEditLastNamePat.setOnClickListener {
            showFieldDialog("Cambiar apellido paterno", InputType.TYPE_CLASS_TEXT, viewModel.lastNamePat.value.orEmpty()) { new ->
                viewModel.actualizarCampo("paterno", new) { ok, err ->
                    if (ok) Toast.makeText(this, "Apellido paterno actualizado", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // apellido materno
        binding.btnEditLastNameMat.setOnClickListener {
            showFieldDialog("Cambiar apellido materno", InputType.TYPE_CLASS_TEXT, viewModel.lastNameMat.value.orEmpty()) { new ->
                viewModel.actualizarCampo("materno", new) { ok, err ->
                    if (ok) Toast.makeText(this, "Apellido materno actualizado", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // fecha de nacimiento
        binding.btnEditFechaNacimiento.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona fecha de nacimiento")
                .build()
            picker.show(supportFragmentManager, "DATEPICKER_PERFIL")
            picker.addOnPositiveButtonClickListener { unix ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val str = sdf.format(Date(unix))
                viewModel.actualizarCampo("fechaNacimiento", str) { ok, err ->
                    if (ok) Toast.makeText(this, "Fecha actualizada", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // domicilio fiscal
        binding.btnEditDomicilioFiscal.setOnClickListener {
            showFieldDialog("Cambiar domicilio fiscal", InputType.TYPE_CLASS_TEXT, viewModel.domicilioFiscal.value.orEmpty()) { new ->
                viewModel.actualizarCampo("domicilioFiscal", new) { ok, err ->
                    if (ok) Toast.makeText(this, "Domicilio actualizado", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
        // eliminar registro
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar registro")
                .setMessage("¿Estás seguro de que deseas eliminar tu registro?")
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.eliminarRegistro { ok, err ->
                        if (ok) {
                            Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show()
                            viewModel.logout()
                            startActivity(Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                        } else {
                            Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
        // cerrar sesión
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun setupNavBar() {
        val tipoStr = intent.getStringExtra("TIPO_USUARIO")
        val tipo = tipoStr?.let { UserType.valueOf(it) }
        if (tipo == UserType.ADMIN) {
            binding.barraAdmin.visibility = View.VISIBLE
            binding.barraAdmin.selectedItemId = R.id.nav_perfil
            binding.barraCliente.visibility = View.INVISIBLE
            binding.btnRegisterAdmin.visibility = View.VISIBLE
        } else {
            binding.barraCliente.visibility = View.VISIBLE
            binding.barraCliente.selectedItemId = R.id.nav_perfil
            binding.barraAdmin.visibility = View.INVISIBLE
            binding.btnRegisterAdmin.visibility = View.GONE
        }
        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> navigateCatalogo(tipo)
                R.id.nav_carrito -> navigateCarrito(tipo)
                R.id.nav_perfil -> true
                R.id.nav_favoritos -> navigateFavoritos(tipo)
                else -> false
            }
        }
        binding.barraAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> navigateCatalogo(tipo)
                R.id.nav_ventas -> navigateVentas(tipo)
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }

    private fun navigateCatalogo(tipo: UserType?) = startActivity(
        Intent(this, CatalogoActivity::class.java)
            .putExtra("TIPO_USUARIO", tipo?.name)
    ).let { true }

    private fun navigateCarrito(tipo: UserType?) = startActivity(
        Intent(this, CarritoActivity::class.java)
            .putExtra("TIPO_USUARIO", tipo?.name)
    ).let { true }

    private fun navigateFavoritos(tipo: UserType?) = startActivity(
        Intent(this, FavoritosActivity::class.java)
            .putExtra("TIPO_USUARIO", tipo?.name)
    ).let { true }

    private fun navigateVentas(tipo: UserType?) = startActivity(
        Intent(this, VentasActivity::class.java)
            .putExtra("TIPO_USUARIO", tipo?.name)
    ).let { true }

    private fun showFieldDialog(
        title: String,
        inputType: Int,
        current: String,
        onConfirm: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(this).setTitle(title)
        val input = EditText(this).apply {
            hint = title
            setText(current)
            this.inputType = inputType
        }
        builder.setView(input)
            .setPositiveButton("OK") { dialog, _ ->
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    input.error = "$title no puede quedar vacío"
                } else {
                    onConfirm(text)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showPasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = view.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = view.findViewById<EditText>(R.id.etNewPassword)
        AlertDialog.Builder(this)
            .setTitle("Cambiar contraseña")
            .setView(view)
            .setPositiveButton("OK") { dialog, _ ->
                val curr = etCurrent.text.toString()
                val nw = etNew.text.toString()
                if (curr.isBlank() || nw.length < 6) {
                    if (curr.isBlank()) etCurrent.error = "Requerido"
                    if (nw.length < 6) etNew.error = "Mínimo 6 caracteres"
                } else {
                    viewModel.actualizarContrasena(curr, nw) { ok, err ->
                        if (ok) Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}
