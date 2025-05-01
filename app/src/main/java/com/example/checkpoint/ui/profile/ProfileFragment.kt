package com.example.checkpoint.ui.profile // Adjust package if needed

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.checkpoint.R
import com.example.checkpoint.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.textViewProfileInfo.text = "Logged in as: ${currentUser.email}"
            binding.buttonLogout.setOnClickListener {
                // Sign out directly using FirebaseAuth
                auth.signOut()
                // Navigate back to the Login screen, clearing the back stack
                findNavController().navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // Clears the entire graph back stack
                    .build())
            }
        } else {
            binding.textViewProfileInfo.text = "Not logged in"
            binding.buttonLogout.visibility = View.GONE // Hide logout if not logged in
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}