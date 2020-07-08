package com.ride.proyectomovilesridetransporte.activities.driver

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.models.Driver
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.DriverProvider
import com.ride.proyectomovilesridetransporte.providers.ImagesProvider
import com.ride.proyectomovilesridetransporte.utils.FileUtil
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class UpdateProfileDriverActivity : AppCompatActivity() {
    private lateinit var mImageViewProfile: ImageView
    private lateinit var mButtonUpdate: Button
    private var mTextViewName: TextView? = null
    private var mTextViewBrandVehicle: TextView? = null
    private var mTextViewPlateVehicle: TextView? = null
    private var mDriverProvider: DriverProvider? = null
    private var mAuthProvider: AuthProvider? = null
    private lateinit var mCircleImageBack: CircleImageView
    private var mImageProvider: ImagesProvider? = null
    private lateinit var mImageFile: File
    private val mImage: String? = null
    private val GALLERY_REQUEST = 1
    private var mProgressDialog: ProgressDialog? = null
    private var mName: String? = null
    private var mVehicleBrand: String? = null
    private var mVehiclePlate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile_driver)
        //    MyToolbar.show(this, "Actualizar perfil", true);
        mImageViewProfile = findViewById(R.id.imageViewProfile)
        mButtonUpdate = findViewById(R.id.btnUpdateProfile)
        mTextViewName = findViewById(R.id.textInputName)
        mTextViewBrandVehicle = findViewById(R.id.textInputVehicleBrand)
        mTextViewPlateVehicle = findViewById(R.id.textInputVehiclePlate)
        mCircleImageBack = findViewById(R.id.circleImageBack)
        mDriverProvider = DriverProvider()
        mAuthProvider = AuthProvider()
        mImageProvider = ImagesProvider("driver_images")
        mProgressDialog = ProgressDialog(this)
        driverInfo
        mImageViewProfile.setOnClickListener { openGallery() }
        mButtonUpdate.setOnClickListener { updateProfile() }
        mCircleImageBack.setOnClickListener { finish() }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                mImageFile = FileUtil.from(this, data!!.data)
                mImageViewProfile!!.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()))
            } catch (e: Exception) {
                Log.d("ERROR", "Mensaje: " + e.message)
            }
        }
    }

    private val driverInfo: Unit
        private get() {
            mDriverProvider!!.getDriver(mAuthProvider!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").value.toString()
                        val vehicleBrand = dataSnapshot.child("vehicleBrand").value.toString()
                        val vehiclePlate = dataSnapshot.child("vehiclePlate").value.toString()
                        var image = ""
                        if (dataSnapshot.hasChild("image")) {
                            image = dataSnapshot.child("image").value.toString()
                            Picasso.with(this@UpdateProfileDriverActivity).load(image).into(mImageViewProfile)
                        }
                        mTextViewName!!.text = name
                        mTextViewBrandVehicle!!.text = vehicleBrand
                        mTextViewPlateVehicle!!.text = vehiclePlate
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun updateProfile() {
        mName = mTextViewName!!.text.toString()
        mVehicleBrand = mTextViewBrandVehicle!!.text.toString()
        mVehiclePlate = mTextViewPlateVehicle!!.text.toString()
        if (mName != "" && mImageFile != null) {
            mProgressDialog!!.setMessage("Espere un momento...")
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.show()
            saveImage()
        } else {
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImage() {
        mImageProvider!!.saveImage(this@UpdateProfileDriverActivity, mImageFile, mAuthProvider!!.id).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mImageProvider!!.storage.downloadUrl.addOnSuccessListener { uri ->
                    val image = uri.toString()
                    val driver = Driver()
                    driver.image = image
                    driver.name = mName
                    driver.id = mAuthProvider!!.id
                    driver.vehicleBrand = mVehicleBrand
                    driver.vehiclePlate = mVehiclePlate
                    mDriverProvider!!.update(driver).addOnSuccessListener {
                        mProgressDialog!!.dismiss()
                        Toast.makeText(this@UpdateProfileDriverActivity, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@UpdateProfileDriverActivity, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}