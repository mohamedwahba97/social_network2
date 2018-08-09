package com.example.moham.socialnetwork

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ClickPostActivity : AppCompatActivity() {

    private lateinit var PostImage: ImageView
    private lateinit var PostDescription: TextView
    private lateinit var DeletePostButton: Button
    private lateinit var EditPostButton: Button
    private var clickPostRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()

    private lateinit var PostKey: String
    private lateinit var currantUserID: String
    private lateinit var datebaseUserID: String
    private lateinit var description: String
    private lateinit var image: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_post)

        mAuth = FirebaseAuth.getInstance()
        currantUserID = mAuth.currentUser!!.uid

        PostKey = intent.extras.get("PostKey").toString()
        clickPostRef = FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey)

        PostImage = findViewById(R.id.click_post_image)
        PostDescription = findViewById(R.id.click_post_description)
        DeletePostButton = findViewById(R.id.delete_post_button)
        EditPostButton = findViewById(R.id.edit_post_button)

        DeletePostButton.visibility = View.INVISIBLE
        EditPostButton.visibility = View.INVISIBLE

        clickPostRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").value.toString()
                    image = dataSnapshot.child("postimage").value.toString()
                    datebaseUserID = dataSnapshot.child("uid").value.toString()

                    PostDescription.text = description
                    Picasso.get().load(image).into(PostImage)

                    if (currantUserID == datebaseUserID) {
                        DeletePostButton.visibility = View.VISIBLE
                        EditPostButton.visibility = View.VISIBLE
                    }

                    EditPostButton.setOnClickListener { EditCurrentPost(description) }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        DeletePostButton.setOnClickListener { DeleteCurrentPost() }

    }

    private fun EditCurrentPost(description: String?) {

        val builder = AlertDialog.Builder(this@ClickPostActivity)
        builder.setTitle("Edit Post")

        val inputField = EditText(this@ClickPostActivity)
        inputField.setText(description)
        builder.setView(inputField)

        builder.setPositiveButton("Update") { dialog, which ->
            clickPostRef.child("description").setValue(inputField.text.toString())
            Toast.makeText(this@ClickPostActivity, "Post Update successfuly", Toast.LENGTH_LONG).show()
        }

        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        val dialog = builder.create()
        dialog.show()
        dialog.window.setBackgroundDrawableResource(android.R.color.holo_green_dark)

    }

    private fun DeleteCurrentPost() {
        clickPostRef.removeValue()
        SendUserToMainActivity()
        Toast.makeText(this, "Post Has been deleted", Toast.LENGTH_LONG).show()
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@ClickPostActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}
