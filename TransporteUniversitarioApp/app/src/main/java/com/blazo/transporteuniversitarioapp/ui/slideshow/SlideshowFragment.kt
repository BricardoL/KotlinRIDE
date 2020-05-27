package com.blazo.transporteuniversitarioapp.ui.slideshow

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blazo.transporteuniversitarioapp.R
import kotlinx.android.synthetic.main.fragment_slideshow.*
import java.util.jar.Manifest


class SlideshowFragment : Fragment() {

    private val PHONE_CALL_REQUEST_CODE = 1

    private lateinit var slideshowViewModel: SlideshowViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {


        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        hacerLlamada()
        slideshowViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    fun hacerLlamada(){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel: 911")
        if (ActivityCompat.checkSelfPermission(this.requireContext(),android.Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED)  {
            startActivity(intent)
        }else{
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(android.Manifest.permission.CALL_PHONE), PHONE_CALL_REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PHONE_CALL_REQUEST_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                hacerLlamada()
            }else{
                Toast.makeText(this.context,"Debes dar el permiso", Toast.LENGTH_LONG).show()
            }
        }
    }

}
