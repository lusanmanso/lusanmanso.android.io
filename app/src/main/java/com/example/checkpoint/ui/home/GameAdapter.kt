package com.example.checkpoint.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.ItemGameBinding

class GameAdapter(
    private val onGameClick: (Game) -> Unit,
    private val onFavoriteClick: (Game) -> Unit // Added back favorite click listener
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) { // Simplified ListAdapter type back to Game directly if loading indicator handled differently

    // ViewHolder for a game
    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            binding.textViewGameName.text = game.name
            // Use String.format for rating to handle nulls safely and format
            binding.textViewGameRating.text = String.format("%.1f / 5", game.rating ?: 0.0)
            binding.textViewReleaseDate.text = game.released ?: itemView.context.getString(R.string.tba)

            Glide.with(itemView.context)
                .load(game.backgroundImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_background) // Placeholder
                .error(R.drawable.ic_launcher_foreground) // Error placeholder
                .into(binding.imageViewGameBackground)

            // Set click listener for the whole item
            binding.root.setOnClickListener {
                onGameClick(game)
            }

            // Set click listener for the favorite icon
            binding.imageViewFavorite.setOnClickListener {
                onFavoriteClick(game)
                // Optional: Immediately toggle icon state visually for responsiveness
                // This assumes the ViewModel call will succeed. A more robust solution
                // would wait for confirmation or observe a LiveData.
                // toggleFavoriteIcon(!game.isFavorite) // Assuming 'isFavorite' exists
            }

            // Update favorite icon based on game state (Requires 'isFavorite' field in Game model)
            // val isFavorite = game.isFavorite // Assuming 'isFavorite' boolean field exists in Game model
            // toggleFavoriteIcon(isFavorite)
            // Placeholder: Set default icon until favorite status is available in Game model
            binding.imageViewFavorite.setImageResource(R.drawable.ic_favorite_border)
        }

        // Helper function to update icon (optional)
        // private fun toggleFavoriteIcon(isFavorite: Boolean) {
        //     val favoriteIconRes = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        //     binding.imageViewFavorite.setImageResource(favoriteIconRes)
        // }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil for comparing Game objects
    class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            // Compare relevant fields, including isFavorite for visual updates
            return oldItem == newItem
        }
    }
}
