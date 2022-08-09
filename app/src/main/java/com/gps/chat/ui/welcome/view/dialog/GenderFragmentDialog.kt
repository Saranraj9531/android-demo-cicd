package com.gps.chat.ui.welcome.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gps.chat.ui.welcome.view.WelcomeActivity
import com.gps.chat.R
import com.gps.chat.databinding.FragmentGenderDialogBinding
import com.gps.chat.utils.preventDoubleClick

/**
 * A simple [Fragment] subclass.
 */
class GenderFragmentDialog(private val welcomeActivity: WelcomeActivity) :
    BottomSheetDialogFragment(),
    View.OnClickListener {
    private lateinit var binding: FragmentGenderDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenderDialogBinding.inflate(layoutInflater)
        binding.tvMale.setOnClickListener(this)
        binding.tvFemale.setOnClickListener(this)
        binding.tvOthers.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(view: View?) {
        view?.let {
            preventDoubleClick(view)
            when (it.id) {
                R.id.tv_male -> {
                    this.dismiss()
                    welcomeActivity.setGender(binding.tvMale.text.toString())
                }
                R.id.tv_female -> {
                    this.dismiss()
                    welcomeActivity.setGender(binding.tvFemale.text.toString())
                }
                R.id.tv_others -> {
                    this.dismiss()
                    welcomeActivity.setGender(binding.tvOthers.text.toString())
                }
            }
        }

    }
}
