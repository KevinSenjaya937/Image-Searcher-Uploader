package au.edu.curtin.reactiveandroid.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.curtin.reactiveandroid.R
import au.edu.curtin.reactiveandroid.controllers.ImagesController

class OneColFragment(private val controller: ImagesController) : Fragment(), ImageDisplayAdapter.OnItemClickListener {

    private lateinit var adapter: ImageDisplayAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageList: ArrayList<Bitmap>
    private lateinit var selectedPositions: ArrayList<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_col, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.imageList = controller.getImageList()
        this.selectedPositions = controller.getSelectedPositions()

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        recyclerView = view.findViewById(R.id.oneColRecycler)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        adapter = ImageDisplayAdapter(imageList, selectedPositions,this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(position: Int) {
        val clickedImage = this.imageList[position]
        val alreadySelected = position in controller.getSelectedPositions()

        if (alreadySelected) {
            controller.removeSelectedImage(position)
            controller.removeFromUploadList(clickedImage)
        } else {
            controller.addToUploadList(clickedImage)
            controller.addSelectedImage(position)
        }
    }
}