package com.example.greenleaf_v100.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityRegistroClientesBinding
import com.example.greenleaf_v100.viewmodel.RegistroClientesViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class RegistroClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroClientesBinding
    private val viewModel: RegistroClientesViewModel by viewModels()

    private val REQUEST_PERMISSIONS   = 100
    private val REQUEST_IMAGE_CAPTURE = 101
    private var photoBitmap: Bitmap?  = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // 1) Validaciones en tiempo real
        installValidation()

        // 2) DatePicker para fecha de nacimiento
        binding.etFechaNacimiento.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona tu fecha de nacimiento")
                .build()
            picker.show(supportFragmentManager, "DATE_PICKER")
            picker.addOnPositiveButtonClickListener { unixTime ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etFechaNacimiento.setText(sdf.format(Date(unixTime)))
                binding.tilFechaNacimiento.error = null
            }
        }

        // 3) Observador del ViewModel
        observeViewModel()

        // 4) Listener del botón Registrar
        setupListeners()

        // 5) Pedir permisos al arrancar
        requestRequiredPermissions()

        // 6) Cámara: botón Tomar foto
        binding.btnTakePhoto.setOnClickListener {
            if (!arePermissionsGranted()) {
                Toast.makeText(this, "Permisos necesarios", Toast.LENGTH_SHORT).show()
                requestRequiredPermissions()
                return@setOnClickListener
            }
            if (!isLocationEnabled()) {
                promptEnableLocation()
            } else {
                dispatchTakePictureIntent()
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // opcional: cerrar esta pantalla para que no regrese con "atrás"
        }

    }



    private fun installValidation() {
        // 1) Nombre/apellidos sólo letras
        val namePattern = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")
        val nameFilter = InputFilter { src, _, _, _, _, _ ->
            if (src.isNullOrEmpty()) null
            else if (namePattern.matcher(src).matches()) src
            else ""
        }
        binding.etFirstName.filters   = arrayOf(nameFilter)
        binding.etLastNamePat.filters = arrayOf(nameFilter)
        binding.etLastNameMat.filters = arrayOf(nameFilter)

        // 2) Watcher genérico para nombre/apellidos
        val nameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val txt = s?.toString().orEmpty()
                when (currentFocus?.id) {
                    binding.etFirstName.id -> binding.tilFirstName.error   = if (txt.isBlank()) "Por favor, completa este campo" else null
                    binding.etLastNamePat.id -> binding.tilLastNamePat.error = if (txt.isBlank()) "Por favor, completa este campo" else null
                    binding.etLastNameMat.id -> binding.tilLastNameMat.error = if (txt.isBlank()) "Por favor, completa este campo" else null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        binding.etFirstName.addTextChangedListener(nameWatcher)
        binding.etLastNamePat.addTextChangedListener(nameWatcher)
        binding.etLastNameMat.addTextChangedListener(nameWatcher)

        // 3) Email en tiempo real
        binding.etEmailReg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                binding.tilEmailReg.error = when {
                    email.isEmpty() -> "Por favor, completa este campo"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            || !email.endsWith(".com", ignoreCase = true)
                        -> "Correo inválido (ej: usuario@dominio.com)"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 4) Password en tiempo real
        val pwdPattern = Pattern.compile("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}\$")
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s.toString()
                binding.tilPassword.error = when {
                    pwd.isEmpty()                          -> "Por favor, completa este campo"
                    !pwdPattern.matcher(pwd).matches()     -> "Ingresa minimo 8 caracteres, 1 número y 1 especial"
                    else                                   -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 5) Confirmar contraseña
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s?.toString().orEmpty()
                val original = binding.etPassword.text.toString()
                binding.tilConfirmPassword.error = when {
                    confirm.isEmpty()      -> "Por favor, completa este campo"
                    confirm != original    -> "No coincide"
                    else                   -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 6) Fecha de nacimiento – no vacía
        binding.etFechaNacimiento.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilFechaNacimiento.error =
                    if (s.isNullOrBlank()) "Por favor, completa este campo" else null
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 7) Domicilio fiscal – no vacío
        binding.etDomicilioFiscal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilDomicilioFiscal.error =
                    if (s.isNullOrBlank()) "Por favor, completa este campo" else null
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            //Chequeo errores en todos los TextInputLayouts
            val tilList = listOf(
                binding.tilFirstName,
                binding.tilLastNamePat,
                binding.tilLastNameMat,
                binding.tilEmailReg,
                binding.tilPassword,
                binding.tilConfirmPassword,
                binding.tilFechaNacimiento,
                binding.tilDomicilioFiscal
            )
            if (tilList.any { it.error != null }) return@setOnClickListener

            //Chequeo foto tomada
            if (photoBitmap == null) {
                Toast.makeText(this, "Toma una fotografía antes de registrar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Recolectar valores
            val nombre   = binding.etFirstName.text.toString().trim()
            val paterno  = binding.etLastNamePat.text.toString().trim()
            val materno  = binding.etLastNameMat.text.toString().trim()
            val email    = binding.etEmailReg.text.toString().trim()
            val pwd      = binding.etPassword.text.toString().trim()
            val fecha    = binding.etFechaNacimiento.text.toString().trim()
            val domicilio= binding.etDomicilioFiscal.text.toString().trim()

            //Lanzar al ViewModel
            viewModel.registerCliente(
                nombre,
                paterno,
                materno,
                email,
                pwd,
                fecha,
                domicilio,
                photoBitmap!!
            )
        }
    }

    private fun observeViewModel() {
        viewModel.registroResult.observe(this) { result ->
            if (result.isSuccess) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --------------- Permisos -------------------
    private fun arePermissionsGranted(): Boolean {
        val cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val loc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return cam && loc
    }

    private fun requestRequiredPermissions() {
        val toRequest = mutableListOf<String>()
        if (!arePermissionsGranted()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                toRequest.add(Manifest.permission.CAMERA)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                toRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (arePermissionsGranted()) {
                //dispatchTakePictureIntent()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permisos requeridos")
                    .setMessage("La app necesita cámara y ubicación para funcionar.")
                    .setPositiveButton("OK") { _, _ -> requestRequiredPermissions() }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    // --------------- Ubicación -------------------
    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableLocation() {
        AlertDialog.Builder(this)
            .setTitle("Activar ubicación")
            .setMessage("Necesitamos GPS activo antes de tomar la foto.")
            .setPositiveButton("Ir a ajustes") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setCancelable(false)
            .show()
    }

    // --------------- Cámara -------------------
    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as? Bitmap
            bmp?.let {
                photoBitmap = it
                binding.ivPhotoPreview.apply {
                    setImageBitmap(it)
                    visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}
