package com.example.moham.socialnetwork

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private var UserEmail: EditText? = null
    private var UserPassword: EditText? = null
    private var UserConfirmPassword: EditText? = null
    private var CreateAccountButton: Button? = null
    private var loadingBar: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        UserEmail = findViewById(R.id.register_email)
        UserPassword = findViewById(R.id.register_password)
        UserConfirmPassword = findViewById(R.id.register_confirm_password)
        CreateAccountButton = findViewById(R.id.register_create_account)
        loadingBar = ProgressDialog(this)

        CreateAccountButton!!.setOnClickListener { CreateNewAccount() }

    }

    override fun onStart() {
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            SendUserToMainActivity()
        }
        super.onStart()
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@RegisterActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun CreateNewAccount() {
        val email = UserEmail!!.text.toString()
        val password = UserPassword!!.text.toString()
        val confirmPassword = UserConfirmPassword!!.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password...", Toast.LENGTH_SHORT).show()
        } else if (password != confirmPassword) {
            Toast.makeText(this, "your password do not match with your confirm password...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar!!.setTitle("Creating New Account")
            loadingBar!!.setMessage("Please wait, while we are creating your new Account...")
            loadingBar!!.show()
            loadingBar!!.setCanceledOnTouchOutside(true)

            mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SendUserToSetupActivity()

                    Toast.makeText(this@RegisterActivity, "you are authenticated successfully...", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                } else {
                    val message = task.exception!!.message
                    Toast.makeText(this@RegisterActivity, "Error Occured: $message", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                }
            }
        }
    }


    private fun SendUserToSetupActivity() {
        val setupIntent = Intent(this@RegisterActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }

}
