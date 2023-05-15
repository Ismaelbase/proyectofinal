package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id:String? = null,
    var usuario:String? = null,
    var contraseña:String? = null,
    var correo:String? = null,
    var puntos:Int? = null,
    var url_avatar:String? = null,
    var fecha:String? = null,
    var alta:Boolean? = null,
    var conectado:Boolean? = null,
    var admin:Boolean? = null
): Parcelable
