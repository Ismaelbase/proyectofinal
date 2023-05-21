package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reserva(
    var id:String? = null,
    var id_usuario:String? = null,
    var id_articulo:String? = null,
    var estado:Int? = null,
    ): Parcelable{
    companion object{
        val estado = listOf(0,1,2,3)
        val estado_texto = listOf("Reservado","Aceptado","Recogido","Cancelado")
    }
}