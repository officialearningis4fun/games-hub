package com.learningis4fun.gameshub.feature_crusade_cards.feature_profile.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.feature_crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.hideKeyBoard
import com.learningis4fun.gameshub.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    var binding: FragmentProfileBinding? = null
    val profileViewModel: ProfileViewModel by viewModels()

    @Inject
    @CrusadeCardsDataModule.ProfileGlideInstance lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        registerObservers()
        registerEventListeners()

        binding?.editProfileNameBtn?.setOnClickListener {
            profileViewModel.onEditProfileNameBtnClick()
        }

        binding?.signOutBtn?.setOnClickListener {
            profileViewModel.signOut()
            val action =
                ProfileFragmentDirections.actionProfileFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        binding?.profileToolbar?.setNavigationOnClickListener {
            profileViewModel.onBackNavigationClick()
        }
    }

    private fun registerObservers() {
        profileViewModel.userProfileInfo().observe(viewLifecycleOwner) { player ->
            binding?.name?.text = player.profileName
            binding?.userEmailOrPhone?.text = when {
                player.email.isEmpty() -> player.phone
                else -> player.email
            }
            binding?.icon?.let { glide.load(player.imageUrl).into(it) }
            binding?.signOutBtn?.isVisible = player.uniqueId.isNotEmpty()

        }
    }

    private fun registerEventListeners() {
        lifecycleScope.launchWhenStarted {
            profileViewModel.profileScreenEvents.collect { event ->
                when (event) {
                    ProfileViewModel.ProfileScreenEvents.PopBackStack -> {
                        hideKeyBoard(requireActivity(), requireView())
                        findNavController().popBackStack()
                    }
                    ProfileViewModel.ProfileScreenEvents.NavigateToSignUpScreen -> {
                        /*val action = ProfileFragmentDirections.actionProfileFragmentToSignUpFragment()
                        findNavController().navigate(action)*/
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyBoard(requireActivity(), requireView())
        binding = null
    }
}