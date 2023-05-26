package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Evento(
    var id:String? = null,
    var nombre:String? = null,
    var fecha:String? = null,
    var precio:Double? = null,
    var aforo:Int? = null,
    var url_foto:String? = null,
    var apuntados:Int? = null,
    var activo:Boolean? = null
): Parcelable
