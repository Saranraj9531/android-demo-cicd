package com.sparkout.chat.ui.photoedit.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sparkout.chat.R

// Created by krish on 23-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class TextEditorDialogFragment : DialogFragment() {
    private var mAddTextEditText: EditText? = null
    private var mAddTextDoneTextView: TextView? = null
    private var colorPickerViewDialog: VerticalSlideColorPicker? = null
    private var mInputMethodManager: InputMethodManager? = null
    private var mColorCode = 0
    private var mTextEditor: TextEditor? = null

    interface TextEditor {
        fun onDone(inputText: String?, colorCode: Int)
    }

    //Show dialog with provide text and text color
    companion object {
        val TAG = TextEditorDialogFragment::class.java.simpleName
        val EXTRA_INPUT_TEXT = "extra_input_text"
        val EXTRA_COLOR_CODE = "extra_color_code"

        fun show(appCompatActivity: AppCompatActivity,
                 inputText: String,
                 @ColorInt colorCode: Int): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment =
                TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager,
                          TAG)
            return fragment
        }

        //Show dialog with default text input as empty and text color white
        fun show(appCompatActivity: AppCompatActivity): TextEditorDialogFragment {
            return show(
                appCompatActivity,
                "",
                ContextCompat.getColor(appCompatActivity, R.color.white))
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)
        colorPickerViewDialog = view.findViewById(R.id.color_picker_view_dialog)
        colorPickerViewDialog!!.setOnColorChangeListener(object :
                                                             VerticalSlideColorPicker.OnColorChangeListener {
            override fun onColorChange(selectedColor: Int) {
                mColorCode = selectedColor
                mAddTextEditText!!.setTextColor(selectedColor)
            }
        })
        mAddTextEditText!!.setText(arguments!!.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments!!.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText!!.setTextColor(mColorCode)
        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                              0)
        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView!!.setOnClickListener(View.OnClickListener { view1 ->
            mInputMethodManager!!.hideSoftInputFromWindow(view1.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText!!.getText().toString()
            if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                mTextEditor!!.onDone(inputText, mColorCode)
            }
        })
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditor: TextEditor?) {
        mTextEditor = textEditor
    }
}