package au.edu.curtin.reactiveandroid

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.curtin.reactiveandroid.controllers.ImagesController
import au.edu.curtin.reactiveandroid.fragments.OneColFragment
import au.edu.curtin.reactiveandroid.fragments.TwoColFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var loadImageBtn: Button
    private lateinit var changeViewBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var searchText: EditText
    private var imageController = ImagesController()
    private var oneColFragment = OneColFragment(imageController)
    private var twoColFragment = TwoColFragment(imageController)
    private var currentFragment = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        loadImageBtn = findViewById(R.id.loadImageBtn)
        changeViewBtn = findViewById(R.id.changeViewBtn)
        uploadBtn = findViewById(R.id.uploadBtn)
        progressBar = findViewById(R.id.progressBarId)
        searchText = findViewById(R.id.inputSearch)

        progressBar.visibility = View.INVISIBLE
        loadImageBtn.setOnClickListener {
            searchImage()
        }

        changeViewBtn.setOnClickListener {
            changeDisplays()
        }

        uploadBtn.setOnClickListener {
            saveImages()
        }
    }

    fun searchImage() {
        Toast.makeText(this@MainActivity, "Searching starts", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.VISIBLE
        val searchTask = SearchTask(this)
        searchTask.setSearchkey(searchText.text.toString())

        var searchObservable: Single<String> = Single.fromCallable(searchTask)
        searchObservable = searchObservable.subscribeOn(Schedulers.io())
        searchObservable = searchObservable.observeOn(AndroidSchedulers.mainThread())
        searchObservable.subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {}
            override fun onSuccess(s: String) {
                Toast.makeText(this@MainActivity, "Searching Ends", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
                loadImage(s)
            }

            override fun onError(e: Throwable) {
                Toast.makeText(this@MainActivity, "Searching Error", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }
        })
    }

    fun loadImage(response: String?) {
        Log.d("KEVIN", response!!)
        val imageRetrievalTask = ImageRetrievalTask(this)
        imageRetrievalTask.setData(response)
        Toast.makeText(this, "Image loading starts", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.VISIBLE
        var searchObservable = Observable.fromCallable(imageRetrievalTask)
        searchObservable = searchObservable.subscribeOn(Schedulers.io())
        searchObservable = searchObservable.observeOn(AndroidSchedulers.mainThread())
        searchObservable.subscribe(object : Observer<ArrayList<Bitmap>> {
            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Image loading error, search again",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.INVISIBLE
            }

            override fun onComplete() {}
            override fun onNext(t: ArrayList<Bitmap>) {
                for (image in t) {
                    imageController.addImage(image)
                }
                Toast.makeText(this@MainActivity, "Image loading Ends", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.imageDisplayFragment, oneColFragment)
                    commit()
                }
            }
        })
    }

    fun changeDisplays() {
        if (currentFragment == 1) {
            currentFragment = 2
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.imageDisplayFragment, twoColFragment)
                commit()
            }
        } else {
            currentFragment = 1
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.imageDisplayFragment, oneColFragment)
                commit()
            }
        }
    }

    fun uploadImages() {

        val imagesList = imageController.getToUploadList()
        val outputStream = ByteArrayOutputStream()
        for (image in imagesList) {
            var imageReference = storageReference.child(image.toString())

            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val data = outputStream.toByteArray()

            var uploadTask = imageReference.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(this@MainActivity, "Image Upload Failed", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Image Upload Success", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun saveImages() {

        for (image in imageController.getToUploadList()) {
            saveImage(image, this, "ReactiveAndroid")
        }
    }



    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            Log.d("KEVINTEST", "We Here!")
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            Log.d("KEVINTEST", uri.toString())
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"

            Log.d("KEVINTEST", "Filename:$fileName")
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                Log.d("KEVINTEST", "File Absolute Path:${ file.absolutePath }")
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}