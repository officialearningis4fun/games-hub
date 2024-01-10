package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.databinding.FragmentPlayCardsBinding
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Resource
import com.learningis4fun.gameshub.feature_crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCardScrollInfo
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation.play.cards.PlayCardsViewModel
import com.learningis4fun.gameshub.feature_crusade_cards.feature_start.CardsStartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@AndroidEntryPoint
class PlayCardsFragment : Fragment(R.layout.fragment_play_cards),
    UserCardsAdapter.UserCardsAdapterClick {

    private var binding: FragmentPlayCardsBinding? = null
    val playCardsViewModel: PlayCardsViewModel by viewModels()
    lateinit var cardsStartViewModel: CardsStartViewModel
    private var backPressedCallback: OnBackPressedCallback? = null

    @Inject
    lateinit var userCardsAdapter: UserCardsAdapter

    @Inject
    lateinit var playersScreenInfoAdapter: PlayersScreenInfoAdapter

    @Inject
    @CrusadeCardsDataModule.ProfileGlideInstance
    lateinit var profileGlide: RequestManager

    @Inject
    @CrusadeCardsDataModule.CardGlideInstance
    lateinit var cardGlide: RequestManager

    @Inject
    @CrusadeCardsDataModule.DefaultGlideInstance
    lateinit var defaultGlide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayCardsBinding.bind(view)

        cardsStartViewModel = ViewModelProvider(requireActivity()).get(CardsStartViewModel::class.java)

        binding?.dustbinCards?.setOnClickListener {
            playCardsViewModel.onDustbinClick()
        }

        binding?.toolbar?.setNavigationOnClickListener {
            cardsStartViewModel._ongoingGameIsActive.value = true
            playCardsViewModel.onCloseNavigationIconClicked()
        }

        setupRecyclerViews()
        registerObservers()
        registerEventListeners()
        registerViewClickListeners()

        userCardsAdapter.setListener(this)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cardsStartViewModel._ongoingGameIsActive.value = true
                if (backPressedCallback?.isEnabled == true) {
                    backPressedCallback?.remove()
                }
                playCardsViewModel.onCloseNavigationIconClicked()
            }
        }
        backPressedCallback?.let {
            requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback as OnBackPressedCallback)
        }
    }

    private fun setupUsersLandscapeRecyclerView() {
        binding?.players?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playersScreenInfoAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupUsersPortraitRecyclerView() {
        binding?.players?.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = playersScreenInfoAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupRecyclerViews() {
        // users
        binding?.userCards?.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = userCardsAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setupUsersLandscapeRecyclerView()
        } else {
            setupUsersPortraitRecyclerView()
        }

    }

    private fun toggleCurrentTypeView(showType: Pair<Boolean, String>) {
        if (!showType.first) {
            hideCurrentCardTypeView()
        } else {
            showCurrentCardTypeView(showType.second)
        }
    }

    private fun toggleChooseCardTypeView(isShow: Boolean) {
        if (isShow) {
            showChangeCardTypesView()
        } else {
            hideChangeCardTypesView()
        }
    }

    private fun toggleDefineJokerActionView(isShow: Boolean) {
        if (isShow) {
            showDefineJokerActionsView()
        } else {
            hideDefineJokerActionsView()
        }
    }

    private fun toggleAllowOthersPlayView(isShow: Boolean) {
        if (isShow) {
            showAllowOthersPlayView()
        } else {
            hideAllowOthersPlayView()
        }
    }

    private fun loadLastThreePlayedCards(topCards: List<UserCard>) {
        var topCardImage = ""
        var secondTopCardImage = ""
        var thirdTopCardImage = ""

        when (topCards.size) {
            1 -> {
                topCardImage = topCards.first().image
            }
            2 -> {
                topCardImage = topCards.first().image
                secondTopCardImage = topCards.last().image
            }
            3 -> {
                topCardImage = topCards.first().image
                secondTopCardImage = topCards[1].image
                thirdTopCardImage = topCards.last().image
            }
            else -> Unit
        }
        loadImagesIntoPlayedCardsImageViews(
            topImage = topCardImage,
            secondTopImage = secondTopCardImage,
            thirdTopImage = thirdTopCardImage
        )
    }

    private fun submitListToAdapter(userCardsAndScrollInfo: Pair<List<UserCard>, UserCardScrollInfo>) {

        userCardsAdapter.submitList(userCardsAndScrollInfo.first)
        /*Toast.makeText(requireContext(),
            "${userCardsAndScrollInfo.second.timeDifference}",
            Toast.LENGTH_SHORT).show()*/

        if (userCardsAndScrollInfo.second.shouldScroll) {
            //binding?.userCards?.smoothScrollToPosition(userCardsAndScrollInfo.second.scrollPosition)
        }
    }

    private fun registerObservers() {
/*
        playCardsViewModel.testData.observe(viewLifecycleOwner){
            binding?.cardsTestData?.text = it
        }*/

        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED){
                playCardsViewModel.playersScreenInfo.collectLatest {
                    playersScreenInfoAdapter.submitList(it)
                }
            }
        }

        playCardsViewModel.playCardsUIState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> TODO()
                is Resource.Loading -> {
                    binding?.rootPlayCards?.isVisible = false
                }
                is Resource.Success -> {
                    when (val state = resource.data) {
                        PlayCardsViewModel.PlayCardsUIStates.Loading -> TODO()
                        PlayCardsViewModel.PlayCardsUIStates.WinnerScreen -> TODO()
                        is PlayCardsViewModel.PlayCardsUIStates.PlayingScreen -> {
                            loadLastThreePlayedCards(state.lastThreePlayedCards)
                            submitListToAdapter(state.userCards)
                            showMessages(
                                message = state.messages.message,
                                shouldAnimate = state.messages.animate
                            )
                            binding?.rootPlayCards?.isVisible = true
                        }

                        null -> TODO()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED){
                playCardsViewModel.gameActions.collect { action ->
                    val state = action.gameActionEvents
                    toggleCurrentTypeView(action.showCurrentCardType)
                    toggleAllowOthersPlayView(isShow = action.allowOthersPlay)
                    Log.d("allowOthersPlay", "ShowAllowOthersPlay: ${action.allowOthersPlay}")
                    Log.d("allowOthersPlay", "state: $state")
                    Log.d("allowOthersPlay", "showChooseCardTypeScreen: ${action.showCurrentCardType}")
                    when(state){
                        PlayCardsViewModel.GameActionEvents.ChooseCardType -> {
                            toggleDefineJokerActionView(isShow = false)
                            toggleChooseCardTypeView(isShow = true)
                        }
                        is PlayCardsViewModel.GameActionEvents.DefaultAction -> {
                            toggleDefineJokerActionView(isShow = false)
                            toggleChooseCardTypeView(isShow = false)
                        }
                        is PlayCardsViewModel.GameActionEvents.DefineJokerAction -> {
                            toggleChooseCardTypeView(isShow = false)
                            toggleDefineJokerActionView(isShow = true)
                        }
                    }
                }
            }
        }

        playCardsViewModel.gameDuration.observe(viewLifecycleOwner) {
            binding?.gameDuration?.text = it
        }

        lifecycleScope.launchWhenStarted {
            playCardsViewModel.playCardsEvents.collect { event ->
                when (event) {
                    is PlayCardsViewModel.PlayCardsScreenEvents.NavigateToWinScreen -> Unit
                    PlayCardsViewModel.PlayCardsScreenEvents.PopBackStack -> {
                        findNavController().popBackStack()
                    }
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowErrorMessage -> Unit
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowGeneralMessage -> Unit
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowWarningMessage -> Unit
                }
            }
        }
    }

    private fun loadImagesIntoPlayedCardsImageViews(
        topImage: String,
        secondTopImage: String,
        thirdTopImage: String
    ) {
        val defaultBaseCard = R.drawable.card_base
        val topImageResource = try {
            topImage.toInt()
        } catch (exception: Exception) {
            null
        }

        val secondTopImageResource = try {
            secondTopImage.toInt()
        } catch (exception: Exception) {
            null
        }

        val thirdTopImageResource = try {
            thirdTopImage.toInt()
        } catch (exception: Exception) {
            null
        }

        binding?.apply {
            if (topImageResource == null) {
                cardGlide.load(defaultBaseCard).into(topPlayedCard)
            } else {
                cardGlide.load(topImageResource).into(topPlayedCard)
            }

            if (secondTopImageResource == null) {
                cardGlide.load(defaultBaseCard).into(secondTopPlayedCard)
            } else {
                cardGlide.load(secondTopImageResource).into(secondTopPlayedCard)
            }

            if (thirdTopImageResource == null) {
                cardGlide.load(defaultBaseCard).into(thirdTopPlayedCard)
            } else {
                cardGlide.load(thirdTopImageResource).into(thirdTopPlayedCard)
            }
        }
    }

    private fun showMessages(message: String, shouldAnimate: Boolean = false) {

        binding?.apply {
            messages.text = message
        }
    }

    private fun showAllowOthersPlayView() {
        binding?.layoutIncludeAllowOthersPlay?.allowOthersPlay?.visibility = View.VISIBLE
    }

    private fun hideAllowOthersPlayView() {
        binding?.layoutIncludeAllowOthersPlay?.allowOthersPlay?.visibility = View.INVISIBLE
    }

    private fun showDefineJokerActionsView() {
        binding?.layoutIncludeDefineJokerAction?.defineJokerAction?.visibility = View.VISIBLE
    }

    private fun showChangeCardTypesView() {
        binding?.layoutIncludeCardTypes?.chooseCardType?.visibility = View.VISIBLE
    }

    private fun hideDefineJokerActionsView() {
        binding?.layoutIncludeDefineJokerAction?.defineJokerAction?.visibility = View.INVISIBLE
    }

    private fun hideChangeCardTypesView() {
        binding?.layoutIncludeCardTypes?.chooseCardType?.visibility = View.INVISIBLE
    }

    private fun showCurrentCardTypeView(type: String) {
        binding?.layoutViewCurrentCardType?.chooseCardType?.isVisible = true
        loadCurrentCardTypeIcons(type = type)
    }

    private fun hideCurrentCardTypeView() {
        binding?.layoutViewCurrentCardType?.chooseCardType?.isVisible = false
    }

    private fun loadCurrentCardTypeIcons(type: String) {

        val drawableImage = when (type) {
            Constants.CardTypes.DIAMOND -> {
                R.drawable.suit_diamond
            }
            Constants.CardTypes.HEART -> {
                R.drawable.suit_heart
            }
            Constants.CardTypes.SPADE -> {
                R.drawable.suit_spade
            }
            Constants.CardTypes.CLUBS -> {
                R.drawable.suit_club
            }
            else -> null

        }

        binding?.layoutViewCurrentCardType?.currentCardTypeIcon1?.let {
            defaultGlide.load(drawableImage).into(it)
        }
        binding?.layoutViewCurrentCardType?.currentCardTypeIcon2?.let {
            defaultGlide.load(drawableImage).into(it)
        }
        binding?.layoutViewCurrentCardType?.currentCardTypeIcon3?.let {
            defaultGlide.load(drawableImage).into(it)
        }
    }

    private fun registerViewClickListeners() {

        binding?.layoutIncludeAllowOthersPlay?.allowOthersPlay?.setOnClickListener {
            playCardsViewModel.onAllowOthersPlayBtnClick()
        }

        binding?.layoutIncludeCardTypes?.apply {

            // spade
            suitSpade.setOnClickListener {
                playCardsViewModel.onCardTypeClick(
                    Constants.CardTypes.SPADE
                )
            }

            // club
            suitClub.setOnClickListener {
                playCardsViewModel.onCardTypeClick(
                    Constants.CardTypes.CLUBS
                )
            }

            // heart
            suitHeart.setOnClickListener {
                playCardsViewModel.onCardTypeClick(
                    Constants.CardTypes.HEART
                )
            }

            // diamond
            suitDiamond.setOnClickListener {
                playCardsViewModel.onCardTypeClick(
                    Constants.CardTypes.DIAMOND
                )
            }
        }

        binding?.layoutIncludeDefineJokerAction?.apply {

            hitJokerAction.setOnClickListener {
                playCardsViewModel.onJokerActionClick(
                    JokerActions.Hit
                )
            }

            changeTypeJokerAction.setOnClickListener {
                playCardsViewModel.onJokerActionClick(
                    JokerActions.ChangeType
                )
            }
        }
    }

    fun registerEventListeners() {
        lifecycleScope.launchWhenStarted {
            playCardsViewModel.playCardsEvents.collect { event ->
                when (event) {
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowErrorMessage -> {
                        showMessages(message = event.message)
                    }
                    is PlayCardsViewModel.PlayCardsScreenEvents.NavigateToWinScreen -> {
                        showMessages("Someone just won")
                    }
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowGeneralMessage -> {
                        showMessages(event.message)
                    }
                    is PlayCardsViewModel.PlayCardsScreenEvents.ShowWarningMessage -> {
                        showMessages(event.message)
                    }
                    PlayCardsViewModel.PlayCardsScreenEvents.PopBackStack -> {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCardClicked(userCard: UserCard) {
        playCardsViewModel.onUserCardClick(userCard = userCard)
        //Toast.makeText(requireContext(), "$userCard is clicked", Toast.LENGTH_SHORT).show()
    }


}