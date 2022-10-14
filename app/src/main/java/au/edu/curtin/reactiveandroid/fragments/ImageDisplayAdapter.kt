package au.edu.curtin.reactiveandroid.fragments

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import au.edu.curtin.reactiveandroid.R
import kotlinx.coroutines.selects.select

class ImageDisplayAdapter(private val data: ArrayList<Bitmap>,
                          private val selected: ArrayList<Int>,
                          private val listener: OnItemClickListener
): RecyclerView.Adapter<ImageDisplayAdapter.ImageDisplayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDisplayViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.image_display_item, parent, false)
        return ImageDisplayViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageDisplayViewHolder, position: Int) {
        val currentItem = data[position]
        var selected = position in selected

        holder.bind(currentItem, selected)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ImageDisplayViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var checkBtnImage: ImageView
        private var picture: ImageView

        init {
            checkBtnImage = itemView.findViewById(R.id.checkBoxImage)
            picture = itemView.findViewById(R.id.pictureId)

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {

            if (checkBtnImage.visibility == View.INVISIBLE) {
                checkBtnImage.visibility = View.VISIBLE
            } else {
                checkBtnImage.visibility = View.INVISIBLE
            }

            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

        fun bind(image: Bitmap, selected: Boolean) {
            picture.setImageBitmap(image)

            if (selected) {
                checkBtnImage.visibility = View.VISIBLE
            } else {
                checkBtnImage.visibility = View.INVISIBLE
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}