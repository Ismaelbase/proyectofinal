package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Evento(
    var id:String? = null,
    var nombre:String? = null,
    var fecha:String? = null,
    var precio:Double? = null,
    var aforo:Int = 0,
    var url_foto:String? = null,
    var apuntados:Int = 0,
    var activo:Boolean? = null,
    var estado_noti:Int? = null,
    var usuario_noti:String? = null
): Parcelable
