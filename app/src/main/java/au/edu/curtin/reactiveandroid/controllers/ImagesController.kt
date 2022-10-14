package au.edu.curtin.reactiveandroid.controllers

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.log

class ImagesController {

    private var imagesList = ArrayList<Bitmap>()
    private var toUploadList = ArrayList<Bitmap>()
    private var selectedPositions = ArrayList<Int>()

    fun addImage(image: Bitmap) {
        this.imagesList.add(image)
    }

    fun addToUploadList(image: Bitmap) {
        Log.d("KEVIN", "Add to Upload List Before: ${ this.toUploadList.count().toString() }")
        this.toUploadList.add(image)
        Log.d("KEVIN", "Add to Upload List After: ${ this.toUploadList.count().toString() }")
    }

    fun removeFromUploadList(image: Bitmap) {
        Log.d("KEVIN", "Remove Upload List Before: ${ this.toUploadList.count().toString() }")
        this.toUploadList.remove(image)
        Log.d("KEVIN", "Remove Upload List After: ${ this.toUploadList.count().toString() }")
    }

    fun getImageList(): ArrayList<Bitmap> {
        return this.imagesList
    }

    fun getToUploadList(): ArrayList<Bitmap> {
        return this.toUploadList
    }

    fun addSelectedImage(position: Int) {
        this.selectedPositions.add(position)
    }

    fun removeSelectedImage(position: Int) {
        this.selectedPositions.remove(position)
    }

    fun getSelectedPositions(): ArrayList<Int> {
        return this.selectedPositions
    }
}