package com.example.checkpoint.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.FragmentFavoritesBinding
import com.example.checkpoint.ui.home.GameAdapter // Correct Adapter import
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var gameAdapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            binding.textViewEmptyFavorites.text = getString(R.string.login_required)
            binding.textViewEmptyFavorites.isVisible = true
            binding.recyclerViewFavorites.isVisible = false
            binding.progressBarFavorites.isVisible = false
            return // Stop setup if not logged in
        }

        setupRecyclerView() // Setup RecyclerView first
        observeViewModel() // Then observe ViewModel

        viewModel.loadFavoriteGames() // Load data
    }

    private fun setupRecyclerView() {
        // Instantiate with the corrected GameAdapter (taking two listeners)
        gameAdapter = GameAdapter(
            // Provide lambda for 'onGameClick' parameter
            onGameClick = { game: Game ->
                val action = FavoritesFragmentDirections.actionFavoritesFragmentToGameDetailFragment(game.id)
                findNavController().navigate(action)
            },
            // Provide lambda for 'onFavoriteClick' parameter
            onFavoriteClick = { game: Game ->
                viewModel.removeFromFavorites(game.id)
                Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewFavorites.adapter = gameAdapter
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.favoriteGames.observe(viewLifecycleOwner) { games ->
            // Use the adapter's submitList method for ListAdapter
            gameAdapter.submitList(games) {
                // Optional: Scroll to top after list update
                // binding.recyclerViewFavorites.scrollToPosition(0)
            }

            binding.textViewEmptyFavorites.isVisible = games.isNullOrEmpty()
            if(games.isNullOrEmpty()) {
                binding.textViewEmptyFavorites.text = getString(R.string.no_favorites_message)
            }
            binding.recyclerViewFavorites.isVisible = !games.isNullOrEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarFavorites.isVisible = isLoading
            if (isLoading) {
                binding.recyclerViewFavorites.isVisible = false
                binding.textViewEmptyFavorites.isVisible = false
            } else {
                // Ensure RecyclerView is visible when not loading AND list is not empty
                if (!viewModel.favoriteGames.value.isNullOrEmpty()) {
                    binding.recyclerViewFavorites.isVisible = true
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}