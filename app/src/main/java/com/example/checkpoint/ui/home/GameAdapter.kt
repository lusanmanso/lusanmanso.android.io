package com.example.checkpoint.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // For the img
import com.example.checkpoint.R
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.databinding.ItemGameBinding // ViewBinding of the item

class GameAdapter(private val onClickListener: (Game) -> Unit): ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {
    // ViewHolder contains the references to the views of the item (videogames)
    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            binding.textViewGameName.text = game.name
            binding.textViewGameRating.text = game.rating.toString()

            // Load images with Glide
            Glide.with(binding.imageViewGameBackground.context)
                .load(game.backgroundImage)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.imageViewGameBackground)

            // Listener when clicking on videogame item
            binding.root.setOnClickListener {
                onClickListener(game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        // Inflate the item layout blablabla
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding) // Returns a new instance of the ViewHolder
    }

    // New views (invocates the layout manager for it and replaces them)
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val currentGame = getItem(position)
        holder.bind(currentGame)
    }

    // DiffUtil Callback helps the List Adapter to determine what items have changed
    class GameDiffCallback: DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem // Compare ALL the data
        }

    }
}