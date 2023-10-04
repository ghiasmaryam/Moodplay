package com.music.moodplay.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.music.moodplay.R
import com.music.moodplay.models.firebaseModels.FSPulseRatesModel
import com.music.moodplay.presentation.utils.StaticFields
import com.music.moodplay.databinding.FragmentDisplaySongsBinding
import com.music.moodplay.models.firebaseModels.FSSongsModel
import com.music.moodplay.models.localModel.SongsModel
import com.music.moodplay.presentation.activities.MainActivity
import com.music.moodplay.presentation.adapters.RowSongsListAdapter
import com.music.moodplay.presentation.utils.constants.FirebaseEndPoints
import com.music.moodplay.presentation.utils.dialogs.CustomDialog

class DisplaySongsFragment : BaseFragment() {

    private lateinit var binding: FragmentDisplaySongsBinding
    private var pulseRate: Int? = null
    private var currentMode: FSPulseRatesModel? = null
    private var englishSongsList: ArrayList<SongsModel> = ArrayList()
    private var hindiSongsList: ArrayList<SongsModel> = ArrayList()
    private lateinit var rowSongsListAdapter: RowSongsListAdapter
    private var isEnglishSongs: Boolean = true
    private var favoriteSongStringList: ArrayList<String> = ArrayList()
    private var favoriteSongsModelList: ArrayList<SongsModel> = ArrayList()
    private lateinit var deviceId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDisplaySongsBinding.inflate(layoutInflater)
        deviceId = sharedPreference.getStringValue(StaticFields.deviceId).toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pulseRate = arguments?.getInt(StaticFields.pulseRateKey)
        currentMode = arguments?.getString(StaticFields.fsPulseRatesKey)?.let {
            StaticFields.stringToModel(it, FSPulseRatesModel())
        }
        initClicks()
        setData()
        loadRV()
    }

    private fun initClicks() {
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_displaySongs_to_thumbPrintFragment)
        }
        binding.favoriteLayout.setOnClickListener {
            if (favoriteSongStringList.size == 0) {
                CustomDialog.singleDialogWithoutListener(
                    requireContext(),
                    "No Song is added in Favorite YET"
                )
                return@setOnClickListener
            }
            val bundle = bundleOf()
            bundle.putStringArrayList(StaticFields.favouriteSongListKey, favoriteSongStringList)
            StaticFields.songsList = favoriteSongsModelList
            findNavController().navigate(R.id.action_displaySongs_to_favoriteSongsFragment, bundle)
        }
        binding.englishBtn.setOnClickListener {
            englishBtnClicked()
        }
        binding.hindiBtn.setOnClickListener {
            hindBtnClicked()
        }
    }

    private fun setData() {
        binding.pulseRateTxt.text = pulseRate.toString()
        currentMode?.let {
            binding.currentMoodTxt.text = it.moodType
            when (it.moodType) {
                FirebaseEndPoints.happyMood -> {
                    binding.moodImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.happy))
                }
                FirebaseEndPoints.sadMood -> {
                    binding.moodImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.sad))
                }
                FirebaseEndPoints.neutralMood -> {
                    binding.moodImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.neutral))
                }
                FirebaseEndPoints.angryMood -> {
                    binding.moodImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.angry))
                }
            }
        }
        getSongFormFirebase()
    }

    private fun englishBtnClicked() {
        isEnglishSongs = true
        binding.selectedSongTxt.text = "English Songs playlist suggested"
        rowSongsListAdapter.setData(englishSongsList)
    }

    private fun hindBtnClicked() {
        isEnglishSongs = false
        binding.selectedSongTxt.text = "Hindi Songs playlist suggested"
        rowSongsListAdapter.setData(hindiSongsList)
    }

    private fun getSongFormFirebase() {
        loader.startLoading()
        englishSongsList = ArrayList()
        hindiSongsList = ArrayList()
        favoriteSongStringList = ArrayList()
        favoriteSongsModelList = ArrayList()
        val deviceId: String? = sharedPreference.getStringValue(StaticFields.deviceId)
        deviceId?.let { id ->
            firebaseStore.collection(FirebaseEndPoints.favorite)
                .document(id).get()
                .addOnSuccessListener { ids ->
//                    ids.forEach {
//                        favoriteSongStringList.add(it.toObject(String::class.java))
//                    }
                    ids.data?.entries?.forEach {
                        favoriteSongStringList.add(it.value.toString())
                    }

                    firebaseStore.collection(FirebaseEndPoints.songs).get()
                        .addOnSuccessListener { songs ->
                            songs.forEach { item ->
                                val fsSongsModel = item.toObject(FSSongsModel::class.java)
                                if (favoriteSongStringList.contains(fsSongsModel.key)) {
                                    favoriteSongsModelList.add(
                                        SongsModel(
                                            songName = fsSongsModel.songName,
                                            songURL = fsSongsModel.songURL,
                                            favoriteCount = fsSongsModel.favoriteCount,
                                            key = fsSongsModel.key
                                        )
                                    )
                                }
                                currentMode?.let {
                                    when (it.moodType) {
                                        FirebaseEndPoints.sadMood -> {
                                            if (fsSongsModel.english && fsSongsModel.sad) {
                                                englishSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            } else if (!fsSongsModel.english && fsSongsModel.sad) {
                                                hindiSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            }
                                        }
                                        FirebaseEndPoints.happyMood -> {
                                            if (fsSongsModel.english && fsSongsModel.happy) {
                                                englishSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            } else if (!fsSongsModel.english && fsSongsModel.happy) {
                                                hindiSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            }
                                        }
                                        FirebaseEndPoints.angryMood -> {
                                            if (fsSongsModel.english && fsSongsModel.angry) {
                                                englishSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            } else if (!fsSongsModel.english && fsSongsModel.angry) {
                                                hindiSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            }
                                        }
                                        FirebaseEndPoints.neutralMood -> {
                                            if (fsSongsModel.english && fsSongsModel.neutral) {
                                                englishSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            } else if (!fsSongsModel.english && fsSongsModel.neutral) {
                                                hindiSongsList.add(
                                                    SongsModel(
                                                        songName = fsSongsModel.songName,
                                                        songURL = fsSongsModel.songURL,
                                                        favoriteCount = fsSongsModel.favoriteCount,
                                                        key = fsSongsModel.key
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            englishSongsList.sortBy { item ->
                                item.songName
                            }
                            hindiSongsList.sortBy { item ->
                                item.songName
                            }
//                    hindiSongsList.add(item.toObject(SongsModel::class.java))

                            if ((activity as MainActivity).getIsEnglishFromService() == false)
                                hindBtnClicked()
                            else englishBtnClicked()
                            loader.isDismiss()
                        }

                        .addOnFailureListener {
                            loader.isDismiss()
                            Log.d("FirebaseError-getsongfromfirebase", "Error ${it.toString()}")
                        }
                }
        }
    }

    private fun loadRV() {
        rowSongsListAdapter = RowSongsListAdapter(object : RowSongsListAdapter.OnItemClicked {
            override fun clickOnItem(position: Int) {
                val bundle = bundleOf()
                if (isEnglishSongs) {
                    bundle.putString(
                        StaticFields.songModelKey,
                        StaticFields.modelToString(englishSongsList[position])
                    )
                    StaticFields.songsList = englishSongsList
//                    bundle.putString(
//                        StaticFields.songModelListKey,
//                        StaticFields.modelToString(englishSongsList)
//                    )
                    bundle.putBoolean(StaticFields.isEnglish, true)
                } else {
                    bundle.putString(
                        StaticFields.songModelKey,
                        StaticFields.modelToString(hindiSongsList[position])
                    )
//                    bundle.putSerializable(
//                        StaticFields.songModelListKey,
//                        StaticFields.modelToString(hindiSongsList)
//                    )
                    StaticFields.songsList = hindiSongsList
                    bundle.putBoolean(StaticFields.isEnglish, false)
                }
                bundle.putBoolean(StaticFields.isFavourite, false)
                bundle.putStringArrayList(StaticFields.favouriteSongListKey, favoriteSongStringList)
                bundle.putInt(StaticFields.songPositionKey, position)
                findNavController().navigate(
                    R.id.action_displaySongs_to_playingSongFragment,
                    bundle
                )
            }
        })
        binding.songList.layoutManager = LinearLayoutManager(requireContext())
        binding.songList.adapter = rowSongsListAdapter
    }

}