package com.ride.proyectomovilesridetransporte.models

class Client {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var image: String? = null

    constructor() {}
    constructor(id: String?, name: String?, email: String?) {
        this.id = id
        this.name = name
        this.email = email
    }

    constructor(id: String?, name: String?, email: String?, image: String?) {
        this.id = id
        this.name = name
        this.email = email
        this.image = image
    }

}