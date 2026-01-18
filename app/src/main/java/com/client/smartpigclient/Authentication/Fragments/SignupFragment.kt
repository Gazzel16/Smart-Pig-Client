package com.client.smartpigclient.Authentication.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.client.smartpigclient.Authentication.Api.AuthenticationRI
import com.client.smartpigclient.Authentication.Model.SignupRequest
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.R
import com.client.smartpigclient.databinding.FragmentSignupBinding
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.login.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.btnSignup.setOnClickListener {
            signup()
        }
    }

    private fun signup() {
        val fullname = binding.etFullname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        // Basic validation
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "password must be the same", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Signing in....."

        lifecycleScope.launch {
            try {
                val response = AuthenticationRI.authApi().signup(
                    SignupRequest(
                        fullname = fullname,
                        email = email,
                        password = password
                    )
                )

                Toast.makeText(
                    requireContext(),
                    "Signup successfully",
                    Toast.LENGTH_LONG
                ).show()

                binding.etFullname.setText("")
                binding.etEmail.setText("")
                binding.etPassword.setText("")
                binding.confirmPassword.setText("")

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()

                // TODO: Navigate to Login or Home
                Log.d("SIGNUP", "User created: ${response.id}")

            } catch (e: Exception) {

                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "Signup"

                Log.e("SIGNUP", e.message ?: "Signup failed")
                Toast.makeText(
                    requireContext(),
                    "Signup failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
