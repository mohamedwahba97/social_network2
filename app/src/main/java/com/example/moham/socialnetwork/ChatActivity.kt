package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var ChattoolBar: Toolbar
    private lateinit var SendMassageButton: ImageButton
    private lateinit var SendImagefileButton: ImageButton
    private lateinit var userMassageInput: EditText

    private lateinit var userMassageList: RecyclerView
    private val messagesList = ArrayList<Messages>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var messagesAdapter: MessagesAdapter

    private lateinit var massageReceiverID: String
    private lateinit var massageReceiverName: String
    private lateinit var messageSenderID: String
    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var receiverName: TextView

    private lateinit var receiverProfileImage: CircleImageView
    private var RootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth= FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        massageReceiverID = intent.extras.get("visit_user_id").toString()
        massageReceiverName = intent.extras!!.get("userName").toString()

        IntializeFields()
        DisplayReceiverInfo()

        SendMassageButton.setOnClickListener { SendMessage() }

        FatchMessages()

    }

    private fun FatchMessages() {

        RootRef.child("Messages").child(messageSenderID).child(massageReceiverID).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    val messages = dataSnapshot.getValue(Messages::class.java)
                    messagesList.add(messages!!)
                    messagesAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun SendMessage() {
        val messageText = userMassageInput.text.toString()
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message first", Toast.LENGTH_LONG).show()
        } else {
            val message_sender_ref = "Messages /$messageSenderID/$massageReceiverID"
            val message_receiver_ref = "Messages /$massageReceiverID/$messageSenderID"
            val user_massage_key = RootRef.child("Messages").child(messageSenderID).child(massageReceiverID).push()
            val message_push_id = user_massage_key.key

            val calFordDate = Calendar.getInstance()
            val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
            saveCurrentDate = currentDate.format(calFordDate.time)

            val calFordTime = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm  aa")
            saveCurrentTime = currentTime.format(calFordTime.time)

            val messageTextBody = HashMap<String , Any>()
            messageTextBody.put("message", messageText)
            messageTextBody.put("time", saveCurrentTime)
            messageTextBody.put("date", saveCurrentDate)
            messageTextBody.put("type", "text")
            messageTextBody.put("from", messageSenderID)

            val messageBodyDetails = HashMap<String , Any>()
            messageBodyDetails.put("$message_sender_ref/$message_push_id", messageTextBody)
            messageBodyDetails.put("$message_receiver_ref/$message_push_id", messageTextBody)
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@ChatActivity, "Message sent Successfuly", Toast.LENGTH_LONG).show()
                    userMassageInput.setText("")
                } else {
                    val message = task.exception!!.message
                    Toast.makeText(this@ChatActivity, "Message sent Successfuly$message", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    fun DisplayReceiverInfo() {
        receiverName.text = massageReceiverName
        RootRef.child("Users").child(massageReceiverID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val profileimage = dataSnapshot.child("profileimage").value!!.toString()
                    Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(receiverProfileImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    fun IntializeFields() {

        ChattoolBar = findViewById(R.id.chat_bar_layout)
        setSupportActionBar(ChattoolBar)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowCustomEnabled(true)

        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null)
        actionBar.customView = action_bar_view

        receiverName = findViewById(R.id.custom_profile_name)
        receiverProfileImage = findViewById(R.id.custom_profile_image)
        SendImagefileButton = findViewById(R.id.send_image_file_button)
        SendMassageButton = findViewById(R.id.send_massage_button)
        userMassageInput = findViewById(R.id.input_message)

        messagesAdapter = MessagesAdapter(messagesList)
        userMassageList = findViewById(R.id.messages_list_users)
        linearLayoutManager = LinearLayoutManager(this)
        userMassageList.setHasFixedSize(true)
        userMassageList.layoutManager = linearLayoutManager
        userMassageList.adapter = messagesAdapter

    }

}
