package com.example.tiendav1

import android.content.Context
import android.content.Intent
import android.graphics.drawable.shapes.Shape
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

class Adaptador_pedidos(
    private val lista_pedidos: MutableList<Reserva>,
    var filtros_check: List<Boolean>
) :
    RecyclerView.Adapter<Adaptador_pedidos.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_pedidos

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        //Todo por aqui
        val vista_item =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
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

        holder.usuario.text = item_actual.nombre_usuario
        holder.fecha.text = item_actual.fecha

        if (item_actual.estado == "Pendiente" || item_actual.estado == "Aceptado") {
            holder.estado_color.setColorFilter(contexto.resources.getColor(R.color.naranja_suave))
            holder.estado_texto.text = "Pendiente"
        } else if (item_actual.estado == "Rechazado") {
            holder.estado_color.setColorFilter(contexto.resources.getColor(R.color.rechazar))
            holder.estado_texto.text = "Rechazado"
        } else if (item_actual.estado == "Listo para recoger"){
            holder.estado_color.setColorFilter(contexto.resources.getColor(R.color.completado))
            holder.estado_texto.text = "Completado"
        }

        holder.usuario.setOnClickListener {
            //todo te lleva al perfil del usuario con todos sus pedidos hechos
        }

        holder.estado_color.setOnClickListener {
            val actividad = Intent(contexto, Admin_gestion_pedido::class.java)
            actividad.putExtra("PEDIDO", item_actual)
            contexto.startActivity(actividad)
        }

        holder.estado_texto.setOnClickListener {
            val actividad = Intent(contexto, Admin_gestion_pedido::class.java)
            actividad.putExtra("PEDIDO", item_actual)
            contexto.startActivity(actividad)
        }


    }

    override fun getItemCount(): Int {
        return lista_filtrada!!.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString().lowercase()

                //Filtro por nombre de usuario
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_pedidos
                } else {
                    lista_filtrada = (lista_pedidos.filter {
                        it.nombre_usuario.toString().lowercase().contains(busqueda)
                    }) as MutableList<Reserva>
                }

                //Filtro por nombre de articulo
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_pedidos
                } else {
                    lista_filtrada = (lista_pedidos.filter {
                        it.nombre_articulo.toString().lowercase().contains(busqueda)
                    }) as MutableList<Reserva>
                }

                //Filtro de checkboxes
                lista_filtrada = lista_filtrada.filter {
                    var posicion: Int = 0

                    if (it.estado == "Listo para recoger") {
                        posicion = 1
                    }else if (it.estado == "Aceptado"){
                        posicion = 0
                    } else if(it.estado == "Rechazado"){
                        posicion = 2
                    }else if(it.estado == "Pendiente") {
                        posicion = 0
                    }else if(it.estado == "Completo") {
                        posicion = 1
                    }

                    filtros_check[posicion]

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

        val imagen: ImageView = itemView.findViewById(R.id.item_pedido_imagen)
        val usuario: TextView = itemView.findViewById(R.id.item_pedido_nombre_usuario)
        val fecha: TextView = itemView.findViewById(R.id.item_pedido_fecha)
        val estado_color: ImageView = itemView.findViewById(R.id.item_pedido_estado_color)
        val estado_texto: TextView = itemView.findViewById(R.id.item_pedido_estado_texto)
        val pedido: ConstraintLayout = itemView.findViewById(R.id.item_pedido_pedido)

    }

}