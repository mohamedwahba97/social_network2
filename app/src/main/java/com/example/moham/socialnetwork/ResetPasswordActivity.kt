package com.example.moham.socialnetwork

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private var ResetPasswordEmailButton: Button? = null
    private var ResetEmailInput: EditText? = null
    private var mToolbar: Toolbar? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        mAuth = FirebaseAuth.getInstance()

        mToolbar = findViewById(R.id.forget_password_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Reset Password"

        ResetPasswordEmailButton = findViewById(R.id.reset_password_email_button)
        ResetEmailInput = findViewById(R.id.reset_passwor_email)

        ResetPasswordEmailButton!!.setOnClickListener {
            val userEmail = ResetEmailInput!!.text.toString()
            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this@ResetPasswordActivity, "Please write your valid email address first...", Toast.LENGTH_SHORT).show()
            } else {
                mAuth!!.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@ResetPasswordActivity, "Please check your Email Account , if you want to Reset Password...", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    } else {
                        val message = task.exception!!.message
                        Toast.makeText(this@ResetPasswordActivity, "Error occured ...$message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}
