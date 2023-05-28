package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.media.Image
import android.text.TextUtils.indexOf
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

class Adaptador_moderar_usuarios(private val lista_usuarios: MutableList<User>) :
    RecyclerView.Adapter<Adaptador_moderar_usuarios.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_usuarios

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

        Glide.with(contexto)
            .load(item_actual.url_avatar)
            .apply(Utilidades.opcionesGlideAvatar(contexto))
            .transition(Utilidades.transicion)
            .into(holder.imagen)

        holder.nombre.text = item_actual.usuario

        if (item_actual.admin!!){
            holder.fondo.setImageDrawable(contexto.resources.getDrawable(R.drawable.usuario_admin))
        }else if (!item_actual.alta!!){
            holder.fondo.setImageDrawable(contexto.resources.getDrawable(R.drawable.usuario_baneado))
        }

        holder.layout.setOnClickListener {
            //Todo te lleva a moderar usuario especifico
        }

    }

    override fun getItemCount(): Int {
        return lista_filtrada!!.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString().lowercase()

                //Filtro por nombre
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_usuarios
                } else {
                    lista_filtrada = (lista_usuarios.filter {
                        it.usuario.toString().lowercase().contains(busqueda)
                    }) as MutableList<User>
                }


                //todo Filtro de baneados?
//                lista_filtrada = lista_filtrada.filter {
//                    var posicion = Articulo.categorias.indexOf(it.categoria)
//                    filtros_check.get(posicion)
//                } as MutableList<Articulo>

                val resultados_filtro = FilterResults()
                resultados_filtro.values = lista_filtrada

                return resultados_filtro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                lista_filtrada = p1?.values as MutableList<User>
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView = itemView.findViewById(R.id.item_usuario_imagen)
        val nombre: TextView = itemView.findViewById(R.id.item_usuario_nombre)
        val layout:ConstraintLayout = itemView.findViewById(R.id.item_usuario_layout)
        val fondo:ImageView = itemView.findViewById(R.id.item_usuario_fondo_color)

    }

}