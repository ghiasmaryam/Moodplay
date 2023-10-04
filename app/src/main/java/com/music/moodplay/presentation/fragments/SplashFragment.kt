package com.music.moodplay.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.music.moodplay.R
import com.music.moodplay.databinding.FragmentSplashBinding
import com.music.moodplay.presentation.activities.MainActivity

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handle = Handler(Looper.getMainLooper())
        handle.postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_thumbFragment)
        }, 3000)
    }
}