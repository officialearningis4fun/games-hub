package com.learningis4fun.gameshub.feature_signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

const val REQUEST_CODE_SIGN_IN = 200

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    var binding: FragmentSignUpBinding? = null
    val signUpViewModel: SignUpViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)

        registerObservers()
        registerEventListeners()

        binding?.googleSignInBtn?.setOnClickListener {
            /*val googleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
            googleSignInClient.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
            }*/
        }
    }

    fun registerEventListeners() {
        lifecycleScope.launchWhenStarted {
            signUpViewModel.signUpScreenEvents.collect { event ->
                when (event) {
                    SignUpViewModel.SignUpScreenEvents.NavigateToStartScreen -> {
                        val action =
                            SignUpFragmentDirections.actionSignUpFragmentToCardsStartFragment2()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }


    fun registerObservers() {
        lifecycleScope.launchWhenStarted {
            signUpViewModel.signUpScreenStates().collectLatest { state ->
                when (state) {
                    is SignUpViewModel.SignUpScreenStates.Error -> {
                        binding?.helperText?.text = state.message
                        binding?.googleSignInBtn?.isVisible = true
                        binding?.helperText?.isVisible = true
                    }
                    SignUpViewModel.SignUpScreenStates.Loading -> {
                        binding?.googleSignInBtn?.isVisible = false
                        binding?.helperText?.isVisible = false
                    }
                    is SignUpViewModel.SignUpScreenStates.NotSignedIn -> {
                        binding?.helperText?.text = state.message
                        binding?.googleSignInBtn?.isVisible = true
                        binding?.helperText?.isVisible = true
                    }
                    SignUpViewModel.SignUpScreenStates.SignedIn -> Unit
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                signUpViewModel.googleAuthForFirebase(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}