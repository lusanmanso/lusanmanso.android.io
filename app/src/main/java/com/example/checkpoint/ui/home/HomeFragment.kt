package com.example.checkpoint.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.FragmentHomeBinding
import com.example.checkpoint.ui.favorites.FavoritesViewModel
import com.google.firebase.auth.FirebaseAuth

public class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private lateinit var gameAdapter: GameAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager


    private var currentFavoriteIds = setOf<Int>()

    private var isGridView = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModels()


        binding.buttonToggleView.setOnClickListener {
            isGridView = !isGridView
            updateLayoutView()
        }

        updateLayoutView()


        favoritesViewModel.loadFavoriteGames()
    }

    private fun setupRecyclerView() {
        layoutManager = GridLayoutManager(requireContext(), 2)

        gameAdapter = GameAdapter(
            onGameClick = { game: Game ->
                val action = HomeFragmentDirections.actionHomeFragmentToGameDetailFragment(game.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { game: Game ->
                toggleFavorite(game)
            }
        )

        binding.recyclerViewGames.adapter = gameAdapter
        binding.recyclerViewGames.layoutManager = layoutManager

        binding.recyclerViewGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)


                val firstVisibleItemPosition = when (layoutManager) {
                    is LinearLayoutManager -> (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    is GridLayoutManager -> (layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                    else -> 0
                }

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount


                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5 && firstVisibleItemPosition >= 0) {
                    viewModel.loadMoreGames()
                }
            }
        })
    }

    private fun observeViewModels() {

        viewModel.games.observe(viewLifecycleOwner) { games ->
            gameAdapter.submitList(games)
            updateEmptyState()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading && (viewModel.games.value.isNullOrEmpty())
            updateEmptyState()
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.textViewError.text = errorMessage
            updateEmptyState()
        }


        favoritesViewModel.favoriteGames.observe(viewLifecycleOwner) { favoriteGamesList ->

            val favoriteMap = favoriteGamesList?.associate { it.id to true } ?: emptyMap()

            currentFavoriteIds = favoriteMap.keys

            gameAdapter.updateFavoriteStatus(favoriteMap)
            Log.d("HomeFragment", "Favorite status derived from list: $favoriteMap")
        }


        favoritesViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), "Favorite Error: $errorMsg", Toast.LENGTH_SHORT).show()
                favoritesViewModel.clearError()
            }
        }

    }

    private fun updateEmptyState() {
        val isLoading = viewModel.isLoading.value ?: false
        val gamesList = viewModel.games.value
        val errorMessage = viewModel.errorMessage.value
        val isListEmpty = gamesList.isNullOrEmpty()
        val showError = !isLoading && isListEmpty && errorMessage != null
        binding.textViewError.isVisible = showError
        binding.recyclerViewGames.isVisible = !isLoading && !isListEmpty
        if (!isLoading && !isListEmpty) {
            binding.progressBar.isVisible = false
        }
    }

    private fun toggleFavorite(game: Game) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.login_required_to_favorite, Toast.LENGTH_SHORT).show()
            return
        }


        val isCurrentlyFavorite = currentFavoriteIds.contains(game.id)

        if (isCurrentlyFavorite) {

            favoritesViewModel.removeFromFavorites(game.id)
            Log.d("HomeFragment", "Calling removeFromFavorites for game ${game.id}")
        } else {

            favoritesViewModel.addFavorite(game)
            Log.d("HomeFragment", "Calling addFavorite for game ${game.id}")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateLayoutView() {
        layoutManager = if (isGridView) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        binding.recyclerViewGames.layoutManager = layoutManager

        val iconRes = if (isGridView) R.drawable.ic_list_view else R.drawable.ic_grid_view
        binding.buttonToggleView.setImageResource(iconRes)

        gameAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewGames.adapter = null
        _binding = null
    }
}