package com.emberiot.emberiot.devices

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.R
import com.google.android.material.textfield.TextInputEditText

class NewValueListAdapter(private val view: RecyclerView) : RecyclerView.Adapter<NewValueListAdapter.Companion.ViewHolder>() {

    val currentList = mutableListOf<String>()

    companion object  {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemId: TextView = itemView.findViewById(R.id.valueItemId)
            val textInputEditText: TextInputEditText = itemView.findViewById(R.id.valueEditText)
            val deleteButton: ImageButton = itemView.findViewById(R.id.deleteValueBtn)
            var textWatcher: TextWatcher? = null
            val handler = Handler(Looper.getMainLooper())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.property_dialog_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= currentList.size) {
            return
        }

        val item = currentList[position]

        holder.itemId.text = position.toString()
        val currentText = holder.textInputEditText.text.toString()
        if (currentText != item) {
            holder.textInputEditText.setText(item)
            holder.textInputEditText.setSelection(holder.textInputEditText.text.toString().length)
        }

        holder.deleteButton.setOnClickListener {
            holder.handler.removeCallbacksAndMessages(null)
            holder.itemView.post {
                holder.textWatcher?.let { holder.textInputEditText.removeTextChangedListener(it) }
                currentList.remove(item)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, currentList.size - position)
            }
        }

        holder.textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                val pos = holder.bindingAdapterPosition

                if (newText != currentList[pos]) {
                    holder.handler.removeCallbacksAndMessages(null)

                    holder.handler.postDelayed({
                        currentList[pos] = newText
                        notifyItemChanged(pos)
                    }, 1000)
                }
            }
        }

        holder.textInputEditText.addTextChangedListener(holder.textWatcher)
    }
}