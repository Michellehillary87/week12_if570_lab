/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    init {
        imageUri = getImageUri(application.applicationContext)
    }
    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */

    private val workManager = WorkManager.getInstance(application)
    internal fun applyBlur(blurLevel: Int) {
        workManager.enqueue(OneTimeWorkRequest.from(BlurWorker::class.java))
            // Add WorkRequest to Cleanup temporary images
            var continuation = workManager
                .beginWith(OneTimeWorkRequest
                    .from(CleanupWorker::class.java))

            // Add WorkRequest to blur the image
            val blurRequest = OneTimeWorkRequest.Builder(BlurWorker::class.java)
                .setInputData(createInputDataForUri())
                .build()

            continuation = continuation.then(blurRequest)

            // Add WorkRequest to save the image to the filesystem
            val save = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java).build()

            continuation = continuation.then(save)

            // Actually start the work
            continuation.enqueue()
        }
    }
    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
            .build()
        return imageUri
    }

    internal fun setOutputUri(outputImageUri: String?) {
        var outputUri = uriOrNull(outputImageUri)
    }
    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
/**
 * Creates the input data bundle which includes the Uri to operate on
 * @return Data which contains the Image Uri as a String
 */
private fun createInputDataForUri(): Data {
    val builder = Data.Builder()
    val imageUri = null
    imageUri?.let {
        builder.putString(KEY_IMAGE_URI, imageUri.toString())
    }
    return builder.build()
}
internal fun applyBlur(blurLevel: Int) {

    val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
        .setInputData(createInputDataForUri())
        .build()
    val workManager = null
    workManager.enqueue(blurRequest)
}
    private fun Nothing?.enqueue(blurRequest: OneTimeWorkRequest) {}
// REPLACE THIS CODE:
// var continuation = workManager
// .beginWith(OneTimeWorkRequest
// .from(CleanupWorker::class.java))
// WITH