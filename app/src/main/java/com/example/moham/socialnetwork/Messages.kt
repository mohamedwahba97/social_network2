package com.example.moham.socialnetwork

class Messages {

    var date: String = ""
    var type: String = ""
    var time: String = ""
    var message: String = ""
    var from: String = ""

    constructor()

    constructor(date: String, type: String, time: String, message: String, from: String) {
        this.date = date
        this.type = type
        this.time = time
        this.message = message
        this.from = from

    }
}
