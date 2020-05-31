package com.example.gettext.ui.main

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.gettext.MainActivity
import com.example.gettext.R
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {




        val root =inflater.inflate(R.layout.main_fragment, container, false)
        return root

    }

    override fun onResume() {
        val activity: MainActivity? = activity as MainActivity?
        val image =activity!!.returnCapturedImage()
        if (image!=null) {



            layout_display_image_camera.setImageBitmap(image)


        }
        super.onResume()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    }




}
