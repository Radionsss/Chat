package com.example.chatmain.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chatmain.R
import com.example.chatmain.databinding.DeleteLayoutBinding
import com.example.chatmain.databinding.RecieveMsgBinding
import com.example.chatmain.databinding.SendMsgBinding
import com.example.chatmain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(
    var context: Context,
    messageList: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var messageList: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVER = 2
    var senderRoom: String
    var receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMessageHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.recieve_msg, parent, false)
            ReceiverMessageHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messageList[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVER
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messages = messageList[position]
        if (holder.javaClass == SentMessageHolder::class.java) {
            val viewHolder = holder as SentMessageHolder
            if (messages.message.equals("photo")) {
                viewHolder.binding.imMessage.visibility = View.VISIBLE
                viewHolder.binding.linearMessage.visibility = View.GONE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.imMessage.load(messages.imageUri)
            }
            viewHolder.binding.tvMessage.text = messages.message

            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.tvDeleteEveryone.setOnClickListener {
                    messages.message = "This message has been deleted"
                    messages.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(messages)
                    }
                    messages.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(messages)
                    }
                    dialog.dismiss()
                }
                binding.tvDeleteForMe.setOnClickListener {
                    messages.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.tvCancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }else{
            val viewHolder = holder as ReceiverMessageHolder
            if (messages.message.equals("photo")) {
                viewHolder.binding.imMessage.visibility = View.VISIBLE
                viewHolder.binding.linearMessage.visibility = View.GONE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.imMessage.load(messages.imageUri)
            }
            viewHolder.binding.tvMessage.text = messages.message

            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.tvDeleteEveryone.setOnClickListener {
                    messages.message = "This message has been deleted"
                    messages.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(messages)
                    }
                    messages.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(messages)
                    }
                    dialog.dismiss()
                }
                binding.tvDeleteForMe.setOnClickListener {
                    messages.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.tvCancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
    }

    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiverMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecieveMsgBinding.bind(itemView)
    }

    init {
        if (messageList != null) {
            this.messageList = messageList
        }
        this.receiverRoom = receiverRoom
        this.senderRoom = senderRoom
    }
}