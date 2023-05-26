package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.text.TextUtils.indexOf
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

class Adaptador_Eventos(private val lista_eventos: MutableList<Evento>) :
    RecyclerView.Adapter<Adaptador_Eventos.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_eventos

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

        Glide.with(contexto)
            .load(item_actual.url_foto)
            .apply(Utilidades.opcionesGlide(contexto))
            .transition(Utilidades.transicion)
            .into(holder.imagen)

        holder.nombre.text = item_actual.nombre.toString()

        holder.evento.setOnClickListener {
            contexto.startActivity(Intent(contexto, Normal_inscripcion_evento::class.java)
                .putExtra("ID", item_actual.id))
        }
        holder.chat.setOnClickListener {
            contexto.startActivity(Intent(contexto, Chat_eventos::class.java)
                .putExtra("ID", item_actual.id))
        }

        if (item_actual.aforo == item_actual.apuntados) {
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.error))
        }else if(item_actual.apuntados!! == 0){
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.cero_monigotes))
        } else if (item_actual.aforo!!.toDouble() / item_actual.apuntados!!.toDouble() > 4) {
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.un_monigote))
        } else if (item_actual.aforo!!.toDouble() / item_actual.apuntados!!.toDouble() > 3) {
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.dos_monigotes))
        } else if(item_actual.aforo!!.toDouble() / item_actual.apuntados!!.toDouble() > 1.5) {
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.dos_monigotes))
        }else if(item_actual.aforo!!.toDouble() / item_actual.apuntados!!.toDouble() > 1) {
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.tres_monigotes))
        }else{
            holder.capacidad.setImageDrawable(getDrawable(contexto, R.drawable.error))
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
                    lista_filtrada = lista_eventos
                } else {
                    lista_filtrada = (lista_eventos.filter {
                        it.nombre.toString().lowercase().contains(busqueda)
                    }) as MutableList<Evento>
                }

                //Filtro precio maximo
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_eventos
                } else if (busqueda.toIntOrNull() != null || busqueda.toDoubleOrNull() != null) {
                    lista_filtrada = (lista_eventos.filter {
                        it.precio!!.toDouble() <= busqueda.toDouble()
                    }) as MutableList<Evento>
                }

                val resultados_filtro = FilterResults()
                resultados_filtro.values = lista_filtrada

                return resultados_filtro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                lista_filtrada = p1?.values as MutableList<Evento>
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView = itemView.findViewById(R.id.item_evento_imagen)
        val nombre: TextView = itemView.findViewById(R.id.item_evento_nombre)
        val evento: RelativeLayout = itemView.findViewById(R.id.item_evento_evento)
        val capacidad: ImageView = itemView.findViewById(R.id.item_evento_capacidad)
        val chat:ImageView = itemView.findViewById(R.id.item_evento_chat)

    }


}
