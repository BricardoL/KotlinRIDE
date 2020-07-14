package com.ride.proyectomovilesridetransporte.providers

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.ride.proyectomovilesridetransporte.utils.CompressorBitmapImage
import java.io.File

class ImagesProvider(ref: String?) {
    var storage: StorageReference
        private set

    fun saveImage(context: Context?, image: File, idUser: String): UploadTask {
        val imageByte = CompressorBitmapImage.getImage(context, image.path, 500, 500)
        val storage = storage.child("$idUser.jpg")
        this.storage = storage
        return storage.putBytes(imageByte)
    }

    init {
        storage = FirebaseStorage.getInstance().reference.child(ref!!)
    }
}