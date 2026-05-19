package com.shopapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopapp.AppSession
import com.shopapp.data.model.User
import com.shopapp.data.repository.FirebaseAuthRepository
import com.shopapp.data.repository.FirestoreUserRepository
import com.shopapp.data.repository.UserRepository

class AuthViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>?>()
    val loginResult: LiveData<Result<User>?> = _loginResult

    private val _registerResult = MutableLiveData<Result<User>?>()
    val registerResult: LiveData<Result<User>?> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Login ─────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = Result.failure(Exception("Correo y contraseña requeridos"))
            return
        }
        _isLoading.value = true

        FirebaseAuthRepository.login(email, password) { result ->
            result.fold(
                onSuccess = { firebaseUser ->
                    // Obtener rol y perfil de Firestore
                    FirestoreUserRepository.getUserProfile(firebaseUser.uid) { profile ->
                        val firestoreRole = profile?.get("role") as? String ?: "customer"
                        // Si el correo es admin, garantizamos el rol independientemente de Firestore
                        val isAdminEmail = email.trim().lowercase() ==
                            FirestoreUserRepository.ADMIN_EMAIL.trim().lowercase()
                        val role = if (isAdminEmail) "admin" else firestoreRole

                        val name = profile?.get("fullName") as? String
                            ?: extractNameFromEmail(email)
                        val phone = profile?.get("phone") as? String ?: ""

                        // Si era admin pero no tenía documento en Firestore, crearlo ahora
                        if (isAdminEmail && profile == null) {
                            FirestoreUserRepository.createUserProfile(
                                firebaseUser.uid, name, email
                            ) { /* en segundo plano */ }
                        }

                        // Actualizar sesión global
                        AppSession.userId = firebaseUser.uid
                        AppSession.userEmail = email
                        AppSession.userRole = role

                        // Guardar en SharedPreferences para acceso local
                        UserRepository.saveUserFromFirebase(firebaseUser.uid, name, email)

                        val user = User(
                            id = firebaseUser.uid,
                            fullName = name,
                            email = email,
                            phone = phone
                        )
                        _loginResult.postValue(Result.success(user))
                        _isLoading.postValue(false)
                    }
                },
                onFailure = { e ->
                    _loginResult.postValue(Result.failure(e))
                    _isLoading.postValue(false)
                }
            )
        }
    }

    // ─── Registro ──────────────────────────────────────────────────────────────

    fun register(fullName: String, email: String, password: String, phone: String) {
        when {
            fullName.isBlank() -> { _registerResult.value = Result.failure(Exception("El nombre es requerido")); return }
            email.isBlank() -> { _registerResult.value = Result.failure(Exception("El correo es requerido")); return }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _registerResult.value = Result.failure(Exception("Correo electrónico inválido")); return
            }
            password.length < 6 -> { _registerResult.value = Result.failure(Exception("La contraseña debe tener al menos 6 caracteres")); return }
        }

        _isLoading.value = true

        FirebaseAuthRepository.register(email, password) { result ->
            result.fold(
                onSuccess = { firebaseUser ->
                    // Crear perfil en Firestore
                    FirestoreUserRepository.createUserProfile(
                        uid = firebaseUser.uid,
                        name = fullName,
                        email = email,
                        phone = phone
                    ) { role ->
                        // Actualizar sesión global
                        AppSession.userId = firebaseUser.uid
                        AppSession.userEmail = email
                        AppSession.userRole = role

                        // Guardar localmente
                        UserRepository.saveUserFromFirebase(firebaseUser.uid, fullName, email)

                        val user = User(
                            id = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            phone = phone
                        )
                        _registerResult.postValue(Result.success(user))
                        _isLoading.postValue(false)
                    }
                },
                onFailure = { e ->
                    _registerResult.postValue(Result.failure(e))
                    _isLoading.postValue(false)
                }
            )
        }
    }

    // ─── Recuperar contraseña ──────────────────────────────────────────────────

    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        FirebaseAuthRepository.sendPasswordReset(email, onResult)
    }

    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }

    private fun extractNameFromEmail(email: String): String {
        val local = email.substringBefore("@")
        return local.replace(".", " ").replaceFirstChar { it.uppercase() }
    }
}
