package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.media.Image
import android.text.TextUtils.indexOf
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

class Adaptador_Chat_Articulo(private val lista_mensajes: List<Mensaje>) :
    RecyclerView.Adapter<Adaptador_Chat_Articulo.UserViewHolder>(){

    private lateinit var contexto: Context
    private var lista_filtrada = lista_mensajes

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        //Todo por aqui
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

        if (item_actual.id_emisor == item_actual.id_receptor) {
            //Este mensaje es mio (yo), debe ir a la derecha.
            holder.mensaje_propio.text = item_actual.texto
            holder.mensaje_otro.text = ""
            holder.fecha_propio.text = item_actual.fecha_hora
            holder.fecha_otro.text = ""

            holder.avatar_propio.visibility = View.VISIBLE
            holder.avatar_otro.visibility = View.INVISIBLE

            Glide.with(contexto)
                .load(item_actual.imagen_emisor)
                .apply(Utilidades.opcionesGlideAvatar(contexto))
                .transition(Utilidades.transicion)
                .into(holder.avatar_propio)

        } else {
            //Este mensaje NO es mio (otro), debe ir a la izquierda.
            holder.mensaje_propio.text = ""
            holder.mensaje_otro.text = item_actual.texto
            holder.fecha_propio.text = ""
            holder.fecha_otro.text = item_actual.fecha_hora

            holder.avatar_propio.visibility = View.INVISIBLE
            holder.avatar_otro.visibility = View.VISIBLE

            Glide.with(contexto)
                .load(item_actual.imagen_emisor)
                .apply(Utilidades.opcionesGlideAvatar(contexto))
                .transition(Utilidades.transicion)
                .into(holder.avatar_otro)
        }
    }

    override fun getItemCount(): Int {
        return lista_filtrada!!.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mensaje_propio: TextView = itemView.findViewById(R.id.mensaje_texto_propio)
        val mensaje_otro: TextView = itemView.findViewById(R.id.mensaje_texto_otro)
        val avatar_propio: ImageView = itemView.findViewById(R.id.mensaje_avatar_propio)
        val avatar_otro: ImageView = itemView.findViewById(R.id.mensaje_avatar_otro)
        val fecha_propio: TextView = itemView.findViewById(R.id.mensaje_fecha_propio)
        val fecha_otro: TextView = itemView.findViewById(R.id.mensaje_fecha_otro)

    }

}