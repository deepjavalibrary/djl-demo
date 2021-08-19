/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.examples.styletransfer

import ai.djl.android.core.BitmapImageFactory
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class StyleTransferActivity : AppCompatActivity() {

    private var styler: Styler? = null
    private var imageView: ImageView? = null

    private companion object {
        const val CAMERA_PERMISSION_CODE = 1
        const val REQUEST_IMAGE_CAPTURE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.style)

        Thread {
            styler = Styler()
        }.start()

        setupArtists()

        val capture = findViewById<ImageButton>(R.id.capture)
        capture.setOnClickListener {
            launchCamera()
        }
    }

    private fun setupArtists() {
        val artists = findViewById<Spinner>(R.id.artists)
        artists.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, Styler.Artist.values())

        artists.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val artist = parent.getItemAtPosition(position).toString()
                styler?.setCurrentArtist(Styler.Artist.valueOf(artist))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun launchCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraActivity()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun startCameraActivity() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity()
            } else {
                Toast.makeText(this, "You denied Camera permissions.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val styledBitmap = processImage(imageBitmap)
            imageView?.setImageBitmap(styledBitmap)
        }
    }

    private fun processImage(bitmap: Bitmap): Bitmap {
        val image = BitmapImageFactory.getInstance().fromImage(bitmap)
        return styler?.apply(image)?.wrappedImage as Bitmap
    }

    override fun onDestroy() {
        styler?.destroy()
        super.onDestroy()
    }
}