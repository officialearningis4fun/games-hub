package com.learningis4fun.gameshub.data.data_source.remote

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import coil3.toCoilUri
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.util.LogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    @ApplicationContext private val context: Context,
) {

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val auth = Firebase.auth
    private val signedInUserFlow = MutableStateFlow<User?>(auth.currentUser?.toUser())
    val signedInUser = signedInUserFlow.asStateFlow()

    private val googleAuthStagesState =
        MutableStateFlow<GoogleAuthStages>(GoogleAuthStages.NotStarted)
    val googleAuthStages = googleAuthStagesState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            Log.d(LogTags.GOOGLE_AUTH_UI_CLIENT, "user: ${firebaseAuth.currentUser?.toUser()}")
            signedInUserFlow.update { firebaseAuth.currentUser?.toUser() }
        }
    }

    fun googleAuthStageChanged(stage: GoogleAuthStages) {
        googleAuthStagesState.update { stage }
    }


    suspend fun signOut(): Response<Unit> {
        return try {
            val clearCredentialRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearCredentialRequest)
            auth.signOut()
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unexpected error")
        }
    }

    fun getSignedInUser(): User? {
        return auth.currentUser?.toUser()
    }

    private fun FirebaseUser.toUser() = User(
        id = uid,
        email = email ?: "",
        name = displayName,
        photoUrl = photoUrl?.toCoilUri()?.toString()
    )

    suspend fun requestUserOrSignInWithGoogle(context: Context): Response<User> {
        return try {
            Log.d(LogTags.GOOGLE_AUTH_UI_CLIENT, "requesting user")
            if (auth.currentUser != null) {
                if (auth.currentUser!!.isAnonymous) {
                    Log.d(LogTags.GOOGLE_AUTH_UI_CLIENT, "found anonymous user")
                    val user = signInWithGoogle(context).getDataOrThrowException()
                    Log.d(LogTags.GOOGLE_AUTH_UI_CLIENT, "signed in user: $user")

                    Response.Success(user)
                } else {
                    Response.Success(auth.currentUser!!.toUser())
                }
            } else {
                signInWithGoogle(context)
            }
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unexpected error")
        }
    }

    suspend fun signInWithGoogle(context: Context): Response<User> {
        return try {
            // Instantiate a Google sign-in request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.google_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            // Create the Credential Manager request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)

            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Create Google ID Token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Sign in to Firebase with using the token
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } else {
                Log.w("GoogleAuthClient", "Credential is not of type Google ID!")
                throw Exception("Failed to signInWithGoogle")
            }
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuthClient", "${e.message}")
            e.printStackTrace()
            Response.Error("Failed to signInWithGoogle")
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): Response<User> {

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser =
                auth.signInWithCredential(credential).await().user?.toUser()
                    ?: throw Exception("Failed to get the signedIn user")
            Response.Success(firebaseUser)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message ?: "Unexpected error")
        }

    }


}

sealed interface GoogleAuthStages {
    object NotStarted : GoogleAuthStages
    object Started : GoogleAuthStages
    object Complete : GoogleAuthStages
}