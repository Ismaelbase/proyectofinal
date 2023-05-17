package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Articulo(
    var id:String? = null,
    var nombre:String? = null,
    var precio:Double? = null,
    var descripcion:String? = null,
    var categoria:String? = null,
    var url_foto:String? = null,
    var fecha:String? = null,
    var cantidad:Int? = null,
    var puntos:Int? = null,

): Parcelable{
    companion object{
        val categoria = listOf("Tecnología", "Ropa", "Ocio", "Muebles")
    }
}
