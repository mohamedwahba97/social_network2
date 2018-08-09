package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PersonProfileActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var userProfName: TextView
    private lateinit var userStatus: TextView
    private lateinit var userCountry: TextView
    private lateinit var userGender: TextView
    private lateinit var userRelation: TextView
    private lateinit var userDOB: TextView
    private lateinit var userProfileImage: CircleImageView
    private lateinit var SendfriendReqButton: Button
    private lateinit var DeclinefriendRequestButton: Button
    private var FriendRequestRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var FriendRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()
    private lateinit var saveCurrentDate: String
    private lateinit var SenderUserId: String
    private lateinit var receiverUserId: String
    private lateinit var CURRENT_STATE: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_profile)

        mAuth = FirebaseAuth.getInstance()
        SenderUserId = mAuth.currentUser!!.uid
        receiverUserId = intent.extras.get("visit_user_id").toString()
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        FriendRequestRef = FirebaseDatabase.getInstance().reference.child("FriendRequests")
        FriendRef = FirebaseDatabase.getInstance().reference.child("Friends")

        IntializaeFields()

        UsersRef.child(receiverUserId).addValueEventListener(object : ValueEventListener {
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
                    MaintananceofButtons()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        DeclinefriendRequestButton.visibility = View.INVISIBLE
        DeclinefriendRequestButton.isEnabled = false

        if (SenderUserId != receiverUserId) {
            SendfriendReqButton.setOnClickListener {
                SendfriendReqButton.isEnabled = false

                if (CURRENT_STATE == "not_friends") {
                    SendfriendRequestToaPerson()
                }
                if (CURRENT_STATE == "request_sent") {
                    CancelFriendrequest()
                }
                if (CURRENT_STATE == "request_received") {
                    AcceptFriendRequest()
                }
                if (CURRENT_STATE == "Friends") {
                    UnFriendAnExistingFriend()
                }
            }
        } else {
            DeclinefriendRequestButton.visibility = View.INVISIBLE
            SendfriendReqButton.visibility = View.INVISIBLE

        }

    }

    @SuppressLint("SetTextI18n")
    private fun UnFriendAnExistingFriend() {
        FriendRef.child(SenderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FriendRef.child(receiverUserId).child(SenderUserId)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        SendfriendReqButton.isEnabled = true
                                        CURRENT_STATE = "not_friends"
                                        SendfriendReqButton.text = "send Friend Request"

                                        DeclinefriendRequestButton.visibility = View.INVISIBLE
                                        DeclinefriendRequestButton.isEnabled = false
                                    }
                                }
                    }
                }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun AcceptFriendRequest() {
        val calFordDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
        saveCurrentDate = currentDate.format(calFordDate.time)

        FriendRef.child(SenderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        FriendRef.child(receiverUserId).child(SenderUserId).child("date").setValue(saveCurrentDate)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        FriendRequestRef.child(SenderUserId).child(receiverUserId)
                                                .removeValue()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        FriendRequestRef.child(receiverUserId).child(SenderUserId)
                                                                .removeValue()
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {

                                                                        SendfriendReqButton.isEnabled = true
                                                                        CURRENT_STATE = "friends"
                                                                        SendfriendReqButton.text = "UnFriend this Person"

                                                                        DeclinefriendRequestButton.visibility = View.INVISIBLE
                                                                        DeclinefriendRequestButton.isEnabled = false
                                                                    }
                                                                }
                                                    }
                                                }
                                    }
                                }
                    }
                }
    }

    @SuppressLint("SetTextI18n")
    private fun CancelFriendrequest() {
        FriendRequestRef.child(SenderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FriendRequestRef.child(receiverUserId).child(SenderUserId)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        SendfriendReqButton.isEnabled = true
                                        CURRENT_STATE = "not_friends"
                                        SendfriendReqButton.text = "send Friend Request"

                                        DeclinefriendRequestButton.visibility = View.INVISIBLE
                                        DeclinefriendRequestButton.isEnabled = false
                                    }
                                }
                    }
                }
    }

    private fun MaintananceofButtons() {
        FriendRequestRef.child(SenderUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)) {
                            val request_type = dataSnapshot.child(receiverUserId).child("request_type").value!!.toString()
                            if (request_type == "sent") {
                                CURRENT_STATE = "request_sent"
                                SendfriendReqButton.text = "Cancel Friend request"

                                DeclinefriendRequestButton.visibility = View.INVISIBLE
                                DeclinefriendRequestButton.isEnabled = false
                            } else if (request_type == "received") {
                                CURRENT_STATE = "request_received"
                                SendfriendReqButton.text = "Accept Friend Request"

                                DeclinefriendRequestButton.visibility = View.VISIBLE
                                DeclinefriendRequestButton.isEnabled = true
                                DeclinefriendRequestButton.setOnClickListener { CancelFriendrequest() }
                            }
                        } else {
                            FriendRef.child(SenderUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserId)) {
                                        CURRENT_STATE = "Friends"
                                        SendfriendReqButton.text = "UnFriend this Person"

                                        DeclinefriendRequestButton.visibility = View.INVISIBLE
                                        DeclinefriendRequestButton.isEnabled = false
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }

    @SuppressLint("SetTextI18n")
    private fun SendfriendRequestToaPerson() {

        FriendRequestRef.child(SenderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FriendRequestRef.child(receiverUserId).child(SenderUserId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        SendfriendReqButton.isEnabled = true
                                        CURRENT_STATE = "request_sent"
                                        SendfriendReqButton.text = "Cancel Friend Request"

                                        DeclinefriendRequestButton.visibility = View.INVISIBLE
                                        DeclinefriendRequestButton.isEnabled = false
                                    }
                                }
                    }
                }

    }

    fun IntializaeFields() {

        userName = findViewById(R.id.person_username)
        userProfName = findViewById(R.id.person_full_name)
        userStatus = findViewById(R.id.person_profile_status)
        userCountry = findViewById(R.id.person_country)
        userGender = findViewById(R.id.person_gender)
        userRelation = findViewById(R.id.person_relationship_status)
        userDOB = findViewById(R.id.person_dob)
        userProfileImage = findViewById(R.id.person_profile_pic)
        SendfriendReqButton = findViewById(R.id.person_send_friend_request_btn)
        DeclinefriendRequestButton = findViewById(R.id.person_decline_friend_request)

        CURRENT_STATE = "not_friends"
    }

}
