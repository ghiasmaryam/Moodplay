package com.music.moodplay.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.music.moodplay.databinding.RowSongListBinding
import com.music.moodplay.models.localModel.SongsModel

class RowSongsListAdapter(private val listener: OnItemClicked) : RecyclerView.Adapter<RowSongsListAdapter.Holder>() {

    private var songsModelList: ArrayList<SongsModel> = ArrayList()

    fun setData(songsModelList: ArrayList<SongsModel>) {
        this.songsModelList = songsModelList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RowSongListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.initUI(songsModelList[position], position, listener)
    }

    override fun getItemCount(): Int = songsModelList.size


    class Holder(private val binding: RowSongListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun initUI(songsModel: SongsModel, position: Int, listener: OnItemClicked) {
            binding.songName.text = songsModel.songName
            binding.root.setOnClickListener {
                listener.clickOnItem(position)
            }
        }

    }

    interface OnItemClicked {
        fun clickOnItem(position: Int)
    }
}