package com.example.tiendav1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendav1.databinding.FragmentAdminPedidosBinding
import com.google.firebase.database.*

//Revisar pedidos
class Admin_pedidos : Fragment() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val lista_pedidos:MutableList<Reserva> by lazy {
        Utilidades.obtenerListaPedidos()
    }

    var filtros_seleccionados = mutableListOf(
        true,
        true,
        true
    )

    private lateinit var busqueda: SearchView
    private lateinit var cb_rechazado:CheckBox
    private lateinit var cb_aceptado:CheckBox
    private lateinit var cb_pendiente:CheckBox
    private lateinit var recyclerView: RecyclerView

    private lateinit var adaptador:Adaptador_pedidos

    private var _binding: FragmentAdminPedidosBinding? = null
    private val binding get() = _binding!!
    private lateinit var principal_admin:Admin_principal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdminPedidosBinding.inflate(inflater, container, false)
        principal_admin = activity as Admin_principal

        //AQUI NADA
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Los bindings

        busqueda = binding.pedidosSearchview
        cb_rechazado = binding.pedidosCbRechazados
        cb_aceptado = binding.pedidosCbAceptados
        cb_pendiente = binding.pedidosCbPendientes
        recyclerView = binding.pedidosRecycler

        cb_rechazado.isChecked = true
        cb_aceptado.isChecked = true
        cb_pendiente.isChecked = true


        val lista_checkbox:List<CheckBox> = listOf(
            cb_rechazado,
            cb_aceptado,
            cb_pendiente
        )

        adaptador = Adaptador_pedidos(lista_pedidos,filtros_seleccionados)

        Utilidades.reservas.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_pedidos.clear()
                    snapshot.children.forEach{
                        val pojo_pedido = it.getValue(Reserva::class.java)
                        lista_pedidos.add(pojo_pedido!!)
                    }
                    lista_pedidos.sortByDescending{
                        it.estado == "Realizado"
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