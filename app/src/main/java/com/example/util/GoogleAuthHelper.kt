package com.example.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed class GoogleSignInResult {
    data class Success(val session: UserSession) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

object GoogleAuthHelper {

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = "273961692928-647a4dddff664e4cb1f14de00182805d.apps.googleusercontent.com"
    ): GoogleSignInResult {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(
                    UserSession(
                        email = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "Google User",
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        idToken = googleIdTokenCredential.idToken
                    )
                )
            } else {
                GoogleSignInResult.Error("Unexpected credential type returned.")
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Error(e.errorMessage?.toString() ?: e.message ?: "Google Sign-In unavailable")
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Google Sign-In failed")
        }
    }
}
