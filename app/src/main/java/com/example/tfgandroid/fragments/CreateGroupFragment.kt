package com.example.tfgandroid.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.helpers.DateHelper
import com.example.tfgandroid.helpers.GroupHelper
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var nextButton: ConstraintLayout
    private lateinit var nextText: TextView

    private var canGoNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_group, container, false)

        nameEditText = view.findViewById(R.id.create_group_name_edittext)
        backButton = view.findViewById(R.id.create_group_button_back)
        nextButton = view.findViewById(R.id.create_group_button_next)
        nextText = view.findViewById(R.id.create_group_button_next_text)

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    canGoNext = true
                    nextText.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_1))
                } else {
                    canGoNext = false
                    nextText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_1))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        backButton.setOnClickListener {
            (requireActivity() as MainActivity).goToMain(true)
        }

        nextButton.setOnClickListener {
            if (nameEditText.text.toString().trim().isNotEmpty()) {
                val group = Group(nameEditText.text.toString().trim(), DateHelper.getCurrentDateString(), GroupHelper.generateRandomPrefix(32))
                (requireActivity() as MainActivity).groupCreated(group)
            }
        }

        return view
    }

    companion object {
        const val TAG = "CreateGroupFragment"
    }
}