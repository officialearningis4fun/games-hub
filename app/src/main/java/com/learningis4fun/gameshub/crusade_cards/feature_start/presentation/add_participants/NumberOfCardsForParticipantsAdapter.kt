package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.add_participants

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.CardNumInfo
import com.learningis4fun.gameshub.databinding.ItemNumberOfCardsForPlayersBinding
import javax.inject.Inject

class NumberOfCardsForParticipantsAdapter @Inject constructor() :
    ListAdapter<CardNumInfo, NumberOfCardsForParticipantsAdapter.NumberOfCardsForParticipantsViewHolder>(
        DIFF_CALLBACK
    ) {

    private var listener: NumberOfCardsToAssignCLick? = null

    fun setListener(listener: NumberOfCardsToAssignCLick) {
        this.listener = listener
    }

    interface NumberOfCardsToAssignCLick {
        fun onNumOfCardsOptionClick(cardNumInfo: CardNumInfo)
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<CardNumInfo> =
            object : DiffUtil.ItemCallback<CardNumInfo>() {
                override fun areItemsTheSame(
                    oldItem: CardNumInfo,
                    newItem: CardNumInfo
                ): Boolean {
                    return oldItem.number == newItem.number
                }

                override fun areContentsTheSame(
                    oldItem: CardNumInfo,
                    newItem: CardNumInfo
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NumberOfCardsForParticipantsViewHolder {
        val binding =
            ItemNumberOfCardsForPlayersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return NumberOfCardsForParticipantsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NumberOfCardsForParticipantsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class NumberOfCardsForParticipantsViewHolder(private val binding: ItemNumberOfCardsForPlayersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val cardNumInfo = getItem(position)
            binding.apply {
                image.borderColor = if (cardNumInfo.selected) {
                        binding.root.context.resources.getColor(R.color.orange_500)
                    } else {
                        binding.root.context.resources.getColor(R.color.black)
                    }

                numOfCards.text = cardNumInfo.number.toString()
            }

        }

        init {
            binding.image.setOnClickListener {
                listener?.let {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        it.onNumOfCardsOptionClick(getItem(position))
                    }
                }
            }
        }

    }

}