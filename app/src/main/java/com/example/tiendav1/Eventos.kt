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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentComprarBinding
import com.example.tiendav1.databinding.FragmentEventosBinding
import com.google.firebase.database.*
import java.time.LocalDate

class Eventos : Fragment() {

    override fun onResume() {
        super.onResume()
        adaptador.notifyDataSetChanged()
    }

    private lateinit var busqueda: SearchView
    private lateinit var recycler: RecyclerView
    private lateinit var historial: ImageView
    private lateinit var adaptador:Adaptador_Eventos

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val lista_eventos:MutableList<Evento> by lazy {
        Utilidades.obtenerListaEventos()
    }

    private var _binding: FragmentEventosBinding? = null

    private val binding get() = _binding!!
    private lateinit var pn:Principal_normal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        pn = activity as Principal_normal

        //AQUI NADA
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        busqueda = binding.eventosSearchview
        recycler = binding.eventosRecycler
        historial = binding.eventosHistorial

        adaptador = Adaptador_Eventos(lista_eventos)

        var fecha_actual = LocalDate.now()

        Utilidades.eventos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista_eventos.clear()
                snapshot.children.forEach{
                    val pojo_evento = it.getValue(Evento::class.java)
                    if (pojo_evento!!.apuntados!!.toInt() < pojo_evento.aforo!!.toInt() &&
                        pojo_evento.activo!!){
                        lista_eventos.add(pojo_evento)
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

        historial.setOnClickListener {
            startActivity(Intent(pn.applicationContext, Normal_historial_inscripciones::class.java))
        }

    }

}