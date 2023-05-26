package com.example.tiendav1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentAdminChatsBinding
import com.example.tiendav1.databinding.FragmentChatsBinding
import com.example.tiendav1.databinding.FragmentConfigBinding
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class Chats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var pn: Principal_normal

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }

    private lateinit var recycler: RecyclerView
    private lateinit var texto: EditText
    private lateinit var enviar: Button

    private lateinit var lista: MutableList<Mensaje>
    private lateinit var usuario_actual: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        pn = activity as Principal_normal
        //AQUI NADA
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lista = mutableListOf()

        recycler = binding.normalChatPublicoRecycler
        texto = binding.normalChatPublicoTexto
        enviar = binding.normalChatPublicoEnviar


        val id_usuario = Utilidades.obtenerIDUsuario(pn.applicationContext)

        Utilidades.usuarios.child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuario_actual = snapshot.getValue(User::class.java)!!
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        //Logica de enviar mensaje:
        enviar.setOnClickListener {
            val txt = texto.text.toString().trim()

            if (txt != "") { //Si el mensaje no es vacío
                //Cogemos la fecha de hoy.
                val ahora = Calendar.getInstance()
                val formateador = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
                val fecha_hora = formateador.format(ahora.time)

                val id_mensaje = Utilidades.chat_foro.push().key

                val mensaje_nuevo = Mensaje(id_mensaje, usuario_actual.id, txt, fecha_hora)

                Utilidades.chat_foro.child(id_mensaje!!).setValue(mensaje_nuevo)

                //Una vez subido el mensaje a firebase, podemos borrar la caja de texto.
                texto.setText("")

            } else {
                Toast.makeText(pn.applicationContext, "Mensaje vacío", Toast.LENGTH_SHORT).show()
            }
        }

        //Cargar mensajes:
        Utilidades.chat_foro.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot,previousChildName: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                    pojo_mensaje!!.id_receptor = usuario_actual.id

                    if(pojo_mensaje.id_emisor == pojo_mensaje.id_receptor){
                        pojo_mensaje.imagen_emisor = usuario_actual.url_avatar
                    }else{
                        var semaforo = CountDownLatch(1)

                        Utilidades.usuarios.child(pojo_mensaje.id_emisor!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    val pojo_user = snapshot.getValue(User::class.java)
                                    pojo_mensaje.imagen_emisor = pojo_user!!.url_avatar

                                    semaforo.countDown()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        semaforo.await()
                    }

                    //Esto modifica la vista, hay que ejecutarlo en el hilo principal:
                    pn.runOnUiThread {
                        lista.add(pojo_mensaje)
                        lista.sortBy { it.id }
                        recycler.scrollToPosition(lista.size - 1)
                        recycler.adapter!!.notifyDataSetChanged()
                    }
                }
            }
            //Me salta excepcion si no dejo todos los miembros nombrados:
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        recycler.adapter = Adaptador_Chat_Articulo(lista)
        recycler.layoutManager = LinearLayoutManager(pn.applicationContext)
        recycler.setHasFixedSize(true)



    }

}