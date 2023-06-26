package com.example.tfgandroid.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.R

class StartFragment : Fragment() {

    companion object {
        const val TAG = "StartFragment"
    }

    private lateinit var buttonCreate: View
    private lateinit var buttonJoin: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        if (requireActivity().intent.getBooleanExtra("comesFromSession", false)) {
            requireActivity().intent.removeExtra("comesFromSession")
            openAfterGroupDialog()
        }

        buttonCreate = view.findViewById(R.id.start_button_1)
        buttonJoin = view.findViewById(R.id.start_button_2)

        buttonCreate.setOnClickListener {
            Log.d(TAG, "Create session")
            myActivity().createGroup()
        }

        buttonJoin.setOnClickListener {
            Log.d(TAG, "Join session")
            myActivity().searchGroups()
        }

        view.findViewById<View>(R.id.start_button_3).setOnClickListener{
            myActivity().goToMyGroups()
        }

        view.findViewById<ConstraintLayout>(R.id.start_button_4).setOnClickListener {
            myActivity().goToConfiguration()
        }

        return view
    }


    private fun myActivity(): MainActivity {
        return (requireActivity() as MainActivity)
    }

    private fun openAfterGroupDialog()
    {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.after_group_dialog)

        dialog.findViewById<ConstraintLayout>(R.id.after_group_accept_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}