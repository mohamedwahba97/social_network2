package com.example.moham.socialnetwork

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
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

class SettingsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var userName: EditText
    private lateinit var userProfName: EditText
    private lateinit var userStatus: EditText
    private lateinit var userCountry: EditText
    private lateinit var userGender: EditText
    private lateinit var userRelation: EditText
    private lateinit var userDOB: EditText
    private lateinit var UpdateAccountSettingsButton: Button
    private lateinit var userProfImage: CircleImageView
    private var mAuth= FirebaseAuth.getInstance()
    private var SettingsuserRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var UserProfileImageRef: StorageReference
    private lateinit var loadingBar: ProgressDialog
    lateinit var currentUserID: String
    private lateinit var uploadTask : UploadTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        SettingsuserRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID)
        UserProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")


        mToolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Account Settings"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        loadingBar = ProgressDialog(this)

        userName = findViewById(R.id.settings_username)
        userProfName = findViewById(R.id.settings_profile_full_name)
        userStatus = findViewById(R.id.settings_status)
        userCountry = findViewById(R.id.settings_country)
        userGender = findViewById(R.id.settings_gender)
        userRelation = findViewById(R.id.settings_relationship_status)
        userDOB = findViewById(R.id.settings_dob)
        userProfImage = findViewById(R.id.settings_profile_image)
        UpdateAccountSettingsButton = findViewById(R.id.update_account_setting_buttons)

        userProfImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, Gallery_Pick)
        }


        SettingsuserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val myProfileImage = dataSnapshot.child("profileimage").value.toString()
                    val myuserName = dataSnapshot.child("username").value.toString()
                    val myuserProfileName = dataSnapshot.child("fullname").value.toString()
                    val myProfileStatus = dataSnapshot.child("status").value.toString()
                    val myDOB = dataSnapshot.child("dob").value.toString()
                    val myCountry = dataSnapshot.child("country").value.toString()
                    val myGender = dataSnapshot.child("gender").value.toString()
                    val myRelationStatus = dataSnapshot.child("relationshipstatus").value.toString()

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage)

                    userName.setText(myuserName)
                    userProfName.setText(myuserProfileName)
                    userStatus.setText(myProfileStatus)
                    userDOB.setText(myDOB)
                    userCountry.setText(myCountry)
                    userGender.setText(myGender)
                    userRelation.setText(myRelationStatus)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        })

        UpdateAccountSettingsButton.setOnClickListener { ValidateAccountInfo() }

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
            loadingBar.show()
            loadingBar.setCanceledOnTouchOutside(true)

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                loadingBar.setTitle("Profile Image")
                loadingBar.setMessage("Please wait, while we updating your profile image...")
                loadingBar.setCanceledOnTouchOutside(true)
                loadingBar.show()

                val resultUri = result.uri
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

                        Toast.makeText(this@SettingsActivity, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show()
                        val downloadUrl = task.result.toString()
                        SettingsuserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //val selfIntent = Intent(this@SettingsActivity, SettingsActivity::class.java)
                               // startActivity(selfIntent)

                                Toast.makeText(this@SettingsActivity, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show()
                                loadingBar.dismiss()
                            } else {
                                val message = task.exception!!.message
                                Toast.makeText(this@SettingsActivity, "Error Occured: $message", Toast.LENGTH_SHORT).show()
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

    fun ValidateAccountInfo() {

        val username = userName.text.toString()
        val Profilename = userProfName.text.toString()
        val status = userStatus.text.toString()
        val dob = userDOB.text.toString()
        val country = userCountry.text.toString()
        val gender = userGender.text.toString()
        val relation = userRelation.text.toString()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(Profilename)) {
            Toast.makeText(this, "Please write your Profilename ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write your status ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please write your dob ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please write your gender ...", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(relation)) {
            Toast.makeText(this, "Please write your relation ...", Toast.LENGTH_LONG).show()
        } else {
            loadingBar.setTitle("Profile Image")
            loadingBar.setMessage("Please wait, while we updating your profile image...")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            UpdateAccountInfo(username, Profilename, status, dob, country, gender, relation)
        }

    }

    private fun UpdateAccountInfo(username: String, profilename: String, status: String, dob: String, country: String, gender: String, relation: String) {

        val userMap = HashMap<String , Any>()
        userMap["username"] = username
        userMap["fullname"] = profilename
        userMap["status"] = status
        userMap["dob"] = dob
        userMap["country"] = country
        userMap["gender"] = gender
        userMap["relation"] = relation

        SettingsuserRef.updateChildren(userMap).addOnCompleteListener{task ->
            if (task.isSuccessful) {
                SendUserToMainActivity()
                Toast.makeText(this@SettingsActivity, "Account Settings Update Successfully", Toast.LENGTH_LONG).show()
                loadingBar.dismiss()
            } else {
                Toast.makeText(this@SettingsActivity, "Error Occured , While Updated account Settings Info", Toast.LENGTH_LONG).show()
                loadingBar.dismiss()
            }

        }
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    companion object {
        internal const val Gallery_Pick = 1
    }

}
