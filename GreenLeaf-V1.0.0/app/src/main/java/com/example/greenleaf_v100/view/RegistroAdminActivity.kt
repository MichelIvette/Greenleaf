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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greenleaf_v100.databinding.ActivityRegistroAdminBinding
import com.example.greenleaf_v100.viewmodel.RegistroAdminViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class RegistroAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroAdminBinding
    private val viewModel: RegistroAdminViewModel by viewModels()

    private val REQUEST_PERMISSIONS    = 200
    private val REQUEST_IMAGE_CAPTURE  = 201
    private var photoBitmap: Bitmap?   = null
    private var launchAfterPerms = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installValidation()

        // DatePicker
        binding.etFechaNacimientoAdmin.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona tu fecha de nacimiento")
                .build()
            picker.show(supportFragmentManager, "DATE_PICKER_ADMIN")
            picker.addOnPositiveButtonClickListener { unixTime ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etFechaNacimientoAdmin.setText(sdf.format(Date(unixTime)))
                binding.tilFechaNacimientoAdmin.error = null
            }
        }

        observeViewModel()
        setupListeners()
        requestRequiredPermissions()

        // Cámara
        binding.btnTakePhotoAdmin.setOnClickListener {
            if (!arePermissionsGranted()) {
                launchAfterPerms = true
                requestRequiredPermissions()
                return@setOnClickListener
            }
            if (!isLocationEnabled()) {
                promptEnableLocation()
            } else {
                dispatchTakePictureIntent()
            }
        }

        binding.btnBackToLoginAdmin.setOnClickListener {
            finish() // vuelve atras
        }
    }

    private fun installValidation() {
        // 1) Letras y espacios
        val namePattern = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")
        val nameFilter = InputFilter { src, _, _, _, _, _ ->
            if (src.isNullOrEmpty()) null
            else if (namePattern.matcher(src).matches()) src
            else ""
        }
        binding.etFirstNameAdmin.filters   = arrayOf(nameFilter)
        binding.etLastNamePatAdmin.filters = arrayOf(nameFilter)
        binding.etLastNameMatAdmin.filters = arrayOf(nameFilter)

        // 2) Watcher genérico
        val nameWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val t = s.toString().trim()
                when (currentFocus?.id) {
                    binding.etFirstNameAdmin.id   -> binding.tilFirstNameAdmin.error   = if (t.isEmpty()) "Por favor, completa este campo" else null
                    binding.etLastNamePatAdmin.id -> binding.tilLastNamePatAdmin.error = if (t.isEmpty()) "Por favor, completa este campo" else null
                    binding.etLastNameMatAdmin.id -> binding.tilLastNameMatAdmin.error = if (t.isEmpty()) "Por favor, completa este campo" else null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        binding.etFirstNameAdmin.addTextChangedListener(nameWatcher)
        binding.etLastNamePatAdmin.addTextChangedListener(nameWatcher)
        binding.etLastNameMatAdmin.addTextChangedListener(nameWatcher)

        // 3) Email
        binding.etEmailAdmin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val mail = s.toString().trim()
                binding.tilEmailAdmin.error = when {
                    mail.isEmpty() -> "Por favor, completa este campo"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()
                            || !mail.endsWith(".com", ignoreCase = true)
                        -> "Correo inválido"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 4) Password
        val pwdPattern = Pattern.compile("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}\$")
        binding.etPasswordAdmin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s.toString()
                binding.tilPasswordAdmin.error = when {
                    pwd.isEmpty()                 -> "Por favor, completa este campo"
                    !pwdPattern.matcher(pwd).matches()
                        -> "Ingresa minimo 8 caracteres, 1 número y 1 especial"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 5) Confirmar pwd
        binding.etConfirmPasswordAdmin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val c = s.toString()
                val o = binding.etPasswordAdmin.text.toString()
                binding.tilConfirmPasswordAdmin.error = when {
                    c.isEmpty()    -> "Por favor, completa este campo"
                    c != o         -> "No coincide"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 6) Fecha no vacía
        binding.etFechaNacimientoAdmin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilFechaNacimientoAdmin.error =
                    if (s.isNullOrBlank()) "Por favor, completa este campo" else null
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 7) Domicilio no vacío
        binding.etDomicilioFiscalAdmin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilDomicilioFiscalAdmin.error =
                    if (s.isNullOrBlank()) "Por favor, completa este campo" else null
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })
    }

    private fun setupListeners() {
        binding.btnRegisterAdmin.setOnClickListener {
            // checar errores
            val tills = listOf(
                binding.tilFirstNameAdmin,
                binding.tilLastNamePatAdmin,
                binding.tilLastNameMatAdmin,
                binding.tilEmailAdmin,
                binding.tilPasswordAdmin,
                binding.tilConfirmPasswordAdmin,
                binding.tilFechaNacimientoAdmin,
                binding.tilDomicilioFiscalAdmin
            )
            if (tills.any { it.error != null }) return@setOnClickListener

            // checar foto
            if (photoBitmap == null) {
                Toast.makeText(this, "Toma una fotografía antes de registrar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fn = binding.etFirstNameAdmin.text.toString().trim()
            val lp = binding.etLastNamePatAdmin.text.toString().trim()
            val lm = binding.etLastNameMatAdmin.text.toString().trim()
            val email = binding.etEmailAdmin.text.toString().trim()
            val pwd   = binding.etPasswordAdmin.text.toString().trim()
            val fecha = binding.etFechaNacimientoAdmin.text.toString().trim()
            val dom   = binding.etDomicilioFiscalAdmin.text.toString().trim()

            viewModel.registerAdmin(fn, lp, lm, email, pwd, fecha, dom, photoBitmap!!)
        }
    }

    private fun observeViewModel() {
        viewModel.registroResult.observe(this) { res ->
            if (res.isSuccess) {
                Toast.makeText(this, "Administrador registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, res.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permisos (idéntico al cliente) -----------------------------------
    private fun arePermissionsGranted(): Boolean {
        val cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
        val loc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        return cam && loc
    }
    private fun requestRequiredPermissions() {
        val toReq = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) toReq.add(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) toReq.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (toReq.isNotEmpty()) ActivityCompat.requestPermissions(this, toReq.toTypedArray(), REQUEST_PERMISSIONS)
    }
    override fun onRequestPermissionsResult(req: Int, perms: Array<String>, res: IntArray) {
        super.onRequestPermissionsResult(req, perms, res)
        if (req==REQUEST_PERMISSIONS) {
            if (arePermissionsGranted() && launchAfterPerms) {
                launchAfterPerms = false
                dispatchTakePictureIntent()
            } else if (!arePermissionsGranted()) {
                AlertDialog.Builder(this)
                    .setTitle("Permisos requeridos")
                    .setMessage("La app necesita cámara y ubicación.")
                    .setPositiveButton("OK"){_,_-> requestRequiredPermissions()}
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun promptEnableLocation() {
        AlertDialog.Builder(this)
            .setTitle("Activar ubicación")
            .setMessage("Necesitamos GPS antes de la foto.")
            .setPositiveButton("Ir a ajustes"){_,_-> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))}
            .setCancelable(false)
            .show()
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        }
        if (intent.resolveActivity(packageManager)!=null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }
    override fun onActivityResult(req: Int, code: Int, data: Intent?) {
        super.onActivityResult(req, code, data)
        if (req==REQUEST_IMAGE_CAPTURE && code==Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as? Bitmap
            bmp?.let {
                photoBitmap = it
                binding.ivPhotoAdminPreview.apply {
                    setImageBitmap(it)
                    visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}
