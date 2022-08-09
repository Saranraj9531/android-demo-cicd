package com.sparkout.chat.ui.pinchzoom.view

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.databinding.ActivityImageZoomBinding
import java.io.File

class ImageZoomActivity : AppCompatActivity(), View.OnClickListener {
    var mPhoto: String? = null
    var value = 0
    private lateinit var binding: ActivityImageZoomBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.photoView.maximumScale = 5.0f
        binding.imageviewBack.setOnClickListener(this)

        value = intent.getIntExtra("value", 0)
        if (value == 1) {
            mPhoto = intent.getStringExtra("PATH")
            val file = File(mPhoto!!)
            val imageUri = Uri.fromFile(file)
            Glide.with(this)
                .load(imageUri).into(binding.photoView)
        } else if (value == 2) {
            binding.progressBar.visibility = View.VISIBLE
            mPhoto = intent.getStringExtra("URL")
            if (mPhoto != null) {
                Glide.with(this)
                    .setDefaultRequestOptions(BaseUtils.requestOptionsD()!!)
                    .load(mPhoto)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            @NonNull target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE

                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any,
                            target: Target<Drawable?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }
                    }).into(binding.photoView)
            }
        }
    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.imageview_back -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
