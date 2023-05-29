package com.example.tiendav1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.tiendav1.databinding.FragmentAdminConfigBinding
import com.example.tiendav1.databinding.FragmentConfigBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Admin_config : Fragment() {
    private var _binding: FragmentAdminConfigBinding? = null
    private val binding get() = _binding!!
    private lateinit var ap: Admin_principal
    private lateinit var pojo_user: User

    private lateinit var cambiar_nombre: EditText
    private lateinit var cambiar_mail: EditText
    private lateinit var switch_noche: Switch
    private lateinit var cambiar_avatar: ImageView
    private lateinit var cambiar_contrasena: Button
    private lateinit var borrar_cuenta: Button
    private lateinit var aplicar_cambios: Button
    private lateinit var logout: Button
    private lateinit var imagen_noche: ImageView
    private lateinit var bienvenida: TextView

    var modo_noche: Boolean = false
    private var url_avatar: Uri? = null


    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminConfigBinding.inflate(inflater, container, false)
        ap = activity as Admin_principal
        //AQUI NADA
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //EQUIVALENTE AL ONCREATE DE UNA ACTIVITY


        //Aqui van los binding de este fragmento:
        cambiar_nombre = binding.adminConfigEtCambiarnombre
        cambiar_mail = binding.adminConfigEtCambiarmail
        switch_noche = binding.adminConfigSwNoche
        cambiar_avatar = binding.adminConfigIvAvatar
        cambiar_contrasena = binding.adminConfigBCambiarcontrasena
        borrar_cuenta = binding.adminConfigBBorrarcuenta
        aplicar_cambios = binding.adminConfigBAplicarcambios
        logout = binding.adminConfigBLogout
        imagen_noche = binding.adminConfigIvNoche
        bienvenida = binding.adminConfigTvBienvenida


        val id_usuario = Utilidades.obtenerIDUsuario(ap.applicationContext)

        Utilidades.usuarios.child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_user = snapshot.getValue(User::class.java)!!
                    binding.adminConfigTvBienvenida.text = "Bienvenido Admin ${pojo_user.usuario}"
                    cambiar_nombre.setText(pojo_user.usuario)
                    cambiar_mail.setText(pojo_user.correo)

                    Glide.with(ap.applicationContext).load(pojo_user.url_avatar)
                        .apply(Utilidades.opcionesGlideAvatar(ap.applicationContext))
                        .transition(Utilidades.transicion)
                        .into(cambiar_avatar)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        //Cargamos las Shared Preferences
        val app_id = getString(com.example.tiendav1.R.string.app_id)
        val shared_theme = "${app_id}_tema_oscuro"
        var SP = ap.getSharedPreferences(shared_theme, 0)

        modo_noche = SP.getBoolean("modo", false)
        switch_noche.isChecked = modo_noche

        //Cambiar tema
        switch_noche.setOnCheckedChangeListener { compoundButton, isChecked ->
            modo_noche = switch_noche.isChecked
            with(SP.edit()) {
                putBoolean("modo", modo_noche)
                apply()
            }
            Utilidades.cambiarTema(modo_noche)
            ap.recreate()
        }

        //Cambiar avatar

        cambiar_avatar.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        //Acceso a camara
        cambiar_avatar.setOnLongClickListener {
            val fichero_temporal = crearFicheroImagen()
            url_avatar = FileProvider.getUriForFile(
                ap.applicationContext,
                "com.example.tiendav1.fileprovider",
                fichero_temporal
            )
            getCamara.launch(url_avatar)
            true
        }


        //Aqui configuro el aplicar cambios
        var correcto = false

        aplicar_cambios.setOnClickListener {

            ap.runOnUiThread {
                correcto = validarUsuario(cambiar_nombre) && validarCorreo(cambiar_mail)
            }

            GlobalScope.launch(Dispatchers.IO) {
                if (Utilidades.existeUser(cambiar_nombre.text.toString().trim()) &&
                    pojo_user.usuario.toString().toString() != cambiar_nombre.text.toString().trim()
                ) {
                    ap.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Ya existe un usuario con ese nombre",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }else if(correcto){

                    val nuevo_nombre = cambiar_nombre.text.toString().trim()
                    val nuevo_mail = cambiar_mail.text.toString().trim()


                    val url_firebase: String?

                    if (url_avatar == null) {
                        url_firebase = pojo_user.url_avatar
                    } else {

                        url_firebase = Utilidades.guardarImagen(
                            referencia_almacenamiento,
                            pojo_user.id.toString(),
                            url_avatar!!
                        )
                    }

                    Utilidades.escribirUser(
                        referencia_bd,
                        pojo_user.id.toString(),
                        nuevo_nombre,
                        pojo_user.contraseña.toString(),
                        nuevo_mail,
                        pojo_user.puntos,
                        url_firebase.toString(),
                        pojo_user.fecha.toString(),
                        pojo_user.alta!!,
                        pojo_user.conectado!!,
                        pojo_user.admin!!
                    )


                    ap.runOnUiThread {
                        Toast.makeText(
                            ap.application,
                            "Cambios realizados con éxito",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    //si no hago esto no me deja cambiar nada mas despues de cambiar el nombre
                    // porque dice que ya existe el usuario
                    ap.finish()

                }
            }
        }


        //Aqui se configura el boton de logout
        logout.setOnClickListener {
            Utilidades.establecerIDUsuario(ap.applicationContext, "")
            Utilidades.establecerTipoUsuario(ap.applicationContext, false)

            startActivity(Intent(ap.applicationContext, MainActivity::class.java))
        }

        //Para cambiar la contraseña
        cambiar_contrasena.setOnClickListener {
            startActivity(Intent(ap.applicationContext, Cambiar_contrasena::class.java))
        }

        //Para borrar la cuenta
        borrar_cuenta.setOnClickListener {
            startActivity(Intent(ap.applicationContext, Borrar_cuenta::class.java))
        }


    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_avatar = uri
            cambiar_avatar.setImageURI(url_avatar)
        }
    }

    fun validarUsuario(e: EditText): Boolean {
        var correcto: Boolean = false
        var valor = e.text.toString().trim()

        if (valor.length >= 3 && valor.matches("[A-Z].+".toRegex())) {
            correcto = true
        } else {
            ap.runOnUiThread {
                e.error = "Formato de nombre incorrecto."
            }
            correcto = false

        }

        return correcto
    }

    fun validarCorreo(e: EditText): Boolean {
        var correcto: Boolean
        var valor = e.text.toString().trim()

        if (valor.isEmpty()) {
            ap.runOnUiThread {
                e.error = "El email es obligatorio."
            }
            correcto = false


            //Devuelve true si el e-mail introducido tiene el formato correcto
            //Por lo que ponemos ! ya que queremos que salte cuando NO es correcto.
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()) {
            ap.runOnUiThread {
                e.error = "El email no tiene un formato correcto."
            }
            correcto = false

        }else{
            correcto = true
        }

        return correcto
    }

    val getCamara = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            cambiar_avatar.setImageURI(url_avatar)
        } else {
            Toast.makeText(
                ap.applicationContext,
                "No has hecho ninguna foto",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun crearFicheroImagen(): File {
        val cal: Calendar? = Calendar.getInstance()
        val timeStamp: String? = SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
        val nombreFichero: String? = "JPGE_" + timeStamp + "_"
        val carpetaDir: File? =
            ap.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File? = File.createTempFile(nombreFichero!!, ".jpg", carpetaDir)

        return ficheroImagen!!
    }
}