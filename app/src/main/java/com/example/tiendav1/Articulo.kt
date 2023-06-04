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
    var stock:Int? = null,
    var disponible:Boolean? = null,
    var estado_noti:Int? = 0,
    var usuario_noti:String? = null

): Parcelable{
    companion object{
        val categorias = listOf("Tecnología", "Ropa", "Deporte", "Muebles")
    }
}
