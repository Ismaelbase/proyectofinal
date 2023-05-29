package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class Admin_moderar_usuario_especifico : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_moderar_usuario_especifico)

        val adaptador:Adaptador_moderar_usuarios = Adaptador_moderar_usuarios(Utilidades.obtenerListaCompletaUsers())

        supportActionBar!!.hide()
        val usuario = intent.getParcelableExtra<User>("USUARIO")
        //Baneamos al usuario y salimos de la actividad
        if(usuario!!.admin!!){
            Toast.makeText(applicationContext, "No puedes banear a un admin", Toast.LENGTH_SHORT).show()
        }else if (usuario!!.alta!!){
            Utilidades.usuarios.child(usuario.id.toString()).child("alta").setValue(false)
            Toast.makeText(applicationContext, "Has baneado a ${usuario.usuario}", Toast.LENGTH_SHORT).show()
        }else{
            Utilidades.usuarios.child(usuario.id.toString()).child("alta").setValue(true)
            Toast.makeText(applicationContext, "Has desbaneado a ${usuario.usuario}", Toast.LENGTH_SHORT).show()
        }

        adaptador.notifyDataSetChanged()
        finish()
    }
}