package com.example.chatmain

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatmain.databinding.ActivitySetUpProfileBinding
import com.example.chatmain.model.User
import com.example.chatmain.utils.ConstHelper
import com.example.chatmain.utils.ConstHelper.PROFILE_PATH
import com.example.chatmain.utils.ConstHelper.USERS_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetUpProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetUpProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri?=null
    private  var dialog: ProgressDialog?=null

  /*  private var lunchActGoogle= registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val uri=it.data.toURI()
        val storage=FirebaseStorage.getInstance()
        val time=Date().time
        val reference=storage.reference
            .child(PROFILE_PATH)
            .child(time.toString()+"")
        reference.putFile(uri).addOnCompleteListener {

        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating...")
        dialog!!.setCancelable(false)
        auth = FirebaseAuth.getInstance()
        dataBase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        supportActionBar?.hide()


        binding.profileImage.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                startActivityForResult(intent,123)
            }

            binding.btnFinish.setOnClickListener {

                val name = binding.edName.text.toString()
                if (name.isEmpty()) {
                    binding.edName.error = "Please enter your name"
                }
                dialog?.show()
                if (selectedImage != null) {
                    val reference = storage.reference.child(PROFILE_PATH)
                        .child(auth.uid!!)
                    reference.putFile(selectedImage!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            reference.downloadUrl.addOnCompleteListener {data->
                                val user=User(auth.uid, binding.edName.text.toString(), auth.currentUser!!.phoneNumber, data.toString())
                                dataBase.reference
                                    .child(USERS_PATH)
                                    .child(auth.uid!!)
                                    .setValue(user)
                                    .addOnCompleteListener {
                                        dialog?.dismiss()
                                        val intent=Intent(this@SetUpProfileActivity,MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }else{
                            dataBase.reference
                                .child(USERS_PATH)
                                .child(auth.uid!!)
                                .setValue(User(auth.uid, binding.edName.text.toString(), auth.currentUser!!.phoneNumber, "No image"))
                                .addOnCompleteListener {
                                    dialog?.dismiss()
                                    val intent=Intent(this@SetUpProfileActivity,MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==123&&data != null&&data.data!=null) {
            val uri = data.data
            val storage = FirebaseStorage.getInstance()
            val time = Date().time
            val reference = storage.reference
                .child(PROFILE_PATH)
                .child(time.toString() + "")
            reference.putFile(uri!!).addOnCompleteListener {
                if(it.isSuccessful){
                    reference.downloadUrl.addOnCompleteListener {
                        val filePath=uri.toString()
                        val obj=java.util.HashMap<String,Any>()
                        obj["image"]=filePath
                        dataBase.reference
                            .child(USERS_PATH)
                            .child(FirebaseAuth.getInstance().uid!!)
                            .updateChildren(obj).addOnSuccessListener{

                            }
                    }
                }
            }
            binding.profileImage.setImageURI(data.data)
            selectedImage= data.data!!
        }
    }
}