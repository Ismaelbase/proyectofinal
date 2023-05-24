package com.example.tiendav1

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentAdminInventarioBinding
import com.google.firebase.database.*

class Admin_inventario : Fragment() {

    override fun onResume() {
        super.onResume()
        adaptador.notifyDataSetChanged()
    }

    private var _binding: FragmentAdminInventarioBinding? = null

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
    private lateinit var adaptador:Adaptador_Comprar_Admin
    private lateinit var busqueda:SearchView
    private lateinit var cb_tecno:CheckBox
    private lateinit var cb_ropa:CheckBox
    private lateinit var cb_ocio:CheckBox
    private lateinit var cb_muebles:CheckBox
    private lateinit var recyclerView: RecyclerView
    private lateinit var anadir_articulo:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAdminInventarioBinding.inflate(inflater, container, false)
        principal_admin = activity as Admin_principal

        //AQUI NADA
        return binding.root


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //EQUIVALENTE AL ONCREATE DE UNA ACTIVITY

        busqueda = binding.adminInventarioSearchview
        cb_tecno = binding.adminInventarioTecno
        cb_ropa = binding.adminInventarioRopa
        cb_ocio = binding.adminInventarioOcio
        cb_muebles = binding.adminInventarioMuebles
        recyclerView = binding.adminInventarioRv
        anadir_articulo = binding.adminInventarioAnadir

        cb_tecno.isChecked = true
        cb_ropa.isChecked = true
        cb_ocio.isChecked = true
        cb_muebles.isChecked = true

        val lista_checkbox:List<CheckBox> = listOf(
            cb_tecno,
            cb_ropa,
            cb_ocio,
            cb_muebles
        )

        adaptador = Adaptador_Comprar_Admin(lista_articulos,filtros_seleccionados)

        referencia_bd.child("SecondCharm").child("Articulos")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_articulos.clear()
                    snapshot.children.forEach{
                        val pojo_articulo = it.getValue(Articulo::class.java)
                        lista_articulos.add(pojo_articulo!!)
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

        //Boton añadir articulo
        anadir_articulo.setOnClickListener {
            startActivity(Intent(principal_admin.applicationContext, Admin_anadir_articulo::class.java))
        }

        //CheckBoxs

        lista_checkbox.forEach {
            it.setOnClickListener {
                val cb = it as CheckBox
                val posicion = lista_checkbox.indexOf(cb)

                filtros_seleccionados[posicion] = cb.isChecked
                adaptador.filter.filter(busqueda.query)
            }

        }

    }

}