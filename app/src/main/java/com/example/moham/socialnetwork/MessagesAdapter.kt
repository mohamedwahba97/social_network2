package com.example.moham.socialnetwork

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessagesAdapter(private val userMessagesList: List<Messages>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var mAuth= FirebaseAuth.getInstance()
    private var usersDatebaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val SenderMassageText: TextView = itemView.findViewById(R.id.sender_message_text)
        val ReceiverMassageText: TextView = itemView.findViewById(R.id.receiver_message_text)
        val receiverProfileImage: CircleImageView = itemView.findViewById(R.id.message_profile_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_layout_of_users, parent, false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val messageSenderID = mAuth.currentUser!!.uid
        val messages = userMessagesList[position]
        val fromUserID = messages.from
        val fromMessageType = messages.type

        usersDatebaseRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID)
        usersDatebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val image = dataSnapshot.child("profileimage").value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.receiverProfileImage)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (fromMessageType == "text") {
            holder.ReceiverMassageText.visibility = View.INVISIBLE
            holder.receiverProfileImage.visibility = View.INVISIBLE

            if (fromUserID == messageSenderID) {
                holder.SenderMassageText.setBackgroundResource(R.drawable.sender_message_text_background)
                holder.SenderMassageText.setTextColor(Color.WHITE)
                holder.SenderMassageText.gravity = Gravity.LEFT
                holder.SenderMassageText.text = messages.message

            } else {
                holder.SenderMassageText.visibility = View.INVISIBLE
                holder.ReceiverMassageText.visibility = View.VISIBLE
                holder.receiverProfileImage.visibility = View.VISIBLE

                holder.ReceiverMassageText.setBackgroundResource(R.drawable.receiver_message_text_background)
                holder.ReceiverMassageText.setTextColor(Color.WHITE)
                holder.ReceiverMassageText.gravity = Gravity.LEFT
                holder.ReceiverMassageText.text = messages.message
            }

        }

    }

    override fun getItemCount(): Int {
        return userMessagesList.size

    }

}
