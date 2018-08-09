package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var loadingBar: ProgressDialog

    private lateinit var SelectPostImage: ImageView
    private lateinit var UpdatePostButton: Button
    private lateinit var PostDescription: EditText
    private lateinit var ImageUri: Uri
    private lateinit var Description: String

    private var PostsImagesRefrence: StorageReference = FirebaseStorage.getInstance().reference
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var PostsRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private lateinit var downloadUrl: String
    private lateinit var current_user_id: String
    private var countposts: Long = 0
    private lateinit var uploadTask : UploadTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth.currentUser!!.uid

        PostsImagesRefrence = FirebaseStorage.getInstance().reference
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        SelectPostImage = findViewById(R.id.select_post_image)
        UpdatePostButton = findViewById(R.id.update_post_button)
        PostDescription = findViewById(R.id.post_description)
        loadingBar = ProgressDialog(this)

        mToolbar = findViewById(R.id.update_post_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Update Post"

        SelectPostImage.setOnClickListener { OpenGallery() }

        UpdatePostButton.setOnClickListener { ValidatePostInfo() }

    }

    private fun ValidatePostInfo() {
        Description = PostDescription.text.toString()
        if (false) {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar.setTitle("Add New Post")
            loadingBar.setMessage("Please wait, while we are updating your new post...")
            loadingBar.show()
            loadingBar.setCanceledOnTouchOutside(true)

            StoringImageToFirebaseStorage()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun StoringImageToFirebaseStorage() {

        val calFordDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
        saveCurrentDate = currentDate.format(calFordDate.time)

        val calFordTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm")
        saveCurrentTime = currentTime.format(calFordTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        val filePath = PostsImagesRefrence.child("Post Images").child(ImageUri.lastPathSegment + postRandomName + ".jpg")

        uploadTask  = filePath.putFile(ImageUri)

        uploadTask.continueWithTask { p0 ->
            if (!p0.isSuccessful) {
                throw p0.exception!!
            }
            // Continue with the task to get the download URL
            filePath.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                Toast.makeText(this@PostActivity, "image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show()
                SavingPostInformationToDatabase()
            } else {
                val message = task.exception!!.message
                Toast.makeText(this@PostActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun SavingPostInformationToDatabase() {

        PostsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    countposts = dataSnapshot.childrenCount
                } else {
                    countposts = 0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        UsersRef.child(current_user_id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    val userFullName = dataSnapshot.child("fullname").value.toString()
                    val userProfileImage = dataSnapshot.child("profileimage").value.toString()

                    val postsMap = HashMap<String , Any>()
                    postsMap.put("uid", current_user_id)
                    postsMap.put("date", saveCurrentDate)
                    postsMap.put("time", saveCurrentTime)
                    postsMap.put("description", Description)
                    postsMap.put("postimage", downloadUrl)
                    postsMap.put("profileimage", userProfileImage)
                    postsMap.put("fullname", userFullName)
                    postsMap.put("counter", countposts)

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener{task ->
                        if (task.isSuccessful) {
                            SendUserToMainActivity()
                            Toast.makeText(this@PostActivity, "New Post is updated successfully.", Toast.LENGTH_SHORT).show()
                            loadingBar.dismiss()
                        } else {
                            Toast.makeText(this@PostActivity, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show()
                            loadingBar.dismiss()
                        }

                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun OpenGallery() {
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, Gallery_Pick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Gallery_Pick && resultCode == Activity.RESULT_OK && data != null) {
            ImageUri = data.data
            SelectPostImage.setImageURI(ImageUri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            SendUserToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun SendUserToMainActivity() {

        val mainIntent = Intent(this@PostActivity, MainActivity::class.java)
        startActivity(mainIntent)
    }

    companion object {

        private const val Gallery_Pick = 1
    }

}
