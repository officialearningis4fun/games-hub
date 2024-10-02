package com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.enterProfile

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.hideKeyBoard
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.showKeyboard
import com.learningis4fun.gameshub.databinding.FragmentEnterProfileNameBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EnterProfileNameFragment : Fragment(R.layout.fragment_enter_profile_name) {

    var binding: FragmentEnterProfileNameBinding? = null
    val enterProfileNameViewModel : EnterProfileNameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterProfileNameBinding.bind(view)
        registerObservers()
        registerEventListeners()

        binding?.nameLayout?.editText?.apply {
            showKeyboard(this)
            requestFocus()
            setSelectAllOnFocus(true)
            selectionEnd
        }

        binding?.save?.setOnClickListener {
            enterProfileNameViewModel.onNameSaveBtnClick()
        }

        binding?.nameLayout?.editText?.addTextChangedListener {
            enterProfileNameViewModel.onNameChanged(it.toString())
        }
    }

    private fun registerObservers() {
        enterProfileNameViewModel.enableSaveProfileBtn().observe(viewLifecycleOwner){
            binding?.save?.isEnabled = it
        }

    }

    private fun registerEventListeners() {
        lifecycleScope.launchWhenStarted {
          /*val name =  enterProfileNameViewModel.getOldProfileName()
            binding?.nameLayout?.editText?.setText(name)
            enterProfileNameViewModel.signUpScreenEvents.collect { event ->
                when(event){
                    EnterProfileNameViewModel.SignUpScreenEvents.PopBackStack -> {
                        hideKeyBoard(requireActivity(), requireView())
                        findNavController().popBackStack()
                    }
                }
            }*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyBoard(requireActivity(), requireView())
        binding = null
    }
}