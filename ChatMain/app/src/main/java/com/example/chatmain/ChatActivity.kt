package com.example.chatmain

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.chatmain.adapters.MessageAdapter
import com.example.chatmain.databinding.ActivityChatBinding
import com.example.chatmain.model.Message
import com.example.chatmain.utils.ConstHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var messages: ArrayList<Message>
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog
    private var senderUid: String? = null
    private var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        dialog = ProgressDialog(this)
        dataBase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog.setMessage("Updating...")
        dialog.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val imageProfile = intent.getStringExtra("image")
        receiverUid = intent.getStringExtra("uid")
        binding.tvName.text = name
        binding.profileImageItemChat.load(imageProfile)
        binding.btnBack.setOnClickListener { finish() }
        senderUid = FirebaseAuth.getInstance().uid

        dataBase.reference.child(ConstHelper.PRESENCE_PATH).child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status=snapshot.getValue(String::class.java)
                    if(status=="offline"){
                        binding.tvStatus.visibility= View.GONE
                    }else{
                        binding.tvStatus.text=status
                        binding.tvStatus.visibility= View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        senderRoom=senderUid+receiverUid
        receiverRoom=receiverUid+senderUid

        adapter= MessageAdapter(this,messages,senderRoom!!,receiverRoom!!)
        binding.rcViewChat.layoutManager= LinearLayoutManager(this)
        binding.rcViewChat.adapter=adapter
        dataBase.reference.child("chats")
            .child(senderRoom!!)
            .child(ConstHelper.MESSAGE_PATH)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for(snapshot1 in snapshot.children) {
                        val message=snapshot1.getValue(Message::class.java)
                        message!!.messageId=snapshot1.key
                        messages.add(message)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.btnSend.setOnClickListener {
            val textMessage =binding.edMassage.text.toString()
            val date= Date()
            val message=Message(textMessage,senderUid,date.time)

            binding.edMassage.setText("")
            val randomKey=dataBase.reference.push().key
            val lastMsgObj=HashMap<String,Any>()
            lastMsgObj["lastMsg"]=message.message!!
            lastMsgObj["lastMsgTime"]=date.time

            dataBase.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            dataBase.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
            dataBase.reference.child("chats").child(senderRoom!!)
                .child(ConstHelper.MESSAGE_PATH)
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    dataBase.reference.child("chats")
                        .child(receiverRoom!!)
                        .child(ConstHelper.MESSAGE_PATH)
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }
        }

        binding.imAttach.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1234)
        }

        val handler=Handler()
        binding.edMassage.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                dataBase.reference.child(ConstHelper.PRESENCE_PATH)
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStopping,1000)
            }
            var userStopping= Runnable {
                dataBase.reference.child(ConstHelper.PRESENCE_PATH)
                    .child(senderUid!!)
                    .setValue("online")
            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1234&&data != null&&data.data!=null) {
            val selectImage=data.data
            val calendar= Calendar.getInstance()
            val ref=storage.reference.child("chats")
                .child(calendar.timeInMillis.toString())
            dialog.show()
            ref.putFile(selectImage!!)
                .addOnCompleteListener{task->
                    dialog.dismiss()
                    if(task.isSuccessful){
                        ref.downloadUrl.addOnSuccessListener { uri->
                            val filePath=uri.toString()
                            val messageText=binding.edMassage.text.toString()
                            val date=Date()
                            val message=Message(messageText,senderUid,date.time)
                            message.message="photo"
                            message.imageUri=filePath
                            binding.edMassage.setText("")
                            val randomKey=dataBase.reference.push().key
                            val lastMsgObj=HashMap<String,Any>()
                            lastMsgObj["lastMsg"]=message.message!!
                            lastMsgObj["lastMsgTime"]=date.time

                            dataBase.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
                            dataBase.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
                            dataBase.reference.child("chats").child(senderRoom!!)
                                .child(ConstHelper.MESSAGE_PATH)
                                .child(randomKey!!)
                                .setValue(message).addOnSuccessListener {
                                    dataBase.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .child(ConstHelper.MESSAGE_PATH)
                                        .child(randomKey)
                                        .setValue(message)
                                        .addOnSuccessListener {  }
                                }
                        }
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId=FirebaseAuth.getInstance().uid
        dataBase.reference.child(ConstHelper.PRESENCE_PATH).child(currentId!!).setValue("Offline")
    }
    override fun onResume() {
        super.onResume()
        val currentId=FirebaseAuth.getInstance().uid
        dataBase.reference.child(ConstHelper.PRESENCE_PATH).child(currentId!!).setValue("Online")
    }

}