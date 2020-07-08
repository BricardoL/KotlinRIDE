package com.ride.proyectomovilesridetransporte.models

class HistoryBooking {
    var idHistoryBooking: String? = null
    var idClient: String? = null
    var idDriver: String? = null
    var destination: String? = null
    var origin: String? = null
    var time: String? = null
    var km: String? = null
    var status: String? = null
    var originLat = 0.0
    var originLng = 0.0
    var destinationLat = 0.0
    var destinationLng = 0.0
    var calificationClient = 0.0
    var calificationDriver = 0.0
    var timestamp: Long = 0

    constructor() {}
    constructor(idHistoryBooking: String?, idClient: String?, idDriver: String?, destination: String?, origin: String?, time: String?, km: String?, status: String?, originLat: Double, originLng: Double, destinationLat: Double, destinationLng: Double) {
        this.idHistoryBooking = idHistoryBooking
        this.idClient = idClient
        this.idDriver = idDriver
        this.destination = destination
        this.origin = origin
        this.time = time
        this.km = km
        this.status = status
        this.originLat = originLat
        this.originLng = originLng
        this.destinationLat = destinationLat
        this.destinationLng = destinationLng
    }

}