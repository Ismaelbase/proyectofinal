package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Switch
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Normal_historial_compras : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        adaptador.notifyDataSetChanged()
    }
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val busqueda: SearchView by lazy {
        findViewById(R.id.historial_searchview)
    }
    val switch_pendientes: Switch by lazy {
        findViewById(R.id.historial_switch_pendientes)
    }
    val recycler:RecyclerView by lazy {
        findViewById(R.id.historial_rv)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.admin_gestion_inscripciones_boton_volver)
    }


    private lateinit var lista_reserva: MutableList<Reserva>
    private lateinit var adaptador:Adaptador_historial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_historial_compras)

        supportActionBar?.hide()

        val id_usuario = Utilidades.obtenerIDUsuario(applicationContext)

        //Cogemos las reservas del usuario con su id

        lista_reserva = Utilidades.obtenerListaHistorial(id_usuario)

        Utilidades.reservas.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_reserva.clear()
                    snapshot.children.forEach {
                        val pojo_reserva = it.getValue(Reserva::class.java)
                        if (pojo_reserva != null) {
                            if (pojo_reserva.id_usuario == id_usuario) {
                                lista_reserva.add(pojo_reserva)
                            }
                        }
                    }
                    adaptador.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext,"Error al acceder a los temas", Toast.LENGTH_SHORT).show()
                }

            })

        var lista_switch = List<Boolean>(1){false}.toMutableList()

        switch_pendientes.setOnCheckedChangeListener { compoundButton, b ->
            //actualizas la lista con el switch
            lista_switch[0] = b
            adaptador.filter.filter(busqueda.query)

        }

        adaptador = Adaptador_historial(lista_reserva,lista_switch)
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationContext)
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

        boton_volver.setOnClickListener {
            finish()
        }



    }
}