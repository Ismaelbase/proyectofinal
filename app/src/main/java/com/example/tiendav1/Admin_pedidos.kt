package com.example.tiendav1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tiendav1.databinding.FragmentAdminPedidosBinding

//Revisar pedidos
class Admin_pedidos : Fragment() {
    private var _binding: FragmentAdminPedidosBinding? = null
    private val binding get() = _binding!!
    private lateinit var principal_admin:Admin_principal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdminPedidosBinding.inflate(inflater, container, false)
        principal_admin = activity as Admin_principal

        //AQUI NADA
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}