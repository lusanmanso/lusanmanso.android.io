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
import com.google.firebase.auth.FirebaseAuth
import com.example.checkpoint.ui.home.GameAdapter

public class FavoritesFragment : Fragment() {

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
            // Ensure UI is correctly handled when not logged in
            binding.textViewEmptyFavorites.text = getString(R.string.login_required_to_view_favorites) // Use a specific string
            binding.textViewEmptyFavorites.isVisible = true
            binding.recyclerViewFavorites.isVisible = false
            binding.progressBarFavorites.isVisible = false
            return // Stop setup if not logged in
        }

        setupRecyclerView()
        observeViewModel()

        // Load data only if logged in
        viewModel.loadFavoriteGames()
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(
            onGameClick = { game: Game ->
                // Navigate to detail, passing the game ID
                val action = FavoritesFragmentDirections.actionFavoritesFragmentToGameDetailFragment(game.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { game: Game ->
                // In Favorites, clicking the heart should *remove* the favorite
                viewModel.removeFromFavorites(game.id)
                // Provide immediate visual feedback (optional, ViewModel update will follow)
                // gameAdapter.updateFavoriteStatus(mapOf(game.id to false)) // Could cause flicker if list reloads quickly
                Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewFavorites.adapter = gameAdapter
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.favoriteGames.observe(viewLifecycleOwner) { games ->
            // Submit the list of favorite games to the adapter
            gameAdapter.submitList(games)

            val favoriteStatusMap = games.associate { it.id to true }
            // Update the adapter's internal status map
            gameAdapter.updateFavoriteStatus(favoriteStatusMap)

            // Update visibility based on the list being empty or not
            val isLoading = viewModel.isLoading.value ?: false
            binding.textViewEmptyFavorites.isVisible = games.isNullOrEmpty() && !isLoading // Show empty message only if not loading
            if(games.isNullOrEmpty() && !isLoading) {
                binding.textViewEmptyFavorites.text = getString(R.string.no_favorites_message)
            }
            binding.recyclerViewFavorites.isVisible = !games.isNullOrEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarFavorites.isVisible = isLoading
            // Adjust visibility based on loading state
            if (isLoading) {
                binding.recyclerViewFavorites.isVisible = false
                binding.textViewEmptyFavorites.isVisible = false
            } else {
                // Re-check list emptiness when loading finishes
                val games = viewModel.favoriteGames.value
                binding.recyclerViewFavorites.isVisible = !games.isNullOrEmpty()
                binding.textViewEmptyFavorites.isVisible = games.isNullOrEmpty()
                if(games.isNullOrEmpty()) {
                    binding.textViewEmptyFavorites.text = getString(R.string.no_favorites_message)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError() // Clear error after showing
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
