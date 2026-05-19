package com.shopapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.shopapp.AppSession
import com.shopapp.R
import com.shopapp.data.repository.AddressRepository
import com.shopapp.data.repository.FirebaseAuthRepository
import com.shopapp.data.repository.FirestoreUserRepository
import com.shopapp.data.repository.NotificationRepository
import com.shopapp.data.repository.PaymentRepository
import com.shopapp.data.repository.UserRepository
import com.shopapp.ui.auth.AuthActivity
import com.shopapp.ui.home.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inicializar repositorios locales
        UserRepository.init(this)
        AddressRepository.init(this)
        PaymentRepository.init(this)
        NotificationRepository.init(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 1800)
    }

    private fun checkAuthAndNavigate() {
        if (FirebaseAuthRepository.isLoggedIn()) {
            val uid   = FirebaseAuthRepository.getCurrentUserId()    ?: ""
            val email = FirebaseAuthRepository.getCurrentUserEmail() ?: ""

            // Si el correo es el de admin, asignamos el rol directamente sin esperar Firestore
            val isAdminEmail = email.trim().lowercase() ==
                FirestoreUserRepository.ADMIN_EMAIL.trim().lowercase()

            if (isAdminEmail) {
                // Rol garantizado: no dependemos de que el documento exista en Firestore
                AppSession.userId    = uid
                AppSession.userEmail = email
                AppSession.userRole  = "admin"

                // Intentar crear/actualizar el documento en Firestore (en segundo plano)
                FirestoreUserRepository.createUserProfile(uid, "Admin", email) { /* ignore */ }

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Usuarios normales: obtener rol desde Firestore
                FirestoreUserRepository.getUserRole(uid) { role ->
                    AppSession.userId    = uid
                    AppSession.userEmail = email
                    AppSession.userRole  = role

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        } else {
            AppSession.clear()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}
