// Author: Pair Programmer
// OS support: Android
// Description: Fragment displaying games, using FavoritesViewModel for favorite logic.
package com.example.checkpoint.ui.home

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by activityViewModels() // Obtener FavoritesViewModel

    private lateinit var gameAdapter: GameAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    // Guardamos el estado actual de los favoritos para consultarlo fácilmente
    private var currentFavoriteIds = setOf<Int>()
    // State to toggle between grid and list
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

        // Toggle button to switch layout
        binding.buttonToggleView.setOnClickListener {
            isGridView = !isGridView
            updateLayoutView()
        }
        // Initialize layout state
        updateLayoutView()

        // Cargar favoritos iniciales si es necesario
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
                toggleFavorite(game) // Llama a la función del fragment
            }
        )

        binding.recyclerViewGames.adapter = gameAdapter
        binding.recyclerViewGames.layoutManager = layoutManager

        binding.recyclerViewGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // --- INICIO CAMBIO ---
                // Determinar el primer item visible según el tipo de LayoutManager
                val firstVisibleItemPosition = when (layoutManager) {
                    is LinearLayoutManager -> (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    is GridLayoutManager -> (layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                    else -> 0 // Valor por defecto o manejo de error si es otro tipo (poco probable aquí)
                }
                // --- FIN CAMBIO ---
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                // Lógica de paginación (sin cambios)
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5 && firstVisibleItemPosition >= 0) {
                    viewModel.loadMoreGames()
                }
            }
        })
    }

    private fun observeViewModels() {
        // Observadores para HomeViewModel
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

        // --- Observador para FavoritesViewModel ---
        // Observa la LISTA de juegos favoritos
        favoritesViewModel.favoriteGames.observe(viewLifecycleOwner) { favoriteGamesList ->
            // Crear el mapa de estado (Map<Int, Boolean>) a partir de la lista de juegos
            val favoriteMap = favoriteGamesList?.associate { it.id to true } ?: emptyMap()
            // Guardar los IDs actuales para la función toggleFavorite
            currentFavoriteIds = favoriteMap.keys
            // Actualizar el adaptador
            gameAdapter.updateFavoriteStatus(favoriteMap)
            Log.d("HomeFragment", "Favorite status derived from list: $favoriteMap")
        }

        // (Opcional) Observar errores del FavoritesViewModel
        favoritesViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), "Favorite Error: $errorMsg", Toast.LENGTH_SHORT).show()
                favoritesViewModel.clearError() // Limpiar error después de mostrarlo
            }
        }
        // --- Fin Observador para FavoritesViewModel ---
    }

    // Función auxiliar para manejar la visibilidad (sin cambios)
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

    // --- Esta función ahora usa add/remove de favoritesViewModel ---
    private fun toggleFavorite(game: Game) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.login_required_to_favorite, Toast.LENGTH_SHORT).show()
            return
        }

        // Consultar el estado usando la variable actualizada por el observer
        val isCurrentlyFavorite = currentFavoriteIds.contains(game.id)

        if (isCurrentlyFavorite) {
            // Llama a removeFromFavorites en FavoritesViewModel
            favoritesViewModel.removeFromFavorites(game.id)
            Log.d("HomeFragment", "Calling removeFromFavorites for game ${game.id}")
        } else {
            // Llama a addFavorite en FavoritesViewModel (NECESITA SER AÑADIDO AL VIEWMODEL)
            favoritesViewModel.addFavorite(game)
            Log.d("HomeFragment", "Calling addFavorite for game ${game.id}")
        }
    }

    // Function to switch RecyclerView layout manager and update button text
    private fun updateLayoutView() {
        layoutManager = if (isGridView) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        binding.recyclerViewGames.layoutManager = layoutManager
        // Update toggle icon instead of text on FloatingActionButton
        val iconRes = if (isGridView) R.drawable.ic_list_view else R.drawable.ic_grid_view
        binding.buttonToggleView.setImageResource(iconRes)
        // Refresh adapter to apply spacing adjustments
        gameAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewGames.adapter = null
        _binding = null
    }
}
// --- End of HomeFragment.kt ---
