package com.example.checkpoint.ui.detail

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.checkpoint.R
import com.example.checkpoint.databinding.FragmentGameDetailBinding
import com.example.checkpoint.data.models.GameDetail
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import androidx.core.net.toUri
import androidx.core.graphics.toColorInt

class GameDetailFragment : Fragment() {

    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!

    private val args: GameDetailFragmentArgs by navArgs()
    private val viewModel: GameDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()

        // Load game details
        viewModel.loadGameDetails(args.gameId)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetail.isVisible = isLoading
            binding.contentGroup.isVisible = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_loading_details, it),
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.clearError()
            }
        }

        viewModel.gameDetails.observe(viewLifecycleOwner) { gameDetail ->
            gameDetail?.let { nonNullGameDetail ->
                updateUI(nonNullGameDetail)
            }
        }

        // Observe changes in favorite status
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButton(isFavorite)
        }
    }

    private fun setupClickListeners() {
        // Button to visit game website
        binding.websiteButton.setOnClickListener {
            val website = viewModel.gameDetails.value?.website
            if (!website.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, website.toUri())
                startActivity(intent)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.not_available),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // Button to mark as favorite
        binding.fabFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val iconResource = if (isFavorite) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_favorite_border
        }
        binding.fabFavorite.setImageResource(iconResource)

        // Optional: show visual feedback
        if (isFavorite) {
            Snackbar.make(
                binding.root,
                "Added to favorites",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUI(gameDetail: GameDetail) {
        val notAvailable = getString(R.string.not_available) // Default values for unavailable strings
        // val tba = getString(R.string.tba) //Default value for TBA dates

        with(binding) {
            // Title and basic details
            textViewGameTitleDetail.text = gameDetail.name

            val currentRating = gameDetail.rating ?: 0.0 // If rating is null or undefined, use 0.0
            textViewRating.text = getString(R.string.rating_format, currentRating, 5)

            // Configure the color of the rating star based on score
            val starDrawable = textViewRating.compoundDrawablesRelative[0]
            // Safe check before tinting
            starDrawable?.setTint(when {
                currentRating >= 4 -> resources.getColor(R.color.rating_star, requireContext().theme) // Green for good scores
                currentRating >= 2 -> "#FFC107".toColorInt() // Amber for medium/mild scores
                else -> "#F44336".toColorInt()
            })

            // Release date
            textViewReleaseDate.text = if (!gameDetail.released.isNullOrEmpty()) {
                gameDetail.released
            } else {
                getString(R.string.tba)
            }

            // Metacritic (if available)
            gameDetail.metacritic?.let { score ->
                if (score != 0) {
                    textViewMetacritic.text = score.toString()
                    textViewMetacritic.isVisible = true

                    // Change colors based on score
                    val background = textViewMetacritic.background as? GradientDrawable
                    background?.setColor(when {
                        score >= 75 -> "#6DC849".toColorInt() // Green for good scores
                        score >= 50 -> "#FFCC33".toColorInt() // Amber for blabla
                        else -> "#FF4136".toColorInt() // Red for bad scores
                    })
                } else {
                    textViewMetacritic.isVisible = false
                }
            } ?: run { // Handle case where metacritic score itself is null
                textViewMetacritic.isVisible = false
            }

            // Description
            val description = gameDetail.description.takeIf { it.isNotBlank() } ?: notAvailable
            textViewGameDescription.text = Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)

            // Configure links to be clickable
            textViewGameDescription.movementMethod = LinkMovementMethod.getInstance()

            // Platforms
            val platformNames = gameDetail.platforms
                ?.mapNotNull { wrapper -> wrapper.platform?.name } // Get non-null names
                ?.joinToString(", ")
                ?.takeIf { it.isNotBlank() } ?: notAvailable // Use default if list/names are null/empty
            textViewPlatforms.text = platformNames

            // Load main image
            Glide.with(this@GameDetailFragment)
                .load(gameDetail.backgroundImage)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageViewGameDetail)

            // Load additional image if available
            if (!gameDetail.backgroundImageAdditional.isNullOrEmpty()) {
                imageViewBackdrop.isVisible = true
                Glide.with(this@GameDetailFragment)
                    .load(gameDetail.backgroundImageAdditional)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageViewBackdrop)
            } else {
                imageViewBackdrop.isVisible = false
            }

            // Add genres as chips
            chipGroupGenres.removeAllViews()
            gameDetail.genres?.forEach { genre ->
                val chip = Chip(context).apply {
                    text = genre.name
                    isCheckable = false
                }
                chipGroupGenres.addView(chip)
            }

            // Update website button visibility
            websiteButton.isVisible = !gameDetail.website.isNullOrEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
