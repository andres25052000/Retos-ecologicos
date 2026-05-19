package com.shopapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shopapp.data.repository.FirebaseAuthRepository
import com.shopapp.data.repository.UserRepository
import com.shopapp.databinding.FragmentEditProfileBinding
import com.shopapp.ui.home.MainActivity

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel    = mainActivity.shopViewModel

        // Pre-rellenar datos actuales
        val user = UserRepository.getCurrentUser()
        user?.let {
            binding.etName.setText(it.fullName)
            binding.etPhone.setText(it.phone)
            binding.tvAvatar.text = it.fullName.firstOrNull()?.uppercase() ?: "U"
        }

        // El avatar es solo visual; foto de perfil se muestra como inicial
        binding.flAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "Cambio de foto disponible próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        binding.btnSave.setOnClickListener {
            val name    = binding.etName.text?.toString()?.trim() ?: ""
            val phone   = binding.etPhone.text?.toString()?.trim() ?: ""
            val newPass = binding.etNewPassword.text?.toString()?.trim() ?: ""
            val confPass= binding.etConfirmPassword.text?.toString()?.trim() ?: ""

            // Validación nombre
            if (name.isBlank()) {
                binding.tilName.error = "El nombre es requerido"
                return@setOnClickListener
            }
            binding.tilName.error = null

            // Validación contraseña (solo si ingresó algo)
            if (newPass.isNotBlank()) {
                if (newPass.length < 6) {
                    binding.tilNewPassword.error = "Mínimo 6 caracteres"
                    return@setOnClickListener
                }
                if (newPass != confPass) {
                    binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                    return@setOnClickListener
                }
                binding.tilNewPassword.error = null
                binding.tilConfirmPassword.error = null
            }

            // Guardar nombre y teléfono (sin campo de dirección — se gestiona en Mis Direcciones)
            UserRepository.updateProfile(name, phone, "")
            viewModel.refreshUser()

            // Cambiar contraseña en Firebase si se ingresó
            if (newPass.isNotBlank()) {
                FirebaseAuthRepository.auth.currentUser
                    ?.updatePassword(newPass)
                    ?.addOnSuccessListener {
                        Toast.makeText(requireContext(), "Perfil y contraseña actualizados ✓", Toast.LENGTH_SHORT).show()
                        mainActivity.navigateUp()
                    }
                    ?.addOnFailureListener { e ->
                        // Si requiere re-autenticación (sesión antigua) se informa al usuario
                        val msg = if (e.message?.contains("requires-recent-login") == true)
                            "Para cambiar la contraseña cierra sesión e inicia de nuevo"
                        else "Error al cambiar contraseña: ${e.message}"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
                mainActivity.navigateUp()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
