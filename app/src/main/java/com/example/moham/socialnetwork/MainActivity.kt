package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var postList: RecyclerView
    private lateinit var mToolbar: Toolbar

    private lateinit var NavProfileImage: CircleImageView
    private lateinit var NavProfileUserName: TextView
    private lateinit var AddNewPostButton: ImageButton

    private var mAuth= FirebaseAuth.getInstance()
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var PostsRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var LikesRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Posts, PostsViewHolder>

    lateinit var currentUserID: String
    internal var Likechecker: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        LikesRef = FirebaseDatabase.getInstance().reference.child("Likes")

        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Home"

        AddNewPostButton = findViewById(R.id.add_new_post_button)

        drawerLayout = findViewById(R.id.drawable_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.navigation_view)

        postList = findViewById(R.id.all_users_post_list)
        postList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList.layoutManager = linearLayoutManager

        val navView = navigationView.inflateHeaderView(R.layout.navigation_header)
        NavProfileImage = navView.findViewById(R.id.nav_profile_image)
        NavProfileUserName = navView.findViewById(R.id.nav_user_full_name)

        UsersRef.child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        val fullname = dataSnapshot.child("fullname").value.toString()
                        NavProfileUserName.text = fullname

                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        val image = dataSnapshot.child("profileimage").value.toString()
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage)
                    } else {
                        Toast.makeText(this@MainActivity, "Profile name do not exists...", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        })

        navigationView.setNavigationItemSelectedListener { item ->
            UserMenuSelector(item)
            false
        }

        AddNewPostButton.setOnClickListener { SendUserToPostActivity() }

        DisplayAllUsersPosts()

    }

    private fun DisplayAllUsersPosts() {

        val sortPostsInDecendingorder = PostsRef.orderByChild("counter")

        val options = FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostsInDecendingorder ,Posts::class.java).setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {

            override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): PostsViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.all_posts_layout, parent, false)
                return PostsViewHolder(view)
            }

            override fun onBindViewHolder( holder: PostsViewHolder, position: Int, model: Posts) {
                val PostKey = getRef(position).key
                holder.setFullname(model.fullname)
                holder.setTime(model.time)
                holder.setDate(model.date)
                holder.setDescription(model.description)
                holder.setProfileimage(applicationContext, model.profileimage)
                holder.setPostimage(applicationContext, model.postimage)
                holder.setLikeButtonStatus(PostKey)

                holder.mview.setOnClickListener {
                    val clickPostIntent = Intent(this@MainActivity, ClickPostActivity::class.java)
                    clickPostIntent.putExtra("PostKey", PostKey)
                    startActivity(clickPostIntent)
                }

                holder.CommentPostButton.setOnClickListener {
                    val CommentsIntent = Intent(this@MainActivity, CommentsActivity::class.java)
                    CommentsIntent.putExtra("PostKey", PostKey)
                    startActivity(CommentsIntent)
                }

                holder.LikePostButton.setOnClickListener {
                    Likechecker = true

                    LikesRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (Likechecker == true) {
                                if (dataSnapshot.child(PostKey!!).hasChild(currentUserID)) {
                                    LikesRef.child(PostKey).child(currentUserID).removeValue()
                                    Likechecker = false
                                } else {
                                    LikesRef.child(PostKey).child(currentUserID).setValue(true)
                                }
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
            }

            override fun onChildChanged(type: ChangeEventType, snapshot: DataSnapshot, newIndex: Int, oldIndex: Int) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex)
                postList.scrollToPosition(newIndex)
            }

        }

        postList.adapter = firebaseRecyclerAdapter

    }

    class PostsViewHolder(internal var mview: View) : RecyclerView.ViewHolder(mview) {

        internal var LikePostButton: ImageButton = mview.findViewById(R.id.like_button)
        internal var CommentPostButton: ImageButton = mview.findViewById(R.id.comment_button)
        internal var DisplayNoOflikes: TextView = mview.findViewById(R.id.display_on_of_likes)
        internal var countLikes: Int = 0
        internal var currentUserId: String = FirebaseAuth.getInstance().currentUser!!.uid
        internal var LikesRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Likes")

        fun setLikeButtonStatus(PostKey: String?) {
            LikesRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(PostKey!!).hasChild(currentUserId)) {
                        countLikes = dataSnapshot.child(PostKey).childrenCount.toInt()
                        LikePostButton.setImageResource(R.drawable.like)
                        DisplayNoOflikes.text = Integer.toString(countLikes) + " Likes"
                    } else {
                        countLikes = dataSnapshot.child(PostKey).childrenCount.toInt()
                        LikePostButton.setImageResource(R.drawable.dislike)
                        DisplayNoOflikes.text = Integer.toString(countLikes) + " Likes"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        fun setFullname(fullname: String) {
            val username = mview.findViewById<TextView>(R.id.post_user_name)
            username.text = fullname
        }

        fun setProfileimage(ctx: Context, profileimage: String) {
            val image = mview.findViewById<CircleImageView>(R.id.post_profile_image)
            Picasso.get().load(profileimage).into(image)
        }

        @SuppressLint("SetTextI18n")
        fun setTime(time: String) {
            val PostTime = mview.findViewById<TextView>(R.id.post_time)
            PostTime.text = "   $time"
        }

        @SuppressLint("SetTextI18n")
        fun setDate(date: String) {
            val PostDate = mview.findViewById<TextView>(R.id.post_date)
            PostDate.text = "   $date"
        }

        fun setDescription(description: String) {
            val PostDescription = mview.findViewById<TextView>(R.id.post_description)
            PostDescription.text = description
        }

        fun setPostimage(ctx1: Context, postimage: String) {
            val PostImage = mview.findViewById<ImageView>(R.id.post_image)
            Picasso.get().load(postimage).into(PostImage)
        }

    }

    private fun SendUserToPostActivity() {
        val addNewPostIntent = Intent(this@MainActivity, PostActivity::class.java)
        startActivity(addNewPostIntent)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            SendUserToLoginActivity()
        } else {
            CheckUserExistence()
        }

    }

    private fun CheckUserExistence() {
        val current_user_id = mAuth.currentUser!!.uid

        UsersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun SendUserToSetupActivity() {
        val setupIntent = Intent(this@MainActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }


    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun UserMenuSelector(item: MenuItem) {

        when (item.itemId) {

            R.id.nav_post -> SendUserToPostActivity()

            R.id.nav_profile -> {
                SendUserToProfileActivity()
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_home -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()

            R.id.nav_friends -> {
                SendUserToFriendsActivity()
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_find_friends -> {
                SendUserToFindFriendsActivity()
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_messages -> {
                SendUserToChatActivity()
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_settings -> {
                SendUserToSettingsActivity()
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_Logout -> {
                mAuth.signOut()
                SendUserToLoginActivity()
            }
        }

    }

    private fun SendUserToChatActivity() {
        val FriendsIntent = Intent(this@MainActivity, ChatActivity::class.java)
        startActivity(FriendsIntent)
    }

    private fun SendUserToFriendsActivity() {
        val FriendsIntent = Intent(this@MainActivity, FriendsActivity::class.java)
        startActivity(FriendsIntent)
    }

    private fun SendUserToSettingsActivity() {
        val loginIntent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(loginIntent)
    }

    private fun SendUserToProfileActivity() {
        val loginIntent = Intent(this@MainActivity, ProfileActivity::class.java)
        startActivity(loginIntent)
    }

    private fun SendUserToFindFriendsActivity() {
        val loginIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(loginIntent)
    }

}
