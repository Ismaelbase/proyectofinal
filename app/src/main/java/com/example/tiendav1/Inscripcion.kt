package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Inscripcion(
    var id:String? = null,
    var id_usuario:String? = null,
    var id_evento:String? = null,
    var estado:String? = null,
    var nombre_usuario:String? = null,
    var nombre_evento:String? = null,
    var url_evento:String? = null,
    var url_usuario:String? = null,
    var fecha:String? = null,
    var precio:Double? = null,
    var estado_noti:Int? = null
    ): Parcelable

{
    companion object{
        val estado_inscripcion = listOf(0,1,2)
        val estado_inscripcion_txt = listOf("Pendiente","Aceptado","Rechazado")
    }
}