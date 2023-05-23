package com.example.tiendav1

import android.content.Context
import android.content.Intent
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

class Adaptador_Comprar(private val lista_articulos: MutableList<Articulo>, var filtros_check: List<Boolean>) :
    RecyclerView.Adapter<Adaptador_Comprar.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_articulos

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        //Todo por aqui
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_articulo, parent, false)
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

        holder.nombre.text = item_actual.nombre
//        holder.categoria.text = item_actual.categoria
//        holder.stock.text = "Stock: "+item_actual.cantidad.toString()
//        holder.puntos.text = item_actual.puntos.toString()
        holder.precio.text = item_actual.precio.toString()+"€"
//        holder.descripcion.text = item_actual.descripcion


        holder.carrito.setOnClickListener {
            contexto.startActivity(Intent(contexto, Comprar_articulo::class.java).putExtra("ID", item_actual.id))
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
                    lista_filtrada = lista_articulos
                } else {
                    lista_filtrada = (lista_articulos.filter {
                        it.nombre.toString().lowercase().contains(busqueda)
                    }) as MutableList<Articulo>
                }

                //Filtro precio maximo
                if (busqueda.isEmpty()) {
                    lista_filtrada = lista_articulos
                } else if (busqueda.toIntOrNull() != null || busqueda.toDoubleOrNull() != null) {
                    lista_filtrada = (lista_articulos.filter {
                        it.precio!!.toDouble() <= busqueda.toDouble()
                    }) as MutableList<Articulo>
                }

                //Filtro de checkboxes
                lista_filtrada = lista_filtrada.filter {
                    var posicion = Articulo.categorias.indexOf(it.categoria)
                    filtros_check.get(posicion)
                } as MutableList<Articulo>

                val resultados_filtro = FilterResults()
                resultados_filtro.values = lista_filtrada

                return resultados_filtro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                lista_filtrada = p1?.values as MutableList<Articulo>
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView = itemView.findViewById(R.id.item_comprar_imagen)
        val nombre: TextView = itemView.findViewById(R.id.item_comprar_nombre)
        val precio: TextView = itemView.findViewById(R.id.item_comprar_precio)
//        val descripcion: TextView = itemView.findViewById(R.id.articulo_descripcion)
//        val stock: TextView = itemView.findViewById(R.id.articulo_stock)
//        val puntos: TextView = itemView.findViewById(R.id.articulo_puntos)
//        val categoria: TextView = itemView.findViewById(R.id.articulo_categoria)
        val carrito: ImageView = itemView.findViewById(R.id.item_comprar_carrito)

    }

}