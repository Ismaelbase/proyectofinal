package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reserva(
    var id:String? = null,
    var id_usuario:String? = null,
    var id_articulo:String? = null,
    var estado:String? = null,
    var nombre_usuario:String? = null,
    var nombre_articulo:String? = null,
    var url_articulo:String? = null,
    var fecha:String? = null
    ): Parcelable{
    companion object{
        val estado = listOf(0,1,2,3,4)
        val estado_texto = listOf("Realizado","Aceptado","Rechazado","Listo para recoger","Recogido")
    }
}