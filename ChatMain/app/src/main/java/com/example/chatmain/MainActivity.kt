package com.example.chatmain

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatmain.adapters.UserAdapter
import com.example.chatmain.databinding.ActivityMainBinding
import com.example.chatmain.databinding.ActivityVerificationBinding
import com.example.chatmain.model.User
import com.example.chatmain.utils.ConstHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var dataBase:FirebaseDatabase
    private lateinit var users:ArrayList<User>
    private lateinit var dialog: ProgressDialog
    private lateinit var adapter: UserAdapter
    private var user: User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.rcView.layoutManager=GridLayoutManager(this,2)
        dataBase.reference
            .child(ConstHelper.USERS_PATH)
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user=snapshot.getValue(User::class.java)////////////////////////////////////////////////////////////////
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        binding.rcView.adapter=adapter

        dataBase.reference.child(ConstHelper.USERS_PATH).addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for(snapshot1 in snapshot.children){
                    val user=snapshot1.getValue(User::class.java)
                    if(!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users.add(user)
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    override fun onResume() {
        super.onResume()
        val currentId=FirebaseAuth.getInstance().uid
        dataBase.reference.child(ConstHelper.PRESENCE_PATH).child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId=FirebaseAuth.getInstance().uid
        dataBase.reference.child(ConstHelper.PRESENCE_PATH).child(currentId!!).setValue("Offline")
    }

    private fun init() {
        dialog = ProgressDialog(this)
        dialog.setMessage("Load image...")
        dialog.setCancelable(false)

        dataBase= FirebaseDatabase.getInstance()
        users=ArrayList()
        adapter=UserAdapter(this,users)
    }
}