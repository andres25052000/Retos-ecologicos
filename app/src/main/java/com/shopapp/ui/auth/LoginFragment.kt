package com.shopapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shopapp.databinding.FragmentLoginBinding
import com.shopapp.ui.home.MainActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authActivity = requireActivity() as AuthActivity
        val viewModel = authActivity.authViewModel

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            authActivity.navigateToRegister()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            if (email.isBlank()) {
                Toast.makeText(requireContext(), "Ingresa tu correo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sendPasswordReset(email) { success, msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }.onFailure { e ->
                Toast.makeText(requireContext(), e.message ?: "Error al iniciar sesión", Toast.LENGTH_LONG).show()
            }
            viewModel.clearResults()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
