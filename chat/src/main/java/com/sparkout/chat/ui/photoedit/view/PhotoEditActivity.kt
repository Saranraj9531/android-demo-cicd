package com.sparkout.chat.ui.photoedit.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.imgRotate
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.removeProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.showProgressDialog
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.databinding.ActivityPhotoEditBinding
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.ImagePickActivity
import com.vincent.filepicker.activity.ImagePickActivity.*
import com.vincent.filepicker.filter.entity.ImageFile
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.SaveSettings
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.jvm.Throws

class PhotoEditActivity : AppCompatActivity(), View.OnClickListener,
    StickerBSFragment.StickerListener {
    private val imageCompressedFilePaths: ArrayList<String> = ArrayList<String>()

    //create a single thread pool to our image compression class.
    private val mExecutorService =
        Executors.newFixedThreadPool(1)
    private var mPhotoEditor: PhotoEditor? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mSelectedColor = 0
    private var listImages: ArrayList<ImageFile> = ArrayList<ImageFile>()

    //    private var images: ArrayList<Image> = ArrayList<Image>()
    //    private var videosList: ArrayList<Image> = ArrayList<Image>()
    var listOfAllImages = ArrayList<String>()
    var listOfEditedImages =
        ArrayList<String>()
    var listOfEditedIds = ArrayList<String>()
    var selectedImage = 0
    private var toId: String? = null
    private lateinit var binding: ActivityPhotoEditBinding

    companion object {
        var isclicable = true
    }

    /**
     * isFrom -> 0 - ChatActivity
     * isFrom -> 1 - SecretChatActivity
     * isFrom -> 2 - GroupChatActivity
     */
    private var isFrom = 0

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.fabPhotoDone.setOnClickListener(this)
        binding.fabAddPhoto.setOnClickListener(this)

        isclicable = true
        val intent = Intent(this, ImagePickActivity::class.java)
        intent.putExtra(IS_NEED_CAMERA, false)
        intent.putExtra(IS_NEED_FOLDER_LIST, true)
        intent.putExtra(IS_NEED_IMAGE_PAGER, false)
        intent.putExtra(Constant.MAX_NUMBER, 5)
        startActivityForResult(intent, 5)

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

        mStickerBSFragment =
            StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)

        binding.imgBackward.setOnClickListener(this)
        binding.imgForward.setOnClickListener(this)

        if (null != getIntent().extras) {
            if (getIntent().getIntExtra("disableMultiple", 0) == 1) {
                binding.fabAddPhoto.visibility = View.GONE
            }
            toId = getIntent().getStringExtra("toId")
            isFrom = getIntent().getIntExtra("isFrom", 0)
        }
    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.fab_photo_done -> {
                saveImage(selectedImage, true, 3)
            }
            R.id.fab_add_photo -> {
                val intent = Intent(this, ImagePickActivity::class.java)
                intent.putExtra(IS_NEED_CAMERA, false)
                intent.putExtra(IS_NEED_FOLDER_LIST, true)
                intent.putExtra(Constant.MAX_NUMBER, 5)
                startActivityForResult(intent, 5)
            }
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
                val textEditorDialogFragment: TextEditorDialogFragment =
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
            R.id.img_backward -> {
                saveImage(selectedImage, false, 2)
            }
            R.id.img_forward -> {
                saveImage(selectedImage, false, 1)
            }
        }
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        isclicable = true
        mPhotoEditor!!.addImage(bitmap)
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

    @SuppressLint("MissingPermission")
    private fun saveImage(value: Int, last: Boolean, isFrom: Int) {
        showProgressDialog(this)
        /*val folder = filesDir
        if (!folder.isDirectory) {
            folder.mkdirs()
        }*/
        try {
            /*val file =
                File(sPhotoDirectoryPath + "/" + listOfEditedIds[value] + ".jpg")
            if (!file.exists()) {
                file.parentFile!!.mkdirs()
            }
            file.createNewFile()*/
            val cw = ContextWrapper(applicationContext)
            val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
            val file = File(folder, listOfEditedIds[value] + ".jpg")
            if (!file.exists()) {
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(file)
                    fos.flush()
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val saveSettings = SaveSettings.Builder()
                .build()
            mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : OnSaveListener {
                @SuppressLint("RestrictedApi")
                override fun onSuccess(imagePath: String) {
                    Log.e("Nive ", "onSuccess:Photo ")
                    removeProgressDialog()
                    listOfEditedImages[value] = imagePath
                    if (isFrom == 1) {
                        selectedImage += 1
                        if (selectedImage == listOfAllImages.size - 1) {
                            binding.fabPhotoDone.visibility = View.VISIBLE
                            binding.imgForward.visibility = View.GONE
                        } else {
                            binding.fabPhotoDone.visibility = View.GONE
                        }
                        binding.imgBackward.visibility = View.VISIBLE
                    } else if (isFrom == 2) {
                        selectedImage -= 1
                        if (selectedImage == 0) {
                            binding.imgBackward.visibility = View.GONE
                        }
                        binding.fabPhotoDone.visibility = View.GONE
                        binding.imgForward.visibility = View.VISIBLE
                    }
                    if (last) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("result", listOfEditedImages)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    } else {
                        loadImage(selectedImage)
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e("Nive ", "onFailure:Photo ")

                    removeProgressDialog()
                    Toast.makeText(
                        this@PhotoEditActivity,
                        "Failed to save Image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            removeProgressDialog()
            e.printStackTrace()
            Toast.makeText(
                this@PhotoEditActivity,
                e.message + " Issue Cause",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadImage(position: Int) {
        mPhotoEditor!!.clearAllViews()
        try {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val fileImage = File(listOfEditedImages[position])
            Log.e("Nive ", "loadImage: $fileImage")
            var image = MediaStore.Images.Media.getBitmap(
                contentResolver,
                Uri.fromFile(
                    fileImage
                )
            )
            val w = image!!.width.toFloat() //get width
            val h = image.height.toFloat() //get height
            val H = (h * width / w).toInt()
            val b = Bitmap.createScaledBitmap(image, width, H, false) //scale the bitmap
            image = null //save memory
            val bitmap: Bitmap = imgRotate(listOfEditedImages[position], b)
            binding.photoEditorView.source.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("Nive ", "onActivityResult:requestCode " + requestCode)
        if (requestCode == 5) {
            if (resultCode == Activity.RESULT_OK) {
                listImages = data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE)!!
                if (listImages.isNotEmpty()) {
                    listOfEditedIds.clear()
                    for (i in listImages.indices) {
                        val mMessageId =
                            listImages[i].id.toString().plus("-").plus(getUTCTime()).plus("-")
                                .plus(SharedPreferenceEditor.getData(Global.USER_ID)).plus("-")
                                .plus(toId)
                        listOfEditedIds.add(getMessageId(mMessageId))
                    }

                    if (listOfEditedIds.isNotEmpty()) {
                        for (i in listImages.indices) {
                            imageCompressedFilePaths.add(
                                getCompressed(
                                    this,
                                    listImages[i].path,
                                    listOfEditedIds[i]
                                )!!.absolutePath
                            )
                        }
                    }

                    if (imageCompressedFilePaths.isNotEmpty()) {
                        listOfAllImages.clear()
                        listOfEditedImages.clear()
                        listOfAllImages.addAll(imageCompressedFilePaths)
                        listOfEditedImages.addAll(imageCompressedFilePaths)

                        if (listOfAllImages.size == 0) {
                            finish()
                        } else {
                            selectedImage = 0
                            binding.imgBackward.visibility = View.GONE
                            if (listOfAllImages.size == 1) {
                                binding.fabPhotoDone.visibility = View.VISIBLE
                                binding.imgForward.visibility = View.GONE
                            } else {
                                binding.fabPhotoDone.visibility = View.GONE
                                binding.imgForward.visibility = View.VISIBLE
                            }
                            loadImage(0)
                        }
                    }
                }
            } else {
                finish()
            }
        }
    }

    @Throws(IOException::class)
    fun getCompressed(context: Context?, path: String, messageId: String): File? {
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
        path: String?,
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
        return BitmapFactory.decodeFile(path, outOptions)
    }
}
