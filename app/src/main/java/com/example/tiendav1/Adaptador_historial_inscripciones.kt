package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Adaptador_historial_inscripciones(private val lista_inscripciones: MutableList<Inscripcion>, var filtros_check: List<Boolean>) :
    RecyclerView.Adapter<Adaptador_historial_inscripciones.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_inscripciones

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_historia_inscripcion, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

        Glide.with(contexto)
            .load(item_actual.url_evento)
            .apply(Utilidades.opcionesGlide(contexto))
            .transition(Utilidades.transicion)
            .into(holder.imagen)

        holder.nombre.text = item_actual.nombre_evento
        holder.fecha.text = item_actual.fecha

        holder.layout.setOnClickListener {
//            contexto.startActivity(Intent(contexto, Normal_detalle_inscripcion::class.java).putExtra("Evento", item_actual))
        }

        if(item_actual.estado == "Pendiente") {
            holder.estado_texto.text = "Pendiente"
            holder.estado_color.setImageDrawable(contexto.getDrawable(R.drawable.pendiente))
            holder.itemView.setBackgroundResource(R.drawable.borde_recycler2)
        }else if(item_actual.estado == "Aceptado") {
            holder.estado_texto.text = "Aceptado"
            holder.estado_color.setImageDrawable(contexto.getDrawable(R.drawable.completado))
            holder.itemView.setBackgroundResource(R.drawable.borde_recycler1)
        }else if (item_actual.estado == "Rechazado"){
            holder.estado_texto.text = "Rechazado"
            holder.estado_color.setImageDrawable(contexto.getDrawable(R.drawable.rechazar))
            holder.itemView.setBackgroundResource(R.drawable.borde_recycler_rechazado)
        }

    }

    override fun getItemCount(): Int {
        return lista_filtrada!!.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString().lowercase()

                //Filtro por nombre de evento
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_inscripciones
                } else {
                    lista_filtrada = (lista_inscripciones.filter {
                        it.nombre_evento.toString().lowercase().contains(busqueda)
                    }) as MutableList<Inscripcion>
                }

                //Filtro nombre usuario
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_inscripciones
                } else {
                    lista_filtrada = (lista_inscripciones.filter {
                        it.nombre_usuario.toString().lowercase().contains(busqueda)
                    }) as MutableList<Inscripcion>
                }

                lista_filtrada = lista_filtrada.filter {
                    var posicion: Int = 0

                    if (it.estado == "Pendiente") {
                        posicion = 0
                    }else if (it.estado == "Rechazado"){
                        posicion = 1
                    } else if(it.estado == "Aceptado"){
                        posicion = 2
                    }

                    filtros_check[posicion]

                } as MutableList<Inscripcion>

                val resultados_filtro = FilterResults()
                resultados_filtro.values = lista_filtrada

                return resultados_filtro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                lista_filtrada = p1?.values as MutableList<Inscripcion>
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView = itemView.findViewById(R.id.historia_inscripciones_evento_imagen)
        val nombre: TextView = itemView.findViewById(R.id.historia_inscripciones_evento_nombre)
        val fecha: TextView = itemView.findViewById(R.id.historia_inscripciones_evento_fecha)
        val estado_color:ImageView = itemView.findViewById(R.id.historia_inscripciones_estado_color)
        val estado_texto:TextView = itemView.findViewById(R.id.historia_inscripciones_estado_texto)
        val layout:ConstraintLayout = itemView.findViewById(R.id.historia_inscripciones_todo)

    }

}