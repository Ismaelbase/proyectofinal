package com.example.tiendav1

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentAdminEventosBinding
import com.google.firebase.database.*

//Revisar eventos
class Admin_eventos : Fragment() {

    override fun onResume() {
        super.onResume()
        adaptador.notifyDataSetChanged()
        busqueda.setQuery("", false)
    }

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val lista_eventos:MutableList<Evento> by lazy {
        Utilidades.obtenerListaEventos()
    }

    private lateinit var busqueda: SearchView
    private lateinit var crear_evento:ImageView
    private lateinit var recycler:RecyclerView

    private lateinit var adaptador:Adaptador_eventos_admin


    private var _binding: FragmentAdminEventosBinding? = null

    private val binding get() = _binding!!
    private lateinit var principal_admin:Admin_principal
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminEventosBinding.inflate(inflater, container, false)
        principal_admin = activity as Admin_principal

        //AQUI NADA
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //BINDINGS
        busqueda = binding.adminEventosSearchview
        crear_evento = binding.adminEventosNuevoEvento
        recycler = binding.adminEventosRv

        //Crear evento
        crear_evento.setOnClickListener {
            startActivity(Intent(principal_admin.applicationContext, Admin_crear_evento::class.java))
        }

        adaptador = Adaptador_eventos_admin(lista_eventos)

        Utilidades.eventos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista_eventos.clear()
                snapshot.children.forEach{
                    val pojo_eventos = it.getValue(Evento::class.java)
                    if(pojo_eventos!!.id != null){
                        lista_eventos.add(pojo_eventos!!)
                    }

                }
                adaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error al acceder a los temas", Toast.LENGTH_SHORT).show()
            }
        })

        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.setHasFixedSize(true)
        recycler.adapter?.notifyDataSetChanged()


        busqueda.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String?): Boolean {
                adaptador.filter.filter(texto)
                return false
            }

        })

    }

}