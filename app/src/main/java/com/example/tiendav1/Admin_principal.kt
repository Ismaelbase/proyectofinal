package com.example.tiendav1

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tiendav1.databinding.ActivityAdminPrincipalBinding

class Admin_principal : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        if (Utilidades.admin_editar){
            navController.navigate(R.id.Admin_inventario)
            navView.selectedItemId = R.id.Admin_inventario
            Utilidades.admin_editar = false
        }

        if (Utilidades.admin_editar_evento){
            navController.navigate(R.id.Admin_eventos)
            navView.selectedItemId = R.id.Admin_eventos
            Utilidades.admin_editar_evento = false
        }
        if (Utilidades.admin_anadir){
            navController.navigate(R.id.Admin_inventario)
            navView.selectedItemId = R.id.Admin_inventario
            Utilidades.admin_anadir = false
        }
        if (Utilidades.admin_gestion_pedido){
            navController.navigate(R.id.Admin_pedidos)
            navView.selectedItemId = R.id.Admin_pedidos
            Utilidades.admin_gestion_pedido = false
        }

        if(Utilidades.obtenerIDUsuario(applicationContext) == ""){
            startActivity(Intent(applicationContext,MainActivity::class.java))
        }

        if(Utilidades.obtenerIDUsuario(applicationContext) != ""){

            if(!Utilidades.obtenerTipoUsuario(applicationContext)){
                startActivity(Intent(applicationContext,MainActivity::class.java))
            }else{
            }
        }

    }

    private lateinit var binding: ActivityAdminPrincipalBinding
    private lateinit var navController: NavController
    private lateinit var navView:BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()

        binding = ActivityAdminPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navViewAdmin

        navController = findNavController(R.id.nav_host_fragment_activity_admin_principal)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.Admin_config,
                R.id.Admin_inventario,
                R.id.Admin_pedidos,
                R.id.Admin_eventos,
                R.id.Admin_chats
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Admin_config->navController.navigate(R.id.Admin_config)
                R.id.Admin_inventario->navController.navigate(R.id.Admin_inventario)
                R.id.Admin_pedidos->navController.navigate(R.id.Admin_pedidos)
                R.id.Admin_eventos->navController.navigate(R.id.Admin_eventos)
                R.id.Admin_chats->navController.navigate(R.id.Admin_chats)
            }
            true
        }
    }

}