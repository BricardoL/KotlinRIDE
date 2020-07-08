package com.ride.proyectomovilesridetransporte.activities.client

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
import com.ride.proyectomovilesridetransporte.models.Client
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.ClientProvider
import com.ride.proyectomovilesridetransporte.providers.ImagesProvider
import com.ride.proyectomovilesridetransporte.utils.FileUtil
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var mImageViewProfile: ImageView
    private lateinit var mButtonUpdate: Button
    private var mTextViewName: TextView? = null
    private var mClientProvider: ClientProvider? = null
    private var mAuthProvider: AuthProvider? = null
    private var mImageProvider: ImagesProvider? = null
    private lateinit var mCircleImageBack: CircleImageView
    private lateinit var mImageFile: File
    private val mImage: String? = null
    private val GALLERY_REQUEST = 1
    private var mProgressDialog: ProgressDialog? = null
    private var mName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        // MyToolbar.show(this, "Actualizar perfil", true);
        mImageViewProfile = findViewById(R.id.imageViewProfile)
        mButtonUpdate = findViewById(R.id.btnUpdateProfile)
        mTextViewName = findViewById(R.id.textInputName)
        mCircleImageBack = findViewById(R.id.circleImageBack)
        mClientProvider = ClientProvider()
        mAuthProvider = AuthProvider()
        mImageProvider = ImagesProvider("client_images")
        mProgressDialog = ProgressDialog(this)
        clientInfo
        mImageViewProfile.setOnClickListener(View.OnClickListener { openGallery() })
        mCircleImageBack.setOnClickListener(View.OnClickListener { finish() })
        mButtonUpdate.setOnClickListener(View.OnClickListener { updateProfile() })
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

    private val clientInfo: Unit
        private get() {
            mClientProvider!!.getClient(mAuthProvider!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").value.toString()
                        var image = ""
                        if (dataSnapshot.hasChild("image")) {
                            image = dataSnapshot.child("image").value.toString()
                            Picasso.with(this@UpdateProfileActivity).load(image).into(mImageViewProfile)
                        }
                        mTextViewName!!.text = name
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun updateProfile() {
        mName = mTextViewName!!.text.toString()
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
        mImageProvider!!.saveImage(this@UpdateProfileActivity, mImageFile, mAuthProvider!!.id).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mImageProvider!!.storage.downloadUrl.addOnSuccessListener { uri ->
                    val image = uri.toString()
                    val client = Client()
                    client.image = image
                    client.name = mName
                    client.id = mAuthProvider!!.id
                    mClientProvider!!.update(client).addOnSuccessListener {
                        mProgressDialog!!.dismiss()
                        Toast.makeText(this@UpdateProfileActivity, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@UpdateProfileActivity, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}