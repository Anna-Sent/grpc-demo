package io.grpc.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.grpc.demo.databinding.CardBinding

class FibonacciAdapter : ListAdapter<Fibonacci, FibonacciViewHolder>(
    object : DiffUtil.ItemCallback<Fibonacci>() {

        override fun areItemsTheSame(oldItem: Fibonacci, newItem: Fibonacci) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Fibonacci, newItem: Fibonacci) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FibonacciViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card, parent, false)
        return FibonacciViewHolder(view)
    }

    override fun onBindViewHolder(holder: FibonacciViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class FibonacciViewHolder(itemView: View) : ViewHolder(itemView) {

    private val binding = CardBinding.bind(itemView)

    fun bind(item: Fibonacci) {
        binding.id.text = "#${item.id}"
        binding.content.text = item.value.toString()
    }
}
