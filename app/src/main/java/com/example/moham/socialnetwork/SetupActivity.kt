package com.example.moham.socialnetwork

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SetupActivity : AppCompatActivity() {

    private lateinit var UserName: EditText
    private lateinit var FullName: EditText
    private lateinit var CountryName: EditText
    private lateinit var SaveInformationbuttion: Button
    private lateinit var ProfileImage: CircleImageView
    private lateinit var loadingBar: ProgressDialog

    private var mAuth= FirebaseAuth.getInstance()
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var UserProfileImageRef: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var uploadTask : UploadTask

    lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID)
        UserProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        UserName = findViewById(R.id.setup_username)
        FullName = findViewById(R.id.setup_full_name)
        CountryName = findViewById(R.id.setup_country_name)
        SaveInformationbuttion = findViewById(R.id.setup_information_button)
        ProfileImage = findViewById(R.id.setup_profile_image)
        loadingBar = ProgressDialog(this)

        SaveInformationbuttion.setOnClickListener { SaveAccountSetupInformation() }

        ProfileImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }

        UsersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        val image = dataSnapshot.child("profileimage").value.toString()
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage)
                    } else {
                        Toast.makeText(this@SetupActivity, "Please select profile image first.", Toast.LENGTH_SHORT).show()
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Gallery_Pick && resultCode == Activity.RESULT_OK && data != null) {
            val ImageUri = data.data

            CropImage.activity(ImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            loadingBar.setTitle("Profile Image")
            loadingBar.setMessage("Please wait, while we updating your profile image...")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                loadingBar.setTitle("Profile Image")
                loadingBar.setMessage("Please wait, while we updating your profile image...")
                loadingBar.show()
                loadingBar.setCanceledOnTouchOutside(true)

                val resultUri  = result.uri
                val filePath = UserProfileImageRef.child("$currentUserID.jpg")
                uploadTask  = filePath.putFile(resultUri)

                uploadTask.continueWithTask { p0 ->
                    if (!p0.isSuccessful) {
                        throw p0.exception!!
                    }
                    // Continue with the task to get the download URL
                    filePath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@SetupActivity, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show()
                        val downloadUrl  = task.result.toString()
                        UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                              //  val selfIntent = Intent(this@SetupActivity, SetupActivity::class.java)
                              //  startActivity(selfIntent)

                                Toast.makeText(this@SetupActivity, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show()
                                loadingBar.dismiss()
                            } else {
                                val message = task.exception!!.message
                                Toast.makeText(this@SetupActivity, "Error Occured: $message", Toast.LENGTH_SHORT).show()
                                loadingBar.dismiss()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }

    }

    private fun SaveAccountSetupInformation() {

        val username = UserName.text.toString()
        val fullname = FullName.text.toString()
        val country = CountryName.text.toString()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar.setTitle("Saving Information")
            loadingBar.setMessage("Please wait, while we are creating your new Account...")
            loadingBar.show()
            loadingBar.setCanceledOnTouchOutside(true)

            val userMap = HashMap<String , Any>()
            userMap.put("username", username)
            userMap.put("fullname", fullname)
            userMap.put("country", country)
            userMap.put("status", "Hey there, i am using Poster Social Network, developed by Coding Cafe.")
            userMap.put("gender", "none")
            userMap.put("dob", "none")
            userMap.put("relationshipstatus", "none")

            UsersRef.updateChildren(userMap).addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    SendUserToMainActivity()
                    Toast.makeText(this@SetupActivity, "your Account is created Successfully.", Toast.LENGTH_LONG).show()
                    loadingBar.dismiss()
                } else {
                    val message = task.exception!!.message
                    Toast.makeText(this@SetupActivity, "Error Occured: $message", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }

            }

        }
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@SetupActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    companion object {
        internal const val Gallery_Pick = 1
    }

}
