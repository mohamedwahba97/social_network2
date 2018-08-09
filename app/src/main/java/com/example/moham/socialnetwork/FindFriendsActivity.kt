package com.example.moham.socialnetwork

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var SearchButton: ImageButton
    private lateinit var SearchInputText: EditText
    private lateinit var SearchResultList: RecyclerView
    private var allUsersDatabaseRef: DatabaseReference =  FirebaseDatabase.getInstance().reference
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")

        mToolbar = findViewById(R.id.find_friend_appbar_layout)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Find Friends"

        SearchResultList = findViewById(R.id.search_result_list)
        SearchResultList.setHasFixedSize(true)
        SearchResultList.layoutManager = LinearLayoutManager(this)

        SearchButton = findViewById(R.id.search_people_friends_button)
        SearchInputText = findViewById(R.id.search_box_input)

        SearchButton.setOnClickListener {
            val SearchBoxText = SearchInputText.text.toString()
            SearchPeopleAndFriends(SearchBoxText)

        }

    }

    private fun SearchPeopleAndFriends(searchBoxText: String) {

        Toast.makeText(this, "Searching ....", Toast.LENGTH_LONG).show()
        val searchPeopleandFriendsQuery = allUsersDatabaseRef.orderByChild("fullname").startAt(searchBoxText).endAt(searchBoxText + "\uf8ff")

        val options = FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleandFriendsQuery ,FindFriends::class.java).setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {

            override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): FindFriendsViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.all_users_display_layout, parent, false)
                return FindFriendsViewHolder(view)

            }

            override fun onBindViewHolder( holder: FindFriendsViewHolder, position: Int, model: FindFriends) {

                holder.setFullname(model.fullname)
                holder.setStatus(model.status)
                holder.setProfileimage(applicationContext, model.profileimage)

                holder.mview.setOnClickListener {
                    val visit_user_id = getRef(position).key
                    val profileIntent = Intent(this@FindFriendsActivity, PersonProfileActivity::class.java)
                    profileIntent.putExtra("visit_user_id", visit_user_id)
                    startActivity(profileIntent)

                }

            }

        }

        SearchResultList.adapter = firebaseRecyclerAdapter

    }

    inner class FindFriendsViewHolder(internal var mview: View) : RecyclerView.ViewHolder(mview) {

        fun setProfileimage(ctx: Context, profileimage: String) {
            val myImage = mview.findViewById<CircleImageView>(R.id.all_users_profile_image)
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage)
        }

        fun setFullname(fullname: String) {
            val myName = mview.findViewById<TextView>(R.id.all_users_profile_full_name)
            myName.text = fullname
        }

        fun setStatus(status: String) {
            val mystatus = mview.findViewById<TextView>(R.id.all_users_status)
            mystatus.text = status
        }

    }

}
