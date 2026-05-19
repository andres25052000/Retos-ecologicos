package com.shopapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shopapp.databinding.FragmentRegisterBinding
import com.shopapp.ui.home.MainActivity

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authActivity = requireActivity() as AuthActivity
        val viewModel = authActivity.authViewModel

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val phone = binding.etPhone.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val confirm = binding.etConfirmPassword.text?.toString() ?: ""

            if (password != confirm) {
                binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }
            binding.tilConfirmPassword.error = null
            viewModel.register(name, email, password, phone)
        }

        binding.tvLogin.setOnClickListener {
            authActivity.navigateToLogin()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                Toast.makeText(requireContext(), "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }.onFailure { e ->
                Toast.makeText(requireContext(), e.message ?: "Error al registrarse", Toast.LENGTH_LONG).show()
            }
            viewModel.clearResults()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
