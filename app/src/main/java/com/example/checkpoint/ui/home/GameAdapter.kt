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

public class GameAdapter(
    private val onGameClick: (Game) -> Unit,
    private val onFavoriteClick: (Game) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    private var favoriteStatusMap = mapOf<Int, Boolean>()

    @SuppressLint("NotifyDataSetChanged")
    public fun updateFavoriteStatus(newStatusMap: Map<Int, Boolean>) {
        favoriteStatusMap = newStatusMap
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

    public inner class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("DefaultLocale")
        public fun bind(game: Game) {

            binding.textViewGameName.text = game.name

            binding.textViewReleaseDate.text = game.released?.substringBefore('-') ?: ""

            binding.textViewGameRating.text = game.rating?.let { String.format("%.1f", it) } ?: ""

            Glide.with(binding.imageViewGameBackground.context)
                .load(game.backgroundImage)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .transform(RoundedCorners(12))
                .into(binding.imageViewGameBackground)


            val isFavorite = favoriteStatusMap[game.id] ?: false
            binding.imageViewFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )


            binding.root.setOnClickListener {
                onGameClick(game)
            }

            binding.imageViewFavorite.setOnClickListener {
                onFavoriteClick(game)

            }
        }
    }
}

public class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {

        return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.backgroundImage == newItem.backgroundImage
    }
}