package com.example.listacompras

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.listacompras.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        }
    }

    fun novoItem() {
        val editText = EditText(this)
        editText.hint = "Nome do item"

        AlertDialog.Builder(this)
            .setTitle("Novo item")
            .setView(editText)
            .setPositiveButton("Inserir", null)
            .create()
            .show()
    }
}
