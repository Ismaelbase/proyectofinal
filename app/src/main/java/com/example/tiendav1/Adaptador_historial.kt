package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Adaptador_historial(private val lista_historial: MutableList<Reserva>, var switch: List<Boolean>) :
    RecyclerView.Adapter<Adaptador_historial.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_historial

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

        Glide.with(contexto)
            .load(item_actual.url_articulo)
            .apply(Utilidades.opcionesGlide(contexto))
            .transition(Utilidades.transicion)
            .into(holder.imagen)

        holder.nombre.text = item_actual.nombre_articulo
        holder.fecha.text = item_actual.fecha
        holder.precio.text = item_actual.precio.toString()+"€"

        holder.itemView.setOnClickListener {
            contexto.startActivity(Intent(contexto, Normal_detalles_reserva::class.java).putExtra("ID", item_actual.id))
        }

        if(item_actual.estado == "Pendiente"){
            holder.color_estado.setImageDrawable(contexto.getDrawable(R.drawable.pendiente))
            holder.texto_estado.text = "Pendiente"
        }else if(item_actual.estado == "Rechazado"){
            holder.color_estado.setImageDrawable(contexto.getDrawable(R.drawable.rechazar))
            holder.texto_estado.text = "Rechazado"
        }else if(item_actual.estado == "Aceptado"){
            holder.color_estado.setImageDrawable(contexto.getDrawable(R.drawable.pendiente))
            holder.texto_estado.text = "Pendiente"
        }else if(item_actual.estado == "Listo para recoger"){
            holder.color_estado.setImageDrawable(contexto.getDrawable(R.drawable.preparando))
            holder.texto_estado.text = "Listo"
        }else if(item_actual.estado == "Completo"){
            holder.color_estado.setImageDrawable(contexto.getDrawable(R.drawable.completado))
            holder.texto_estado.text = "Completado"
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
                    lista_filtrada = lista_historial
                } else {
                    lista_filtrada = (lista_historial.filter {
                        it.nombre_articulo.toString().lowercase().contains(busqueda)
                    }) as MutableList<Reserva>
                }

                //Filtro precio maximo
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_historial
                } else if (busqueda.toIntOrNull() != null || busqueda.toDoubleOrNull() != null) {
                    lista_filtrada = (lista_historial.filter {
                        it.precio!!.toDouble() <= busqueda.toDouble()
                    }) as MutableList<Reserva>
                }

                //Filtro de switch
                lista_filtrada = lista_filtrada.filter {
                    if (switch[0]) {
                        it.estado == "Pendiente" || it.estado == "Aceptado" || it.estado == "Listo para recoger"
                    } else {
                        it.estado == "Rechazado" || it.estado == "Completo" || it.estado == "Pendiente" || it.estado == "Aceptado" || it.estado == "Listo para recoger"
                    }

                } as MutableList<Reserva>

                val resultados_filtro = FilterResults()
                resultados_filtro.values = lista_filtrada

                return resultados_filtro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                lista_filtrada = p1?.values as MutableList<Reserva>
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView = itemView.findViewById(R.id.item_historial_imagen)
        val nombre: TextView = itemView.findViewById(R.id.item_historial_nombre)
        val precio: TextView = itemView.findViewById(R.id.item_historial_precio)
        val fecha: TextView = itemView.findViewById(R.id.item_historial_fecha)
        val color_estado: ImageView = itemView.findViewById(R.id.item_historial_estado_color)
        val texto_estado: TextView = itemView.findViewById(R.id.item_historial_estado_texto)

    }

}