package com.example.chatmain.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.example.chatmain.ChatActivity
import com.example.chatmain.R
import com.example.chatmain.databinding.ProfileItemBinding
import com.example.chatmain.model.User

class UserAdapter(var context: Context, var userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUserName.text=user.name
        //holder.binding.profileImageItem.load(user.profileImage)
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.defalt_avatar)
            .into(holder.binding.profileImageItem)

        holder.itemView.setOnClickListener {
            val intent= Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("image", user.profileImage)
            context.startActivity(intent)
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProfileItemBinding.bind(itemView)
    }
}