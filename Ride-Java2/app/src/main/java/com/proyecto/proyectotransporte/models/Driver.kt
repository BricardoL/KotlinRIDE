package com.proyecto.proyectotransporte.models

class Driver {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var vehicleBrand: String? = null
    var vehiclePlate: String? = null
    var image: String? = null

    constructor() {}
    constructor(id: String?, name: String?, email: String?, vehicleBrand: String?, vehiclePlate: String?) {
        this.id = id
        this.name = name
        this.email = email
        this.vehicleBrand = vehicleBrand
        this.vehiclePlate = vehiclePlate
    }

}