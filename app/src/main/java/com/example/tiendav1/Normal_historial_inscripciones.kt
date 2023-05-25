package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Normal_historial_inscripciones : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
//        adaptador.notifyDataSetChanged()
    }
    var filtros_seleccionados = mutableListOf(
        true,
        true,
        true
    )

    val busqueda: SearchView by lazy {
        findViewById(R.id.historial_inscripciones_searchview)
    }
    val cb_pendientes: CheckBox by lazy {
        findViewById(R.id.historial_inscripciones_cb_pendientes)
    }
    val cb_rechazados: CheckBox by lazy {
        findViewById(R.id.historial_inscripciones_cb_rechazadas)
    }
    val cb_aceptados: CheckBox by lazy {
        findViewById(R.id.historial_inscripciones_cb_aceptadas)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.historial_inscripciones_boton_volver)
    }
    val recycler: RecyclerView by lazy {
        findViewById(R.id.historial_inscripciones_recycler)
    }
    val id_usuario:String by lazy {
        Utilidades.obtenerIDUsuario(applicationContext)
    }
    val lista_inscripciones:MutableList<Inscripcion> by lazy {
        Utilidades.obtenerListaInscripciones()
    }

    private lateinit var adaptador:Adaptador_historial_inscripciones


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_historial_inscripciones)

        supportActionBar!!.hide()

        adaptador = Adaptador_historial_inscripciones(lista_inscripciones,filtros_seleccionados)

        cb_aceptados.isChecked = true
        cb_pendientes.isChecked = true
        cb_rechazados.isChecked = true

        val lista_checkbox:List<CheckBox> = listOf(
            cb_aceptados,
            cb_pendientes,
            cb_rechazados
        )

        Utilidades.inscripcion.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista_inscripciones.clear()
                snapshot.children.forEach{
                    val pojo_inscripcion = it.getValue(Inscripcion::class.java)
                    if (pojo_inscripcion!!.id_usuario == id_usuario){
                        lista_inscripciones.add(pojo_inscripcion!!)
                    }
                }
                adaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"Error al acceder a los temas", Toast.LENGTH_SHORT).show()
            }
        })

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

        lista_checkbox.forEach {
            it.setOnClickListener {
                val cb = it as CheckBox
                val posicion = lista_checkbox.indexOf(cb)

                filtros_seleccionados[posicion] = cb.isChecked
                adaptador.filter.filter(busqueda.query)
                adaptador.notifyDataSetChanged()
            }

        }

        boton_volver.setOnClickListener {
            finish()
        }


    }
}