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

class Admin_comprar : Fragment() {
    private var _binding: FragmentComprarBinding? = null

    private val binding get() = _binding!!
    private lateinit var principal_admin:Admin_principal

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
        principal_admin = activity as Admin_principal

        //AQUI NADA
        return binding.root


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //EQUIVALENTE AL ONCREATE DE UNA ACTIVITY


    }

}