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
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

//todo se puede pasar un booleano extra a los adaptadores que indique el estado actual del modo noche
// para así adaptar los iconos
class Adaptador_Comprar_Admin(private val lista_articulos: MutableList<Articulo>, var filtros_check: List<Boolean>) :
    RecyclerView.Adapter<Adaptador_Comprar_Admin.UserViewHolder>(), Filterable {

    private lateinit var contexto: Context
    private var lista_filtrada = lista_articulos

    private lateinit var referencia_bd: DatabaseReference
    private lateinit var referencia_almacenamiento: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        //Todo por aqui
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_inventario, parent, false)
        contexto = parent.context

        referencia_bd = FirebaseDatabase.getInstance().getReference()
        referencia_almacenamiento = FirebaseStorage.getInstance().getReference()

        return UserViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, posicion: Int) {
        val item_actual = lista_filtrada!![posicion]

//        if (posicion % 2 == 0) {
//            holder.itemView.setBackgroundColor(getColor(contexto, R.color.principal_morado))
//        } else {
//            holder.itemView.setBackgroundColor(getColor(contexto, R.color.principal_verde))
//        }

        if(item_actual.cantidad == 0) {
            holder.itemView.setBackgroundColor(getColor(contexto, R.color.naranja_suave))
        }

        Glide.with(contexto)
            .load(item_actual.url_foto)
            .transition(Utilidades.transicion)
            .into(holder.imagen)

        holder.nombre.text = item_actual.nombre
        holder.categoria.text = item_actual.categoria
        holder.stock.text = item_actual.cantidad.toString()
        holder.precio.text = item_actual.precio.toString()+"€"
//        holder.descripcion.text = item_actual.descripcion


        holder.editar.setOnClickListener {
            val intent = Intent(contexto, Admin_editar_articulo::class.java)
            intent.putExtra("ID", item_actual.id)
            contexto.startActivity(intent)
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

        val imagen: ImageView = itemView.findViewById(R.id.item_inventario_imagen)
        val nombre: TextView = itemView.findViewById(R.id.item_inventario_nombre)
        val precio: TextView = itemView.findViewById(R.id.item_inventario_precio)
//        val descripcion: TextView = itemView.findViewById(R.id.articulo_descripcion)
        val stock: TextView = itemView.findViewById(R.id.item_inventario_stock)
//        val puntos: TextView = itemView.findViewById(R.id.articulo_puntos)
        val categoria: TextView = itemView.findViewById(R.id.item_inventario_categoria)
        val editar: ImageView = itemView.findViewById(R.id.item_inventario_boton_editar)

    }

}