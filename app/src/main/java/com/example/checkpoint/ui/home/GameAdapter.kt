package com.example.checkpoint.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.ItemGameBinding

class GameAdapter(
    private val onGameClick: (Game) -> Unit,
    private val onFavoriteClick: (Game) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    // Map to store the favorite status (Game ID -> isFavorite)
    private var favoriteStatusMap = mapOf<Int, Boolean>()

    /**
     * Updates the internal map of favorite statuses and refreshes the adapter.
     * @param newStatusMap A map where the key is the game ID and the value is true if favorite, false otherwise.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateFavoriteStatus(newStatusMap: Map<Int, Boolean>) {
        favoriteStatusMap = newStatusMap
        // Notify the adapter that the data set has changed to force re-binding of visible items.
        // Consider using more specific notifyItemChanged if performance becomes an issue.
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        holder.bind(game)
    }

    inner class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        // This function binds the data from a Game object to the views in the item layout
        fun bind(game: Game) {
            // --- CORRECTIONS ---
            // Use the correct ID from item_game.xml for the title TextView
            binding.textViewGameName.text = game.name

            // Release date badge
            binding.textViewReleaseDate.text = game.released?.substringBefore('-') ?: ""

            // Game rating display
            binding.textViewGameRating.text = game.rating?.let { String.format("%.1f", it) } ?: ""

            // Use the correct ID for the background ImageView and the correct property from Game.kt
            Glide.with(binding.imageViewGameBackground.context)
                .load(game.backgroundImage) // Use game.backgroundImage
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .transform(RoundedCorners(12))
                .into(binding.imageViewGameBackground) // Target the correct ImageView


            // Set favorite button state based on the map, using the correct ID for the favorite ImageView
            val isFavorite = favoriteStatusMap[game.id] ?: false // Default to false if not in map
            binding.imageViewFavorite.setImageResource( // Use imageViewFavorite
                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            // --- END CORRECTIONS ---

            // Click listener for the whole item card
            binding.root.setOnClickListener {
                onGameClick(game)
            }

            // Click listener for the favorite ImageView
            binding.imageViewFavorite.setOnClickListener { // Use imageViewFavorite
                onFavoriteClick(game)
                // Optionally, update the icon immediately for better UX,
                // though the LiveData update will handle it eventually.
                // binding.imageViewFavorite.setImageResource(
                //     if (!isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                // )
            }

            // Optional: Bind other views like rating or release date if needed
            // binding.textViewGameRating.text = game.rating?.toString() ?: "N/A"
            // binding.textViewReleaseDate.text = game.released?.substringBefore('-') ?: "" // Example: Show only year
        }
    }
}

// DiffUtil Callback for ListAdapter efficiency
class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        // Compare relevant content fields. If only favorite status changes externally,
        // comparing by ID might be enough. If other fields can change, compare them too.
        // return oldItem == newItem
        // More specific comparison if needed:
        return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.backgroundImage == newItem.backgroundImage // Add other relevant fields
    }
}
