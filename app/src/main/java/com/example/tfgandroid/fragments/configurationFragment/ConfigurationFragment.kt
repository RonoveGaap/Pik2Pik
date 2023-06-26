package com.example.tfgandroid.fragments.configurationFragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.helpers.SharedPreferencesHelper

class ConfigurationFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var saveButton: ConstraintLayout
    private lateinit var saveButtonText: TextView
    private lateinit var backButton: ImageView

    private var canAccept = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        nameEditText = view.findViewById(R.id.configuration_name_edittext)
        saveButton = view.findViewById(R.id.configuration_button_accept)
        saveButtonText = view.findViewById(R.id.configuration_button_accept_text)
        backButton = view.findViewById(R.id.configuration_button_back)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        nameEditText.setText(SharedPreferencesHelper.getUsername(requireContext()))

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    canAccept = true
                    saveButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_1))
                } else {
                    canAccept = false
                    saveButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_1))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        saveButton.setOnClickListener {
            if (canAccept) {
                SharedPreferencesHelper.setUsername(requireContext(), nameEditText.text.toString())
                requireActivity().onBackPressed()
            }
        }

        return view
    }

}