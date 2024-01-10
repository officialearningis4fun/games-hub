package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.feature_crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.databinding.ItemCardBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserCardsAdapter @Inject constructor(
   @CrusadeCardsDataModule.CardGlideInstance val glide: RequestManager
) :
    ListAdapter<UserCard, UserCardsAdapter.UserCardsViewHolder>(
        DIFF_CALLBACK
    ) {

    private var listener : UserCardsAdapterClick? = null

    fun setListener(listener : UserCardsAdapterClick){
        this.listener = listener
    }

    interface UserCardsAdapterClick{
        fun onCardClicked(userCard: UserCard)
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<UserCard> =
            object : DiffUtil.ItemCallback<UserCard>() {
                override fun areItemsTheSame(
                    oldItem: UserCard,
                    newItem: UserCard
                ): Boolean {
                    return oldItem.gameCardId == newItem.gameCardId
                }

                override fun areContentsTheSame(
                    oldItem: UserCard,
                    newItem: UserCard
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCardsViewHolder {
        val binding =
            ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserCardsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserCardsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserCardsViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardImage.setOnClickListener {
            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION || listener != null){
                listener?.onCardClicked(getItem(bindingAdapterPosition))
            }

            }
        }

        fun bind(position: Int) {
            val userCard = getItem(position)
            binding.apply {

                val imageDrawable = try {
                    userCard.image.toInt()
                } catch (e: Exception) {
                    null
                }
                imageDrawable?.let {
                    // use glide to set image
                    glide.load(it).into(cardImage)
                } ?: glide.load(R.drawable.back_aqua).into(cardImage)
            }

        }

    }

}