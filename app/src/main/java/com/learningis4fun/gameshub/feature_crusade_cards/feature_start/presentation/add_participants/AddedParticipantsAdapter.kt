package com.learningis4fun.gameshub.feature_crusade_cards.feature_start.presentation.add_participants

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.databinding.ItemAddedPlayersBinding
import javax.inject.Inject

class AddedParticipantsAdapter @Inject constructor(
    val glide: RequestManager
) :
    ListAdapter<Player, AddedParticipantsAdapter.AddedParticipantsViewHolder>(
        DIFF_CALLBACK
    ) {

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Player> =
            object : DiffUtil.ItemCallback<Player>() {
                override fun areItemsTheSame(
                    oldItem: Player,
                    newItem: Player
                ): Boolean {
                    return oldItem.uniqueId == newItem.uniqueId
                }

                override fun areContentsTheSame(
                    oldItem: Player,
                    newItem: Player
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedParticipantsViewHolder {
        val binding =
            ItemAddedPlayersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddedParticipantsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddedParticipantsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class AddedParticipantsViewHolder(private val binding: ItemAddedPlayersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val player = getItem(position)
            binding.apply {
                name.text = player.profileName
                glide.load(player.imageUrl).into(image)
            }

        }

    }

}