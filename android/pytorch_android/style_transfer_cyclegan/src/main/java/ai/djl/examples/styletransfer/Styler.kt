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

import ai.djl.modality.cv.Image

class Styler {

    enum class Artist {
        CEZANNE, MONET, UKIYOE, VANGOGH
    }

    private val cezanne = StyleTransferModel(Artist.CEZANNE)
    private val monet = StyleTransferModel(Artist.MONET)
    private val ukiyoe = StyleTransferModel(Artist.UKIYOE)
    private val vangogh = StyleTransferModel(Artist.VANGOGH)

    private var currentArtist: Artist = Artist.CEZANNE

    fun apply(image: Image): Image {
        return when (currentArtist) {
            Artist.CEZANNE -> cezanne.style(image)
            Artist.MONET -> monet.style(image)
            Artist.UKIYOE -> ukiyoe.style(image)
            else -> vangogh.style(image)
        }
    }

    fun setCurrentArtist(artist: Artist) {
        currentArtist = artist
    }

    fun destroy() {
        cezanne.close()
        monet.close()
        ukiyoe.close()
        vangogh.close()
    }

}