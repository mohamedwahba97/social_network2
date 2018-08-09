package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FriendsActivity : AppCompatActivity() {

    private lateinit var mFriendsList: RecyclerView
    private var FriendsRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()
    private lateinit var online_user_id: String
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Friends, FriendsViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        mAuth = FirebaseAuth.getInstance()
        online_user_id = mAuth.currentUser!!.uid
        FriendsRef = FirebaseDatabase.getInstance().reference.child("Friends").child(online_user_id)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        mFriendsList = findViewById(R.id.friend_list)
        mFriendsList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        mFriendsList.layoutManager = linearLayoutManager

        DisplayAllFriends()

    }

    private fun DisplayAllFriends() {

        val options = FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef ,Friends::class.java).setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.all_users_display_layout, parent, false)
                return FriendsViewHolder(view)

            }

            override fun onBindViewHolder(holder: FriendsViewHolder, position: Int, model: Friends) {
                holder.setDate(model.date)

                val usersIDs = getRef(position).key
                UsersRef.child(usersIDs!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userName = dataSnapshot.child("fullname").value.toString()
                            val profileImage = dataSnapshot.child("profileimage").value.toString()

                            holder.setFullname(userName)
                            holder.setProfileimage( applicationContext,profileImage)
                            holder.mView.setOnClickListener {
                                val Options = arrayOf<CharSequence>(userName + "s Profile", "Send Massage")
                                val builder = AlertDialog.Builder(this@FriendsActivity)
                                builder.setTitle("Select Option")
                                builder.setItems(Options) { dialog, which ->
                                    if (which == 0) {
                                        val profileintent = Intent(this@FriendsActivity, PersonProfileActivity::class.java)
                                        profileintent.putExtra("visit_user_id", usersIDs)
                                        startActivity(profileintent)

                                    }
                                    if (which == 1) {
                                        val chatintent = Intent(this@FriendsActivity, ChatActivity::class.java)
                                        chatintent.putExtra("visit_user_id", usersIDs)
                                        chatintent.putExtra("userName", usersIDs)
                                        startActivity(chatintent)

                                    }
                                }
                                builder.show()
                            }

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }

                })

            }

        }

        mFriendsList.adapter = firebaseRecyclerAdapter

    }

    class FriendsViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setProfileimage(ctx: Context, profileimage: String) {
            val myImage = mView.findViewById<CircleImageView>(R.id.all_users_profile_image)
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage)

        }

        fun setFullname(fullname: String) {
            val myName = mView.findViewById<TextView>(R.id.all_users_profile_full_name)
            myName.text = fullname
        }

        @SuppressLint("SetTextI18n")
        fun setDate(date: String) {
            val FriendsDate = mView.findViewById<TextView>(R.id.all_users_status)
            FriendsDate.text = "Friends Since : $date"
        }

    }

}
