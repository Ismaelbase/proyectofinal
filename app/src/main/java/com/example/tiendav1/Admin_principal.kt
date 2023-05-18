package com.example.tiendav1

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tiendav1.databinding.ActivityAdminPrincipalBinding

class Admin_principal : AppCompatActivity() {

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
                R.id.Admin_comprar,
                R.id.Admin_eventos,
                R.id.Admin_puntos,
                R.id.Admin_chats
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Admin_config->navController.navigate(R.id.Admin_config)
                R.id.Admin_comprar->navController.navigate(R.id.Admin_comprar)
                R.id.Admin_eventos->navController.navigate(R.id.Admin_eventos)
                R.id.Admin_puntos->navController.navigate(R.id.Admin_puntos)
                R.id.Admin_chats->navController.navigate(R.id.Admin_chats)
            }
            true
        }
    }
}