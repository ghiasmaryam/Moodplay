package com.music.moodplay.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.music.moodplay.R
import com.music.moodplay.databinding.FragmentPlayingSongBinding
import com.music.moodplay.models.localModel.SongsModel
import com.music.moodplay.presentation.activities.MainActivity
import com.music.moodplay.presentation.utils.StaticFields
import com.music.moodplay.presentation.utils.constants.FirebaseEndPoints
import com.music.moodplay.presentation.utils.mediaInterfaces.MediaChangeListener
import com.music.moodplay.presentation.utils.mediaInterfaces.MediaLiveData

class PlayingSongFragment : BaseFragment(),
    MediaChangeListener {

    private lateinit var binding: FragmentPlayingSongBinding
    private var songsModel: SongsModel? = null
    private var songsList: List<SongsModel>? = null
    private var position: Int = 0
    private var favoriteSongList: ArrayList<String> = ArrayList()
    private lateinit var deviceId: String
    private var isFavorite: Boolean = false
    private var isEnglish: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayingSongBinding.inflate(layoutInflater)
        deviceId = sharedPreference.getStringValue(StaticFields.deviceId) ?: ""
        loader.startLoading()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songsModel = arguments?.getString(StaticFields.songModelKey)?.let {
            StaticFields.stringToModel(
                it, SongsModel()
            )
        }
        songsList = StaticFields.songsList
        favoriteSongList =
            arguments?.getStringArrayList(StaticFields.favouriteSongListKey) as ArrayList<String>

        position = arguments?.getInt(StaticFields.songPositionKey) ?: 0
        initClicks()
        initView()
        this.isFavorite = arguments?.getBoolean(StaticFields.isFavourite, false)!!
        this.isEnglish = arguments?.getBoolean(StaticFields.isEnglish, false)!!

        songsList?.let {
            (activity as MainActivity).initService(position, isFavorite, isEnglish, this)
        }

        initLiveData()
    }

    private fun initLiveData() {
        MediaLiveData.pausePlayLiveData.observe(viewLifecycleOwner) {
            getPausePlay(it)
        }
        MediaLiveData.changeSongLiveData.observe(viewLifecycleOwner) {
            if(it < songsList!!.size)
                changeSong(it)
        }
    }

    private fun initView() {
        songsModel?.let {
            binding.songTitle.text = it.songName
            binding.favoriteCount.text = it.favoriteCount.toString()
            when (favoriteSongList.contains(it.key)) {
                true -> binding.favoriteImg.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
                false -> binding.favoriteImg.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }

        binding.previousSong.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (position == 0)
                    R.color.purple_500 else R.color.black
            )
        )
        binding.forwardsSong.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (position == songsList?.size!! - 1) (R.color.purple_500) else R.color.black
            )
        )
    }

    private fun initClicks() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.favoriteImg.setOnClickListener {
            when (favoriteSongList.contains(songsModel?.key)) {
                true -> {
                    binding.favoriteImg.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    songsModel!!.favoriteCount--
                    favoriteSongList.remove(songsModel?.key)
                    firebaseStore.collection(FirebaseEndPoints.favorite).document(deviceId)
                        .update(songsModel!!.key, FieldValue.delete())
                }
                false -> {
                    binding.favoriteImg.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    songsModel!!.favoriteCount++
                    val hashMap = HashMap<String, String>()
                    favoriteSongList.forEach { item ->
                        hashMap[item] = item
                    }
                    hashMap[songsModel!!.key] = songsModel!!.key
                    firebaseStore.collection(FirebaseEndPoints.favorite).document(deviceId).set(
                        hashMap
                    )
                    favoriteSongList.add(songsModel?.key!!)
                }
            }
            firebaseStore.collection(FirebaseEndPoints.songs).document(songsModel!!.key).update(
                FirebaseEndPoints.favoriteCount, songsModel!!.favoriteCount
            )

            binding.favoriteCount.text = songsModel?.favoriteCount.toString()
            StaticFields.songsList = songsList as ArrayList<SongsModel>?
        }
        binding.forwardsSong.setOnClickListener {
            if (position < (songsList?.size!! - 1)) {
                position++
                songsModel = songsList!![position]
                (activity as MainActivity).initService(position, isFavorite, isEnglish, this)
            }
            initView()
        }
        binding.previousSong.setOnClickListener {
            if (position > 0) {
                position--
                songsModel = songsList!![position]
                (activity as MainActivity).initService(position, isFavorite, isEnglish, this)
            }
            initView()
        }
        binding.playSong.setOnClickListener {
            (activity as MainActivity).pausePlaySong()
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    (activity as MainActivity).changeSongProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        binding.playList.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun changeSong(position: Int) {
        this.position = position
        songsModel = songsList!![position]
        initView()
    }

    override fun seekBarChange(progress: Int) {
        Log.d("Duration", progress.toString())
        binding.seekbar.progress = progress
        binding.songPosition.text = getTime(progress / 1000)
    }

    override fun getDuration(duration: Int) {
        loader.isDismiss()
        binding.songDuration.text = getTime(duration / 1000)
        binding.seekbar.max = duration
        binding.seekbar.progress = 0
    }

    private fun getTime(duration: Int): StringBuilder {
        val time: StringBuilder = StringBuilder()
        time.append(if ((duration / 60) >= 10) (duration / 60) else "0${(duration / 60)}")
        time.append(":")
        time.append(if ((duration % 60) >= 10) (duration % 60) else "0${(duration % 60)}")
        return time
    }

    private fun getPausePlay(isPause: Boolean) {
        if (isPause) {
            binding.playSong.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_baseline_play_arrow_24
                )
            )
        } else binding.playSong.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_baseline_pause_24
            )
        )
    }

}