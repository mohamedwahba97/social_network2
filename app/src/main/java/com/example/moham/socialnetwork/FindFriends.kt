package com.example.moham.socialnetwork

class FindFriends {

    var profileimage: String = ""
    var fullname: String = ""
    var status: String = ""

    constructor() {
    }

    constructor(profileimage: String, fullname: String, status: String) {
        this.profileimage = profileimage
        this.fullname = fullname
        this.status = status
    }
}
