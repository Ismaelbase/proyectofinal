package com.example.tiendav1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentComprarBinding
import com.google.firebase.database.*

class Comprar : Fragment() {
    private var _binding: FragmentComprarBinding? = null

    private val binding get() = _binding!!
    private lateinit var pn:Principal_normal

    //VARIABLES
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val lista_articulos:MutableList<Articulo> by lazy {
        Utilidades.obtenerListaArticulos(referencia_bd)
    }
    var filtros_seleccionados = mutableListOf(
        true,
        true,
        true,
        true
    )
    private lateinit var adaptador:Adaptador_Comprar
    private lateinit var busqueda:SearchView
    private lateinit var cb_tecno:CheckBox
    private lateinit var cb_ropa:CheckBox
    private lateinit var cb_ocio:CheckBox
    private lateinit var cb_muebles:CheckBox
    private lateinit var recyclerView: RecyclerView

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentComprarBinding.inflate(inflater, container, false)
        pn = activity as Principal_normal

        //AQUI NADA
        return binding.root


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //EQUIVALENTE AL ONCREATE DE UNA ACTIVITY

        busqueda = binding.comprarSearchview
        cb_tecno = binding.comprarCbTecnologia
        cb_ropa = binding.comprarCbRopa
        cb_ocio = binding.comprarCbOcio
        cb_muebles = binding.comprarCbMuebles
        recyclerView = binding.comprarRv

        val lista_checkbox:List<CheckBox> = listOf(
            cb_tecno,
            cb_ropa,
            cb_ocio,
            cb_muebles
        )

        adaptador = Adaptador_Comprar(lista_articulos,filtros_seleccionados)

        referencia_bd.child("SecondCharm").child("Articulos")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_articulos.clear()
                    snapshot.children.forEach{
                        val pojo_articulo = it.getValue(Articulo::class.java)
                        if (pojo_articulo!!.cantidad != 0){
                            lista_articulos.add(pojo_articulo)
                        }
                    }
                    adaptador.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,"Error al acceder a los temas", Toast.LENGTH_SHORT).show()
                }

            })

        recyclerView.adapter = adaptador
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter?.notifyDataSetChanged()

        busqueda.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String?): Boolean {
                adaptador.filter.filter(texto)
                return false
            }

        })

        //CheckBoxs

        lista_checkbox.forEach {
            it.setOnClickListener {
                val cb = it as CheckBox
                val posicion = lista_checkbox.indexOf(cb)

                filtros_seleccionados[posicion] = cb.isChecked
                adaptador.filter.filter(busqueda.query)
            }
            adaptador.notifyDataSetChanged()
        }

    }

}