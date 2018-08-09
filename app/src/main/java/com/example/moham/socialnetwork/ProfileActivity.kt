package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var userProfName: TextView
    private lateinit var userStatus: TextView
    private lateinit var userCountry: TextView
    private lateinit var userGender: TextView
    private lateinit var userRelation: TextView
    private lateinit var userDOB: TextView
    private lateinit var userProfileImage: CircleImageView
    private var profileUserRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()
    private lateinit var currantUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        currantUserId = mAuth.currentUser!!.uid
        profileUserRef = FirebaseDatabase.getInstance().reference.child("Users").child(currantUserId)

        userName = findViewById(R.id.my_username)
        userProfName = findViewById(R.id.my_profile_full_name)
        userStatus = findViewById(R.id.my_profile_status)
        userCountry = findViewById(R.id.my_country)
        userGender = findViewById(R.id.my_gender)
        userRelation = findViewById(R.id.my_relationship_status)
        userDOB = findViewById(R.id.my_dob)
        userProfileImage = findViewById(R.id.my_profile_pic)

        profileUserRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage)

                    userName.text = "@$myuserName"
                    userProfName.text = myuserProfileName
                    userStatus.text = myProfileStatus
                    userDOB.text = "DOB :$myDOB"
                    userCountry.text = "Country :$myCountry"
                    userGender.text = "Gender :$myGender"
                    userRelation.text = "Relationship :$myRelationStatus"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }
}
