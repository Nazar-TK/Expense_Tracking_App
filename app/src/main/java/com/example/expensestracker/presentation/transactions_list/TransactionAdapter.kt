package com.example.expensestracker.presentation.transactions_list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensestracker.databinding.DateItemBinding
import com.example.expensestracker.databinding.TransactionItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TransactionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_DATE = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

    private val itemList = arrayListOf<Any>()
    class DateViewHolder(private val headerBinding: DateItemBinding) : RecyclerView.ViewHolder(headerBinding.root) {
        fun bind(item: RecyclerItem.Header) {
            headerBinding.tvDate.text = item.date
        }
    }

    class TransactionViewHolder(private val itemBinding: TransactionItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: RecyclerItem.Item) {
            // Bind transaction data to ViewHolder
            itemBinding.tvTime.text = "Time: ${formatTime(item.transaction.date)}"
            itemBinding.tvBtcAmount.text = "Amount: ${item.transaction.btcAmount} BTC"

            if (item.transaction.category != null) {
                itemBinding.tvCategory.text = "Category: ${item.transaction.category}"
            } else {
                itemBinding.tvCategory.text = "Category: ${item.transaction.type}"
            }
        }
        private fun formatTime(dateTime: LocalDateTime): String {
            // Format time (e.g., "09:47")
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE -> DateViewHolder(DateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(TransactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateViewHolder -> holder.bind(itemList[position] as RecyclerItem.Header)
            is TransactionViewHolder -> holder.bind(itemList[position] as RecyclerItem.Item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int {
       return when(itemList[position]) {
           is RecyclerItem.Header -> VIEW_TYPE_DATE
           is RecyclerItem.Item -> VIEW_TYPE_TRANSACTION
           else -> throw IllegalArgumentException("Invalid Item")
       }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updatedList: List<Any>) {
        itemList.clear()
        itemList.addAll(updatedList)
        notifyDataSetChanged()
    }
}
