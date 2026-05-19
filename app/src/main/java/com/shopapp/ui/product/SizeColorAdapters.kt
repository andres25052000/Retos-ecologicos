package com.shopapp.ui.product

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shopapp.R
import com.shopapp.databinding.ItemColorBinding
import com.shopapp.util.ColorConstants

class SizeAdapter(
    private val sizes: List<String>,
    private var selectedSize: String,
    private val onSizeSelected: (String) -> Unit
) : RecyclerView.Adapter<SizeAdapter.ViewHolder>() {

    inner class ViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tv = LayoutInflater.from(parent.context).inflate(R.layout.item_size, parent, false) as TextView
        return ViewHolder(tv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val size = sizes[position]
        holder.tv.text = size
        val ctx = holder.tv.context
        if (size == selectedSize) {
            holder.tv.setBackgroundColor(ContextCompat.getColor(ctx, R.color.primary))
            holder.tv.setTextColor(ContextCompat.getColor(ctx, R.color.white))
        } else {
            holder.tv.setBackgroundColor(ContextCompat.getColor(ctx, R.color.surface_variant))
            holder.tv.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
        }
        holder.tv.setOnClickListener {
            selectedSize = size
            onSizeSelected(size)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = sizes.size
}

/**
 * Adapter de colores.
 * [colors] es una lista de NOMBRES (ej. "Rojo", "Azul marino").
 * Busca el HEX en ColorConstants para pintar el círculo.
 * Al seleccionar, devuelve el NOMBRE al callback.
 */
class ColorAdapter(
    private val colors: List<String>,
    private var selectedColor: String,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorName = colors[position]
        val hex = ColorConstants.hexForName(colorName)

        // Círculo coloreado con GradientDrawable (oval)
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            try { setColor(Color.parseColor(hex)) }
            catch (e: Exception) { setColor(Color.GRAY) }
        }
        holder.binding.colorView.background = circle

        // Nombre debajo del círculo
        holder.binding.tvColorName.text = colorName

        // Anillo de selección
        holder.binding.selectedRing.visibility =
            if (colorName == selectedColor) android.view.View.VISIBLE else android.view.View.GONE

        holder.binding.root.setOnClickListener {
            selectedColor = colorName
            onColorSelected(colorName)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = colors.size
}
