package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.databinding.ItemPlayerScreenInfoBinding
import com.learningis4fun.gameshub.feature_crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.PlayerScreenInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class PlayersScreenInfoAdapter @Inject constructor(
    @CrusadeCardsDataModule.ProfileGlideInstance val glide: RequestManager
) : ListAdapter<PlayerScreenInfo, PlayersScreenInfoAdapter.PlayersScreenInfoViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<PlayerScreenInfo> =
            object : DiffUtil.ItemCallback<PlayerScreenInfo>() {
                override fun areItemsTheSame(
                    oldItem: PlayerScreenInfo,
                    newItem: PlayerScreenInfo
                ): Boolean {
                    return oldItem.playerId == newItem.playerId
                }

                override fun areContentsTheSame(
                    oldItem: PlayerScreenInfo,
                    newItem: PlayerScreenInfo
                ): Boolean {
                    return oldItem.hashCode() == newItem.hashCode()
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayersScreenInfoViewHolder {
        val binding =
            ItemPlayerScreenInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayersScreenInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayersScreenInfoViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PlayersScreenInfoViewHolder(private val binding: ItemPlayerScreenInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val playerScreenInfo = getItem(position)
            binding.apply {
                playerName.text = playerScreenInfo.playerName
                numberOfCards.text = playerScreenInfo.numOfCardsLeft
                rootItemPlayerScreenInfo.strokeWidth = if (playerScreenInfo.isCurrentlyPlaying) {
                    R.dimen.item_player_screen_info_root_stroke_width
                } else {
                    R.dimen.item_player_screen_info_root_stroke_zero_width
                }
                val image = playerScreenInfo.imageUrl ?: playerScreenInfo.defaultAvatar
                glide.load(image).into(profile)
            }

        }

    }


}