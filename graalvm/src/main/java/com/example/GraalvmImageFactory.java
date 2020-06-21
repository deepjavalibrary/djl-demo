/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.example;

import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public class GraalvmImageFactory extends BufferedImageFactory {

    @Override
    public Image fromFile(Path path) throws IOException {
        try {
            return fromImage(Imaging.getBufferedImage(path.toFile()));
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Image fromUrl(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return fromInputStream(is);
        }
    }

    @Override
    public Image fromInputStream(InputStream is) throws IOException {
        try {
            return fromImage(Imaging.getBufferedImage(is));
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void save(BufferedImage image, OutputStream os, String type) throws IOException {
        try {
            type = type.toUpperCase();
            if ("JPEG".equals(type) || "JPG".equals(type)) {
                throw new IllegalArgumentException("Unsupported format for write.");
            }
            ImageFormat format = ImageFormats.valueOf(type);
            Imaging.writeImage(image, os, format, null);
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }
}
