package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Admin_moderar_usuarios : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        adaptador.notifyDataSetChanged()
        busqueda.setQuery("",false)
    }
    val id_usuario:String by lazy {
        Utilidades.obtenerIDUsuario(applicationContext)
    }
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val lista_usuarios:MutableList<User> by lazy {
        Utilidades.obtenerListaCompletaUsersMenosActual(id_usuario)
    }
    val busqueda:SearchView by lazy {
        findViewById(R.id.moderar_usuarios_searchview)
    }
    val recycler:RecyclerView by lazy {
        findViewById(R.id.moderar_usuarios_recycler)
    }
    val boton_volver:ImageView by lazy {
        findViewById(R.id.moderar_usuarios_boton_volver)
    }
    val adaptador:Adaptador_moderar_usuarios by lazy {
        Adaptador_moderar_usuarios(lista_usuarios)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_moderar_usuarios)

        supportActionBar?.hide()

        recycler.adapter = adaptador
        recycler.layoutManager = GridLayoutManager(applicationContext,3)
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