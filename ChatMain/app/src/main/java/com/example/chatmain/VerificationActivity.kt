package com.example.chatmain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatmain.databinding.ActivityMainBinding
import com.example.chatmain.databinding.ActivityVerificationBinding
import com.example.chatmain.utils.ConstHelper.NUMBER_PHONE_KEY
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        checkUser()

        supportActionBar?.hide()
        binding.edPhoneNumber.requestFocus()
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, OTPActivity::class.java)
            intent.putExtra(NUMBER_PHONE_KEY, binding.edPhoneNumber.text.toString())
            startActivity(intent)
        }
    }

    private fun checkUser() {
        if (auth!!.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}