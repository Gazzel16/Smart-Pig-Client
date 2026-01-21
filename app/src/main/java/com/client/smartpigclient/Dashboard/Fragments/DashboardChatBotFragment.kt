package com.client.smartpigclient.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.smartpigclient.Dashboard.Adapter.ChatAdapter
import com.client.smartpigclient.Dashboard.Model.ChatMessage
import com.client.smartpigclient.Dashboard.Api.DashBoardRI
import com.client.smartpigclient.Dashboard.Model.ChatRequest
import com.client.smartpigclient.databinding.FragmentDashboardChatBotBinding
import com.client.smartpigclient.R
import com.client.smartpigclient.Utils.TokenManager
import kotlinx.coroutines.launch

class DashboardChatBotFragment : Fragment() {

    private var _binding: FragmentDashboardChatBotBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardChatBotBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupButtons()

        val insightMessage = arguments?.getString("INSIGHT_MESSAGE")
        insightMessage?.let {
            binding.questionInputs.visibility = View.GONE
            sendMessage(it)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter
    }

    private fun setupButtons() {

        // send button
        binding.sentBtn.setOnClickListener {
            binding.questionInputs.visibility = View.GONE
            val text = binding.inputMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        // Suggested quick questions
        binding.input1.setOnClickListener {
            binding.questionInputs.visibility = View.GONE
            sendMessage(binding.input1.text.toString())
        }
        binding.input2.setOnClickListener {
            binding.questionInputs.visibility = View.GONE
            sendMessage(binding.input2.text.toString())
        }
        binding.input3.setOnClickListener {
            binding.questionInputs.visibility = View.GONE
            sendMessage(binding.input3.text.toString())
        }
    }

    private fun sendMessage(text: String) {
        // Add user message
        chatAdapter.addMessage(ChatMessage(text, true))
        scrollToBottom()

        binding.inputMessage.setText("")

        // Call API with user text
        fetchBotResponse(text)
    }

    private fun fetchBotResponse(userText: String) {
        lifecycleScope.launch {
            try {
                val api = DashBoardRI.getInstance(TokenManager.getToken(requireContext()))

                // Correct API call
                val response = api.chatBotResponse(
                    ChatRequest(request = userText)
                )

                // Add bot message using response.response
                chatAdapter.addMessage(ChatMessage(response.response, false))
                scrollToBottom()

            } catch (e: Exception) {
                chatAdapter.addMessage(ChatMessage("Error: Cannot connect to server.", false))
                scrollToBottom()
            }
        }
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            binding.rvChat.smoothScrollToPosition(messages.size - 1)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }
}
