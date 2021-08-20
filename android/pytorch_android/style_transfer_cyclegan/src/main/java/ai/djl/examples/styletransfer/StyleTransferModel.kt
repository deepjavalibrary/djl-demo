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
import ai.djl.inference.Predictor
import ai.djl.modality.cv.Image
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDArrays
import ai.djl.ndarray.NDList
import ai.djl.ndarray.types.DataType
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ModelZoo
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext

class StyleTransferModel(artist: Styler.Artist) {

    private val model: ZooModel<Image, Image> = loadModel(artist)
    private val styler: Predictor<Image, Image> = model.newPredictor()

    private fun loadModel(artist: Styler.Artist): ZooModel<Image, Image> {
        val artistName = artist.toString().lowercase()
        val modelUrl =
            "https://mlrepo.djl.ai/model/cv/image_generation/ai/djl/pytorch/cyclegan/0.0.1/style_${artistName}.zip"

        val criteria = Criteria.builder()
            .setTypes(Image::class.java, Image::class.java)
            .optModelUrls(modelUrl)
            .optProgress(ProgressBar())
            .optTranslator(StyleTransferTranslator())
            .build()

        return ModelZoo.loadModel(criteria)
    }

    fun style(image: Image): Image {
        return styler.predict(image)
    }

    fun close() {
        styler.close()
        model.close()
    }

    internal inner class StyleTransferTranslator : Translator<Image?, Image> {

        override fun processInput(ctx: TranslatorContext, input: Image?): NDList {
            val image = switchFormat(input?.toNDArray(ctx.ndManager)).expandDims(0)
            return NDList(image.toType(DataType.FLOAT32, false))
        }

        override fun processOutput(ctx: TranslatorContext, list: NDList): Image {
            val output = list[0].addi(1).muli(128).toType(DataType.UINT8, false)
            return BitmapImageFactory.getInstance().fromNDArray(output.squeeze())
        }

        private fun switchFormat(array: NDArray?): NDArray {
            return NDArrays.stack(array?.split(3, 2)).squeeze()
        }

        override fun getBatchifier(): Batchifier? {
            return null
        }
    }
}