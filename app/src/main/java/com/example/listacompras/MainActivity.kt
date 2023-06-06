package com.example.listacompras

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.listacompras.databinding.ActivityMainBinding
import com.example.listacompras.databinding.ItemBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tratarLogin()

        binding.fab.setOnClickListener {
            novoItem()
        }
    }

    fun tratarLogin() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
            val intent = AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            startActivityForResult(intent, 1)
        } else {
            configurarBase()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Autenticado", Toast.LENGTH_LONG).show()
            configurarBase()

        } else {
            finishAffinity()
        }
    }

    fun configurarBase() {
        FirebaseAuth.getInstance().currentUser?.let {
            database = FirebaseDatabase.getInstance().reference.child(it.uid)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tratarDadosProdutos(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MainActivity",  "configurarBase", error.toException())
                    Toast.makeText(this@MainActivity,
                        "Erro de conexão",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }

            database.child("produtos").addValueEventListener(valueEventListener)
        }
    }

    fun novoItem() {
        val editText = EditText(this)
        editText.hint = "Nome do item"

        AlertDialog.Builder(this)
            .setTitle("Novo item")
            .setView(editText)
            .setPositiveButton("Inserir") { dialog, button ->

                var produto = Produto(nome = editText.text.toString())
                var novoNOh = database.child("produtos").push()
                produto.id = novoNOh.key
                novoNOh.setValue(produto)
            }
            .create()
            .show()
    }

    fun tratarDadosProdutos(dataSnapshot: DataSnapshot){
        val listaProdutos = arrayListOf<Produto>()

        if(dataSnapshot.exists()){
            dataSnapshot.children.forEach {
                val produto = it.getValue(Produto::class.java)

                produto?.let { it -> listaProdutos.add(it) }
            }
        }
        atualizarTela(listaProdutos)
    }

    fun atualizarTela(lista: List<Produto>){
        //0 - limpar o container
        binding.container.removeAllViews()

        lista.forEach {
            //1- infla o elemento que representa um item da lisa
            val item = ItemBinding.inflate(layoutInflater)
            //2- confg os atributos no elemento
            item.nome.text = it.nome
            item.comprado.isChecked = it.comprado

            item.excluir.setOnClickListener { view ->
                it.id?.let {
                    val noh =  database.child("produtos").child(it)
                    noh.removeValue()
                }
            }

            item.comprado.setOnCheckedChangeListener { button, isChecked ->
                it.id?.let {
                    val noh = database.child("produtos").child(it)
                    noh.child("comprado").setValue(isChecked)
                }
            }
            //3- coloca o elemento dentro do container
            binding.container.addView(item.root)
        }
    }
}

