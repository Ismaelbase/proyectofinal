package com.example.tiendav1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mensaje(var id:String?=null,
                   var id_emisor:String?=null,
                   var texto:String?=null,
                   var fecha_hora:String?=null,
                   var id_receptor:String?=null,
                   var imagen_emisor:String?=null,
                   var id_articulo:String?=null): Parcelable