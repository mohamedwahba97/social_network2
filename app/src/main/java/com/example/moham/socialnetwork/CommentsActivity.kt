package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : AppCompatActivity() {

    private lateinit var CommentList: RecyclerView
    private lateinit var postCommentButton: ImageButton
    private lateinit var CommentInputText: EditText
    private var mAuth= FirebaseAuth.getInstance()
    private var UsersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var PostsRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Comments, CommentsViewHolder>

    private lateinit var Post_Key: String
    private lateinit var current_user_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        Post_Key = intent.extras.get("PostKey").toString()
        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        PostsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(Post_Key).child("Comments")

        CommentList = findViewById(R.id.comments_list)
        CommentList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        CommentList.layoutManager = linearLayoutManager

        CommentInputText = findViewById(R.id.comment_input)
        postCommentButton = findViewById(R.id.post_comment_btn)

        postCommentButton.setOnClickListener {
            UsersRef.child(current_user_id).addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("username").value.toString()
                    validateComment(userName)
                    CommentInputText.setText("")
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

    }



    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostsRef ,Comments::class.java).setLifecycleOwner(this)
                .build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.all_comments_layout, parent, false)
                return CommentsViewHolder(view)

            }

            override fun onBindViewHolder(holder: CommentsViewHolder, position: Int, model: Comments) {
                holder.setUsername(model.username)
                holder.setComment(model.comment)
                holder.setDate(model.date)
                holder.setTime(model.time)
            }
        }

        CommentList.adapter = firebaseRecyclerAdapter

    }

    class CommentsViewHolder(private var mView: View) : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setUsername(username: String) {
            val myUserName = mView.findViewById<TextView>(R.id.comment_username)
            myUserName.text = "@$username  "
        }

        fun setComment(comment: String) {
            val myComment = mView.findViewById<TextView>(R.id.comment_text)
            myComment.text = comment
        }

        @SuppressLint("SetTextI18n")
        fun setDate(date: String) {
            val myDate = mView.findViewById<TextView>(R.id.comment_date)
            myDate.text = "  $date"
        }

        @SuppressLint("SetTextI18n")
        fun setTime(time: String) {
            val myTime = mView.findViewById<TextView>(R.id.comment_time)
            myTime.text = "  $time"
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun validateComment(userName: String) {
        val commentText = CommentInputText.text.toString()
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Please write Text to Comments...", Toast.LENGTH_SHORT).show()
        }
        else {
            val calFordDate = Calendar.getInstance()
            val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
            val saveCurrentDate = currentDate.format(calFordDate.time)

            val calFordTime = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm")
            val saveCurrentTime = currentTime.format(calFordTime.time)

            val RandomKey = current_user_id + saveCurrentDate + saveCurrentTime

            val commentsMap = HashMap<String , Any>()
            commentsMap.put("uid", current_user_id)
            commentsMap.put("comment", commentText)
            commentsMap.put("date", saveCurrentDate)
            commentsMap.put("time", saveCurrentTime)
            commentsMap.put("username", userName)
            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@CommentsActivity, "You have Commented Successfully ...", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@CommentsActivity, "Error Occured  , try again...", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}
