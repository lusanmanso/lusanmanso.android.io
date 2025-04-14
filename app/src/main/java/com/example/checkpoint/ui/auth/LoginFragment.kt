package com.example.checkpoint.ui.auth

import android.os.Bundle
import android.util.Log // Logging
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Toast Messages
import com.example.checkpoint.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth // Firebase Auth
import com.google.firebase.auth.ktx.auth // KTX
import com.google.firebase.ktx.Firebase // Firebase KTX

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Initialize Firebase Auth
        auth = Firebase.auth

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Login Button
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign In
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) { // Success: Navigate
                    Log.d("LoginFragment", "signInWithEmail: Success")
                    val user = auth.currentUser
                    Toast.makeText(context, "Authentication: Success", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to the next fragment of games
                } else { // Fail: Display message
                    Log.w("LoginFragment", "signInWithEmail: Failure", task.exception)
                    Toast.makeText(context, "Authentication: Failure - ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    // TODO: Show error message
                }
            }
        }

        // Register Button

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("LoginFragment", "User already logged in: ${currentUser.uid}")
            // TODO: Navigate to the next fragment of games
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}