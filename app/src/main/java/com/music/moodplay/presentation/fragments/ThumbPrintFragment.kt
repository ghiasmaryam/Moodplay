package com.music.moodplay.presentation.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.music.moodplay.R
import com.music.moodplay.databinding.FragmentThumbPrintBinding
import com.music.moodplay.models.firebaseModels.FSPulseRatesModel
import com.music.moodplay.models.firebaseModels.FSSongsModel
import com.music.moodplay.models.pulseRateModels.OutputAnalyzer
import com.music.moodplay.presentation.utils.*
import com.music.moodplay.presentation.utils.constants.FirebaseEndPoints
import com.music.moodplay.presentation.utils.dialogs.CustomDialog
import com.music.moodplay.presentation.utils.dialogs.DialogListener

class ThumbPrintFragment : BaseFragment() {

    private lateinit var binding: FragmentThumbPrintBinding
    private var analyzer: OutputAnalyzer? = null
    private lateinit var pulseRateHandler: PulseRateHandler
    private lateinit var cameraService: CameraService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThumbPrintBinding.inflate(layoutInflater)
//        It is only for adding song by code don't uncomment.
//        addSongInFS()
        getDeviceId()
        ///just for testing.
//        getMoodFromFirebase(40F)
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                initView()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        pulseRateHandler = object : PulseRateHandler {
            override fun updatingData(code: Int, message: String, data: Float) {
                if (code == StaticFields.MESSAGE_UPDATE_REALTIME) {

                }
                if (code == StaticFields.MESSAGE_UPDATE_FINAL) {
                    cameraService.stop()
//                    StaticFields.toastClass("message", requireContext())
                    loader.startLoading()
                    getMoodFromFirebase(data)
                }
                if (code == StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE) {
                    CustomDialog.singleDialogWithListener(requireContext(),
                        message, listener = object : DialogListener {
                            override fun onPositiveClick() {

                            }

                            override fun onNegativeClick() {

                            }

                        })
                    analyzer?.stop()
                }
            }

        }
        cameraService = CameraService(requireActivity(), pulseRateHandler)
    }

    private fun getMoodFromFirebase(pulseRate: Float) {

        var foundPulse = false
        firebaseStore.collection(FirebaseEndPoints.pulseRate).get()
            .addOnSuccessListener {
                loader.isDismiss()
                for (item in it) {
                    val fsPulseRatesModel = item.toObject(FSPulseRatesModel::class.java)
                    if (fsPulseRatesModel.lowest <= pulseRate && fsPulseRatesModel.highest >= pulseRate) {
                        val bundle = bundleOf()
                        bundle.putInt(StaticFields.pulseRateKey, pulseRate.toInt())
                        bundle.putString(
                            StaticFields.fsPulseRatesKey,
                            StaticFields.modelToString(fsPulseRatesModel)
                        )
                        findNavController().navigate(
                            R.id.action_thumbPrintFragment_to_displaySongs,
                            bundle
                        )
                        foundPulse = true
                        break
                    }
                }
                if (!foundPulse) {
                    CustomDialog.singleDialogWithoutListener(
                        requireContext(),
                        "Please try again pulse rate does not measure correctly."
                    )
                }

            }
            .addOnFailureListener {
                loader.isDismiss()
                Log.d("FirebaseError", "ERorr ${it.toString()}")
            }
    }

    private fun initClicks() {
        binding.startLayout.setOnClickListener {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) -> {
                    initView()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
        }
    }

    private fun initView() {
        val previewSurfaceTexture: SurfaceTexture? = binding.textureView.surfaceTexture
        analyzer = OutputAnalyzer(requireActivity(), binding.graphTextureView, pulseRateHandler)
        if (previewSurfaceTexture != null) {
            val previewSurface = Surface(previewSurfaceTexture)

            if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                Snackbar.make(
                    binding.mainLayout,
                    getString(R.string.noFlashWarning),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            cameraService.start(previewSurface)
            analyzer?.measurePulse(binding.textureView, cameraService)
        }
    }

    override fun onPause() {
        super.onPause()
        cameraService.stop()
        if (analyzer != null) analyzer!!.stop()
        analyzer = OutputAnalyzer(requireActivity(), binding.graphTextureView, pulseRateHandler)
    }

    ///just for adding new song. testing
    private fun addSongInFS() {
        var fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        var songModel = FSSongsModel()
        songModel.songName = "AEROPLANE"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/AEROPLANE.mp3?alt=media&token=878b77c4-7257-408c-a491-9b0515972cbf"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = true
        songModel.sad = false
        songModel.key = fsKey.id
        fsKey.set(songModel)

        fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        songModel = FSSongsModel()
        songModel.songName = "Aila Re Aillaa"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/Aila%20Re%20Aillaa.mp3?alt=media&token=8fe59136-fda7-4a0d-aba1-ab4eb732ba49"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = true
        songModel.sad = false
        songModel.key = fsKey.id
        fsKey.set(songModel)

        fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        songModel = FSSongsModel()
        songModel.songName = "Abhagi Piya Ki"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/Abhagi%20Piya%20Ki.mp3?alt=media&token=75d03ba0-199a-45a9-9393-87e88391cfea"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = true
        songModel.sad = false
        songModel.key = fsKey.id
        fsKey.set(songModel)

        fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        songModel = FSSongsModel()
        songModel.songName = "Baaki Baatein Peene Baad"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/Baaki%20Baatein%20Peene%20Baad.mp3?alt=media&token=795baf0a-c0c1-4543-a936-3236e2967ecd"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = false
        songModel.sad = true
        songModel.key = fsKey.id
        fsKey.set(songModel)

        fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        songModel = FSSongsModel()
        songModel.songName = "Baari - bilal saeed"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/Baari%20-%20bilal%20saeed.mp3?alt=media&token=6ea8ded0-7ad3-425f-adc8-83411518a08a"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = false
        songModel.sad = true
        songModel.key = fsKey.id
        fsKey.set(songModel)

        fsKey = firebaseStore.collection(FirebaseEndPoints.songs).document()
        songModel = FSSongsModel()
        songModel.songName = "Aye Musht-e-Khaak Ost Slowed"
        songModel.songURL =
            "https://firebasestorage.googleapis.com/v0/b/mood-based-music-application.appspot.com/o/Aye%20Musht-e-Khaak%20Ost%20Slowed.mp3?alt=media&token=0426f16f-31e9-4cb4-8753-c1a0db422478"
        songModel.english = false
        songModel.angry = false
        songModel.happy = false
        songModel.neutral = false
        songModel.sad = true
        songModel.key = fsKey.id
        fsKey.set(songModel)
    }

    private fun getDeviceId() {
        if (sharedPreference.getStringValue(StaticFields.deviceId) == "") {
            val androidId: String = Settings.Secure.getString(
                requireActivity().contentResolver,
                Settings.Secure.ANDROID_ID
            )
            Log.d("DeviceId", androidId)
            sharedPreference.saveStringValue(StaticFields.deviceId, androidId)
        }
    }
}