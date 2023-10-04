package com.music.moodplay.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.music.moodplay.R
import com.music.moodplay.databinding.FragmentFavoriteSongsBinding
import com.music.moodplay.models.localModel.SongsModel
import com.music.moodplay.presentation.adapters.RowSongsListAdapter
import com.music.moodplay.presentation.utils.StaticFields

class FavoriteSongsFragment : BaseFragment() {

    private lateinit var binding: FragmentFavoriteSongsBinding
    private var favoriteSongStringList: ArrayList<String>? = null
    private var favoriteSongModelList: ArrayList<SongsModel>? = null
    private lateinit var rowSongsListAdapter: RowSongsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteSongsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        initRecyclerView()
        getData()
    }

    private fun initClicks() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerView() {

        rowSongsListAdapter = RowSongsListAdapter(object : RowSongsListAdapter.OnItemClicked {
            override fun clickOnItem(position: Int) {
                val bundle = bundleOf()
                bundle.putString(
                    StaticFields.songModelKey,
                    StaticFields.modelToString(favoriteSongModelList?.get(position)!!)
                )
                bundle.putBoolean(StaticFields.isFavourite, true)
                bundle.putStringArrayList(StaticFields.favouriteSongListKey, favoriteSongStringList)
                bundle.putInt(StaticFields.songPositionKey, position)
                findNavController().navigate(
                    R.id.action_favoriteSongsFragment_to_playingSongFragment,
                    bundle
                )
            }
        })
        binding.songList.layoutManager = LinearLayoutManager(requireContext())
        binding.songList.adapter = rowSongsListAdapter
    }

    private fun getData() {
        favoriteSongStringList =
            arguments?.getStringArrayList(StaticFields.favouriteSongListKey)
        favoriteSongModelList = StaticFields.songsList

        favoriteSongModelList?.let {
            rowSongsListAdapter.setData(it)
        }

        binding.countTxt.text = favoriteSongStringList?.size.toString()
    }

}