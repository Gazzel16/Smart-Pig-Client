package com.client.smartpigclient.Authentication.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.R
import com.client.smartpigclient.Utils.TokenManager
import com.client.smartpigclient.databinding.FragmentGetStartedBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GetStartedFragment : Fragment() {
    private var _binding: FragmentGetStartedBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if token exists
        val token = TokenManager.getToken(requireContext())
        if (token.isNotEmpty()) {
            // Token exists, redirect to Dashboard
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGetStartedBinding.inflate(inflater, container, false)

        binding.signup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignupFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.login.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GetStartedFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
