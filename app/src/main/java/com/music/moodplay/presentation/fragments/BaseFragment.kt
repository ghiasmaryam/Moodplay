package com.music.moodplay.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.music.moodplay.presentation.utils.CustomSharedPreference
import com.music.moodplay.presentation.utils.dialogs.LoadingDialog

open class BaseFragment: Fragment() {
    protected lateinit var loader: LoadingDialog
    protected var firebaseStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    protected val sharedPreference: CustomSharedPreference by lazy {
        CustomSharedPreference(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loader = LoadingDialog(requireActivity())
    }
}