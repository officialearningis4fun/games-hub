package com.learningis4fun.gameshub.feature_crusade_cards.feature_start

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.databinding.ItemCompletedGameBinding
import javax.inject.Inject

class CompletedGamesAdapter @Inject constructor(
    val glide: RequestManager
) :
    ListAdapter<Game, CompletedGamesAdapter.CompletedGamesViewHolder>(
        DIFF_CALLBACK
    ) {

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Game> =
            object : DiffUtil.ItemCallback<Game>() {
                override fun areItemsTheSame(
                    oldItem: Game,
                    newItem: Game
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Game,
                    newItem: Game
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedGamesViewHolder {
        val binding =
            ItemCompletedGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CompletedGamesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletedGamesViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class CompletedGamesViewHolder(private val binding: ItemCompletedGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val game = getItem(position)
            binding.apply {
                name.text = game.name
                message.text = game.completedGameMessage
            }

        }

    }

}