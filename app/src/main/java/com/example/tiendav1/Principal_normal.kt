package com.example.tiendav1

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tiendav1.databinding.ActivityPrincipalNormalBinding

class Principal_normal : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalNormalBinding

    private lateinit var navController:NavController
    private lateinit var navView:BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()

        binding = ActivityPrincipalNormalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_principal_normal)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(

            setOf(
                R.id.Config,
                R.id.Comprar,
                R.id.Eventos,
                R.id.Puntos,
                R.id.Chats
            )

        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Config-> navController.navigate(R.id.Config)
                R.id.Comprar-> navController.navigate(R.id.Comprar)
                R.id.Eventos-> navController.navigate(R.id.Eventos)
                R.id.Puntos-> navController.navigate(R.id.Puntos)
                R.id.Chats-> navController.navigate(R.id.Chats)
            }
            true
        }
    }
}