// Author: Pair Programmer
// OS support: All
// Description: Fragment to display a list of games, handle searching, filtering, favorites, and pagination.
package com.example.checkpoint.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView // Correct SearchView import
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar // Use Snackbar or Toast consistently
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Use ktx delegate for ViewModel
    private val homeViewModel: HomeViewModel by viewModels()

    // Use lateinit for adapter as it's initialized in onViewCreated
    private lateinit var gamesAdapter: GameAdapter

    private var searchJob: Job? = null
    private val searchDelayMs = 500L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // Setup adapter and listeners first
        setupSearchView()
        setupChipFilters()
        setupFabButton()
        setupSwipeToRefresh()
        observeViewModel() // Observe data changes

        // Load initial data only if the list is currently empty (handles configuration changes)
        if (homeViewModel.games.value.isNullOrEmpty()) {
            homeViewModel.loadGames()
        }
    }

    private fun setupRecyclerView() {
        // Instantiate adapter with correct two-parameter constructor
        gamesAdapter = GameAdapter(
            onGameClick = { game: Game ->
                // Navigate using correct Directions class
                val action = HomeFragmentDirections.actionHomeFragmentToGameDetailFragment(game.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { game: Game ->
                // Call ViewModel to handle favorite toggle
                homeViewModel.toggleFavorite(game) // Assuming this exists now
            }
        )

        binding.recyclerViewGames.apply {
            adapter = gamesAdapter
            layoutManager = LinearLayoutManager(requireContext()) // Default to LinearLayout
            setHasFixedSize(true) // Optimization if item size doesn't change

            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy <= 0) return // Only detect downward scroll

                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    val isLoading = homeViewModel.isLoading.value == false
                    val isLoadingMore = homeViewModel.isLoadingMore.value == false // Check specific pagination loading state
                    val threshold = 5 // How many items before the end to trigger loading

                    // Check if not loading, near the end, and have items
                    if (!isLoading && !isLoadingMore && totalItemCount > 0 &&
                        (visibleItemCount + firstVisibleItemPosition + threshold) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                        // Check if ViewModel indicates more pages are available
                        // if (homeViewModel.canLoadMore()) { // Assumes ViewModel exposes this capability
                        Log.d("HomeFragment", "Loading next page triggered")
                        homeViewModel.loadNextPage() // Assuming this function exists
                        // }
                    }
                }
            })
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("HomeFragment", "Swipe to refresh triggered")
            binding.searchView.setQuery("", false) // Optionally clear search on refresh
            homeViewModel.refreshGames() // Assuming this function exists in ViewModel
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel() // Cancel any pending delayed search
                view?.clearFocus() // Hide keyboard
                if (!query.isNullOrBlank()) {
                    Log.d("HomeFragment", "Search submitted: $query")
                    performSearch(query)
                }
                return true // Indicate query handled
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel() // Cancel previous job
                searchJob = MainScope().launch {
                    delay(searchDelayMs)
                    if (view == null || _binding == null) return@launch // Check fragment view validity
                    val currentQuery = binding.searchView.query?.toString() // Get current query text when delay finishes
                    if (currentQuery == newText) { // Perform search only if text hasn't changed during delay
                        if (!newText.isNullOrBlank()) {
                            Log.d("HomeFragment", "Search text changed (debounced): $newText")
                            performSearch(newText)
                        } else if (newText.isNullOrEmpty()) {
                            // Clear search results, load initial set
                            Log.d("HomeFragment", "Search cleared")
                            homeViewModel.loadGames() // Reset to all games/current filter
                        }
                    }
                }
                return true // Indicate change handled
            }
        })
    }

    private fun performSearch(query: String) {
        // Let observers handle UI changes based on ViewModel state
        homeViewModel.loadGames(search = query)
    }

    private fun setupChipFilters() {
        val chipToGenreMap = mapOf(
            binding.chipAll to null,
            binding.chipAction to "action",
            binding.chipAdventure to "adventure",
            binding.chipRPG to "role-playing-games-rpg",
            binding.chipShooter to "shooter"
        )

        chipToGenreMap.forEach { (chip, genreId) ->
            chip.setOnClickListener {
                Log.d("HomeFragment", "Chip clicked - Genre: $genreId")
                binding.searchView.setQuery("", false) // Clear search query
                binding.searchView.clearFocus() // Remove focus/keyboard
                homeViewModel.loadGames(genre = genreId) // Load games for selected genre
            }
        }
    }

    private fun setupFabButton() {
        binding.fabFilterOptions.setOnClickListener { view -> // Rename lambda param to avoid conflict
            val layoutManager = binding.recyclerViewGames.layoutManager
            if (layoutManager is GridLayoutManager) {
                // Switch to Linear
                binding.recyclerViewGames.layoutManager = LinearLayoutManager(requireContext())
                Snackbar.make(view, "Switched to List view", Snackbar.LENGTH_SHORT).show()
            } else {
                // Switch to Grid
                binding.recyclerViewGames.layoutManager = GridLayoutManager(requireContext(), 2)
                // Note: SpanSizeLookup for loading item might be needed if adapter supports it again
                Snackbar.make(view, "Switched to Grid view", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        homeViewModel.games.observe(viewLifecycleOwner) { gamesList ->
            Log.d("HomeFragment", "Observer: Received ${gamesList?.size ?: 0} games")
            // Submit list (handles null automatically)
            gamesAdapter.submitList(gamesList)

            // Handle empty state visibility based on list and loading status
            val isLoading = homeViewModel.isLoading.value == false
            val hasError = !homeViewModel.errorMessage.value.isNullOrEmpty() // Check if error message exists
            val isEmpty = gamesList.isNullOrEmpty() && !isLoading && !hasError

            binding.textViewErrorHome.isVisible = isEmpty || hasError // Show if empty OR error
            if (isEmpty) {
                binding.textViewErrorHome.text = getString(R.string.no_games_found)
            }
            // Error text is handled in the errorMessage observer

            binding.recyclerViewGames.isVisible = !isEmpty && !hasError // Show list if not empty and no error
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show SwipeRefreshLayout spinner only if triggered by swipe, not initial load or pagination
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            // Handle main progress bar visibility (e.g., show only on initial load/refresh)
            val isLoadingMore = homeViewModel.isLoadingMore.value == false
            binding.progressBarHome.isVisible = isLoading && !isLoadingMore && !binding.swipeRefreshLayout.isRefreshing
        }

        // Observer for pagination loading state (e.g., to show a footer spinner)
        homeViewModel.isLoadingMore.observe(viewLifecycleOwner) { isLoadingMore ->
            Log.d("HomeFragment", "Observer: isLoadingMore changed to $isLoadingMore")
            // If using an adapter that supports a loading item type, update it here.
            // Example (if adapter had submitList(games, isLoadingMore)):
            // homeViewModel.games.value?.let { gamesAdapter.submitList(it, isLoadingMore) }
            // Since our adapter doesn't, we might show/hide a separate footer progress bar
            // binding.progressBarPagination.isVisible = isLoadingMore
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            val hasError = !errorMessage.isNullOrEmpty()
            // Show error message text if error occurred
            binding.textViewErrorHome.isVisible = hasError
            if (hasError) {
                binding.textViewErrorHome.text = errorMessage
                // Optionally show Snackbar as well, but might be redundant with the TextView
                // Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel() // Cancel any ongoing search coroutine
        binding.recyclerViewGames.adapter = null // Prevent memory leaks
        _binding = null // Crucial for Fragments
    }
}
// --- End of HomeFragment.kt ---