package com.example.checkpoint.ui.detail

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.checkpoint.R
import com.example.checkpoint.databinding.FragmentGameDetailBinding

class GameDetailFragment: Fragment() {
    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!! // So it is not null after onCreateView
    private val args: GameDetailFragmentArgs by navArgs() // Necesitarás importar la clase Args generada
    private val viewModel: GameDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadGameDetails(args.gameId) // Carga inicial

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetail.isVisible = isLoading
            binding.contentGroup.isVisible = !isLoading // O maneja la visibilidad de forma individual
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Muestra el error (Toast, Snackbar, etc.)
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError() // Limpia el error después de mostrarlo
            }
        }

        viewModel.gameDetails.observe(viewLifecycleOwner) { gameDetail ->
            gameDetail?.let { bindGameDetails(it) }
        }
    }

    private fun bindGameDetails(gameDetail: GameDetail) {
        binding.textViewGameTitleDetail.text = gameDetail.name
        binding.textViewRating.text = getString(R.string.rating_format, gameDetail.rating ?: 0.0, gameDetail.ratingTop ?: 5) // Usa un string resource formateado
        binding.textViewReleaseDate.text = gameDetail.released ?: getString(R.string.tba) // Maneja fecha nula o TBA

        // Descripción: Decide si usar description (puede tener HTML) o descriptionRaw
        // Si usas 'description' y contiene HTML:
        // binding.textViewGameDescription.text = Html.fromHtml(gameDetail.description, Html.FROM_HTML_MODE_COMPACT)
        // Si usas 'descriptionRaw':
        binding.textViewGameDescription.text = gameDetail.descriptionRaw

        // Carga la imagen con Glide
        Glide.with(this)
            .load(gameDetail.backgroundImage)
            .placeholder(R.drawable.placeholder_image) // Usa un placeholder adecuado
            .error(R.drawable.error_image) // Usa una imagen de error adecuada
            .into(binding.imageViewGameDetail)

        // Actualiza otros campos como plataformas si los descomentas en GameDetail.kt
        // binding.textViewPlatforms.text = gameDetail.platforms?.joinToString { it.platform?.name ?: "" } ?: getString(R.string.not_available)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}