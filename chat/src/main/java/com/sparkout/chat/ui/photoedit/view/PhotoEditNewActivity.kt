package com.sparkout.chat.ui.photoedit.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.ImageFilePath
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.databinding.ActivityPhotoEditNewBinding
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.SaveSettings
import java.io.*
import kotlin.jvm.Throws

class PhotoEditNewActivity : AppCompatActivity(),
    StickerBSFragment.StickerListener,
    View.OnClickListener {
    private lateinit var imageUri: Uri
    private lateinit var mFileOutputPath: File
    private var mCameraPath: String = ""
    private var mPhotoEditor: PhotoEditor? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mSelectedColor = 0
    private var toId: String? = null
    var isclicable = true
    private lateinit var binding: ActivityPhotoEditNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditNewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_photo_edit_new)

        mPhotoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()
        binding.colorPickerView.setOnColorChangeListener(
            object : VerticalSlideColorPicker.OnColorChangeListener {
                override fun onColorChange(selectedColor: Int) {
                    mSelectedColor = selectedColor
                    if (binding.colorPickerView.visibility == View.VISIBLE) {
                        binding.imgPhotoEditPaint.setBackgroundColor(selectedColor)
                        mPhotoEditor!!.brushColor = selectedColor
                    }
                }
            })
        binding.imgPhotoEditBack.setOnClickListener(this)
        binding.imgPhotoEditUndo.setOnClickListener(this)
        binding.imgPhotoEditRedo.setOnClickListener(this)
        binding.imgPhotoEditCrop.setOnClickListener(this)
        binding.imgPhotoEditStickers.setOnClickListener(this)
        binding.imgPhotoEditText.setOnClickListener(this)
        binding.imgPhotoEditPaint.setOnClickListener(this)
        binding.fabPhotoDone.setOnClickListener(this)

        isclicable = true
        mStickerBSFragment =
            StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)

        try {
            toId = intent.getStringExtra("toId")
            Log.e("Nive ", "onCreate:Image " + intent.getStringExtra("imageUri"))
            mCameraPath = intent.getStringExtra("imageUri")!!
            imageUri =
                Uri.parse(intent.getStringExtra("imageUri"))
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            binding.photoEditorView.source.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        isclicable = true
        mPhotoEditor!!.addImage(bitmap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_photo_edit_back -> {
                finish()
            }
            R.id.img_photo_edit_undo -> {
                mPhotoEditor!!.undo()
            }
            R.id.img_photo_edit_redo -> {
                mPhotoEditor!!.redo()
            }
            R.id.img_photo_edit_crop -> {
            }
            R.id.img_photo_edit_stickers -> {
                if (isclicable) {
                    isclicable = false
                    showBrush(false)
                    mStickerBSFragment!!.show(
                        supportFragmentManager,
                        mStickerBSFragment!!.tag
                    )
                }
            }
            R.id.img_photo_edit_text -> {
                showBrush(false)
                val textEditorDialogFragment =
                    TextEditorDialogFragment.show(
                        this
                    )
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        mPhotoEditor!!.addText(inputText, colorCode)
                    }
                })
            }
            R.id.img_photo_edit_paint -> {
                if (binding.colorPickerView.visibility == View.VISIBLE) {
                    showBrush(false)
                } else {
                    showBrush(true)
                }
            }
            R.id.fab_photo_done -> {
                saveImage()
            }
        }
    }

    private fun showBrush(enableBrush: Boolean) {
        if (enableBrush) {
            mPhotoEditor!!.brushColor = mSelectedColor
            binding.imgPhotoEditPaint.setBackgroundColor(mSelectedColor)
            mPhotoEditor!!.setBrushDrawingMode(true)
            binding.colorPickerView.visibility = View.VISIBLE
        } else {
            binding.imgPhotoEditPaint.setBackgroundColor(resources.getColor(android.R.color.transparent))
            mPhotoEditor!!.setBrushDrawingMode(false)
            binding.colorPickerView.visibility = View.INVISIBLE
        }
    }

    /**
     * Method to save the edited image
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun saveImage() {
        val mMessageId = getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-").plus(toId)
        mCameraPath = ImageFilePath.getPath(this, imageUri)!!
        Log.e("Nive ", "saveImage:camerapath " + mCameraPath)
        mFileOutputPath = getCompressed(this, mCameraPath, mMessageId)
        val cw = ContextWrapper(applicationContext)
        val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
        val file =
            File(folder.absolutePath, getMessageId(mMessageId) + ".jpg")
        copy(mFileOutputPath.absolutePath, file)
        val saveSettings = SaveSettings.Builder()
            .setClearViewsEnabled(true)
            .setTransparencyEnabled(true)
            .build()
        mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : OnSaveListener {
            override fun onSuccess(imagePath: String) {
                setResult(Activity.RESULT_OK, Intent().putExtra("imagePath", imagePath))
                finish()
            }

            override fun onFailure(exception: Exception) {
                showToast(this@PhotoEditNewActivity, "Failed to save Image")
            }
        })
    }

    @Throws(IOException::class)
    fun copy(src: String, dst: File?) {
        val input: InputStream = FileInputStream(src)
        try {
            val out: OutputStream = FileOutputStream(dst)
            try {
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (input.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            } finally {
                out.close()
            }
        } finally {
            input.close()
        }
    }

    fun getCompressed(context: Context?, path: String, messageId: String): File {
        Log.e("Nivi ", "getCompressed:file path $path")
        if (context == null) throw NullPointerException("Context must not be null.")
        //getting device external cache directory, might not be available on some devices,
        // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
        var cacheDir = context.externalCacheDir
        if (cacheDir == null) //fall back
            cacheDir = context.cacheDir
        val rootDir = cacheDir!!.absolutePath + "/ImageCompressor"
        val root = File(rootDir)
        if (!root.exists()) root.mkdirs()
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap =
            decodeImageFromFiles(path,  /* your desired width*/300,  /*your desired height*/300)
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        val compressed = File(
            root,
            messageId + ".jpg" /*Your desired format*/
        )
        val fileOutputStream = FileOutputStream(compressed)
        fileOutputStream.write(byteArrayOutputStream.toByteArray())
        fileOutputStream.flush()
        fileOutputStream.close()
        //File written, return to the caller. Done!
        return compressed
    }

    fun decodeImageFromFiles(
        path: String,
        width: Int,
        height: Int
    ): Bitmap {
        val scaleOptions =
            BitmapFactory.Options()
        scaleOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, scaleOptions)
        var scale = 1
        while (scaleOptions.outWidth / scale / 2 >= width
            && scaleOptions.outHeight / scale / 2 >= height
        ) {
            scale *= 2
        }
        // decode with the sample size
        val outOptions =
            BitmapFactory.Options()
        outOptions.inSampleSize = scale
        Log.e("Nive ", "decodeImageFromFiles: " + path)
        Log.e("Nive ", "decodeImageFromFiles:outOptions " + outOptions)
        return BitmapFactory.decodeFile(path, outOptions)
    }
}
