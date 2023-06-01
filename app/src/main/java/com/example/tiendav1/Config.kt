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
import com.bumptech.glide.Glide
import com.example.tiendav1.databinding.FragmentConfigBinding
import com.google.firebase.database.*
import com.google.firebase.database.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Config : Fragment() {
    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private lateinit var pn: Principal_normal
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
    private lateinit var imagen_moneda:ImageView
    private lateinit var switch_moneda:Switch

    var modo_noche: Boolean = false
    var modo_moneda: Boolean = false
    private var url_avatar: Uri? = null


    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        pn = activity as Principal_normal
        //AQUI NADA
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //EQUIVALENTE AL ONCREATE DE UNA ACTIVITY


        //Aqui van los binding de este fragmento:
        cambiar_nombre = binding.configEtCambiarnombre
        cambiar_mail = binding.configEtCambiarmail
        switch_noche = binding.configSwNoche
        cambiar_avatar = binding.configIvAvatar
        cambiar_contrasena = binding.configBCambiarcontrasena
        borrar_cuenta = binding.configBBorrarcuenta
        aplicar_cambios = binding.configBAplicarcambios
        logout = binding.configBLogout
        imagen_noche = binding.configIvNoche
        imagen_moneda = binding.configIvMoneda
        switch_moneda = binding.configSwMoneda


        val id_usuario = Utilidades.obtenerIDUsuario(pn.applicationContext)

        Utilidades.usuarios.child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_user = snapshot.getValue(User::class.java)!!
                    binding.configTvBienvenida.text = "Bienvenido ${pojo_user.usuario}"
                    cambiar_nombre.setText(pojo_user.usuario)
                    cambiar_mail.setText(pojo_user.correo)

                    Glide.with(pn.applicationContext).load(pojo_user.url_avatar)
                        .apply(Utilidades.opcionesGlideAvatar(pn.applicationContext))
                        .transition(Utilidades.transicion)
                        .into(cambiar_avatar)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        //MONEDA
        //Crear
        val app_id_moneda = getString(com.example.tiendav1.R.string.app_id)
        val sp_moneda = "${app_id_moneda}_moneda"
        var SP_MONEDA = pn.getSharedPreferences(sp_moneda, 0)

        //consultar el valor
        val moneda_elegida = SP_MONEDA.getBoolean("moneda", false)

        //Asignar valor
        switch_moneda.isChecked = moneda_elegida

        if(moneda_elegida){
            imagen_moneda.setImageResource(com.example.tiendav1.R.drawable.dolar_sc3)
        }else{
            imagen_moneda.setImageResource(com.example.tiendav1.R.drawable.euro_sc3)
        }

        //cambiar moneda
        switch_moneda.setOnCheckedChangeListener { compoundButton, isChecked ->
            modo_moneda = switch_moneda.isChecked
            with(SP_MONEDA.edit()) {
                putBoolean("moneda", modo_moneda)
                apply()
            }

            if(modo_moneda){
                imagen_moneda.setImageResource(com.example.tiendav1.R.drawable.dolar_sc3)
            }else{
                imagen_moneda.setImageResource(com.example.tiendav1.R.drawable.euro_sc3)
            }
        }


        //Cargamos las Shared Preferences
        val app_id = getString(com.example.tiendav1.R.string.app_id)
        val shared_theme = "${app_id}_tema_oscuro"
        var SP = pn.getSharedPreferences(shared_theme, 0)

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
            pn.recreate()
        }

        //Cambiar avatar

        cambiar_avatar.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        //Acceso a camara
        cambiar_avatar.setOnLongClickListener {
            val fichero_temporal = crearFicheroImagen()
            url_avatar = FileProvider.getUriForFile(
                pn.applicationContext,
                "com.example.tiendav1.fileprovider",
                fichero_temporal
            )
            getCamara.launch(url_avatar)
            true
        }


        //Aqui configuro el aplicar cambios

        var correcto = false
        aplicar_cambios.setOnClickListener {

            pn.runOnUiThread {
                correcto = validarUsuario(cambiar_nombre) && validarCorreo(cambiar_mail)
            }

            GlobalScope.launch(Dispatchers.IO) {
                if (Utilidades.existeUser(cambiar_nombre.text.toString().trim()) &&
                    pojo_user.usuario.toString().toString() != cambiar_nombre.text.toString().trim()
                ) {
                    pn.runOnUiThread {
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


                    pn.runOnUiThread {
                        Toast.makeText(
                            pn.application,
                            "Cambios realizados con éxito",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    //si no hago esto no me deja cambiar nada mas despues de cambiar el nombre
                    // porque dice que ya existe el usuario
                    pn.finish()

                }
            }
        }


        //Aqui se configura el boton de logout
        logout.setOnClickListener {
            Utilidades.establecerIDUsuario(pn.applicationContext, "")
            Utilidades.establecerTipoUsuario(pn.applicationContext, false)

            startActivity(Intent(pn.applicationContext, MainActivity::class.java))
        }
        //Para cambiar la contraseña
        cambiar_contrasena.setOnClickListener {
            startActivity(Intent(pn.applicationContext, Cambiar_contrasena::class.java))
        }

        //Para borrar la cuenta
        borrar_cuenta.setOnClickListener {
            startActivity(Intent(pn.applicationContext, Borrar_cuenta::class.java))
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
        var correcto: Boolean
        var valor = e.text.toString().trim()

        if (valor.length >= 3 &&
            valor.matches("[A-Z].+".toRegex())
        ) {
            correcto = true
        } else {
            pn.runOnUiThread {
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
            pn.runOnUiThread {
                e.error = "El email es obligatorio."
            }
            correcto = false


            //Devuelve true si el e-mail introducido tiene el formato correcto
            //Por lo que ponemos ! ya que queremos que salte cuando NO es correcto.
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()) {
            pn.runOnUiThread {
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
                pn.applicationContext,
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
            pn.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File? = File.createTempFile(nombreFichero!!, ".jpg", carpetaDir)

        return ficheroImagen!!
    }
}