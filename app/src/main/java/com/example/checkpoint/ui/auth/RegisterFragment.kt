package com.example.checkpoint.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.checkpoint.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment: Fragment() {
    // Firebase
    private lateinit var auth: FirebaseAuth

    // ViewBinding
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!! // Valid only onCreateView & onDestroyView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth // Initialize Firebase
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout with ViewBinding
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listener for register
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextRegisterEmail.text.toString().trim()
            val password = binding.editTextRegisterPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            // Validations
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context,"Please enter all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if fields are empty
            }

            if (password != confirmPassword) {
                Toast.makeText(context,"Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(context,"Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterFragment", "createUserWithEmail: Success")
                    val user = auth.currentUser
                    Toast.makeText(context, "Authentication: Success", Toast.LENGTH_SHORT).show()
                    // Navigate to Login (TODO: ¿O mejor lo llevo ya a la aplicación?)
                    // findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
                } else {
                    Log.w("RegisterFragment", "createUserWithEmail: Failure", task.exception)
                    Toast.makeText(context, "Authentication: Failure - ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Listener for go back to login link
        binding.textViewLoginLink.setOnClickListener {
            findNavController().navigateUp()
            // TODO: Define action findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}