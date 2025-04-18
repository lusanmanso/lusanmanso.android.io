package com.example.checkpoint.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // Visibility
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checkpoint.ui.home.GameAdapter
import com.example.checkpoint.databinding.FragmentHomeBinding
import com.example.checkpoint.databinding.FragmentRegisterBinding
import com.example.checkpoint.ui.home.HomeViewModel

class HomeFragment: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels() // Delegating to ktx

    private lateinit var gamesAdapter: GameAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        if (homeViewModel.games.value.isNullOrEmpty()) {
            homeViewModel.loadGames()
        }
    }

    private fun setupRecyclerView() {
        gamesAdapter = GameAdapter { game ->
            Toast.makeText(context, "Clicked on ${game.name}", Toast.LENGTH_SHORT).show()
            // findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDetailFragment(game.id)) // Ejemplo si tuvieras pantalla de detalle
        }

        binding.recyclerViewGames.apply {
            adapter = gamesAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Games list
        homeViewModel.games.observe(viewLifecycleOwner) { gamesList ->
            Log.d("HomeFragment", "Updating UI with ${gamesList.size} games")
            gamesAdapter.submitList(gamesList)
        }

        // Loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarHome.isVisible = isLoading
            // binding.recyclerViewGames.isVisible = !isLoading
        }

        // Error messages
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.textViewErrorHome.isVisible = errorMessage != null
            binding.textViewErrorHome.text = errorMessage
            // errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewGames.adapter = null // No leaks
        _binding = null
    }
}