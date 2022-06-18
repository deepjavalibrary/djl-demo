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
package com.examples;

import ai.djl.modality.Classifications;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HandlerTest {

    @Test
    public void invokeTest() throws IOException {
        Context context = new MockContext();
        Handler handler = new Handler();

        Request request = new Request();
        request.setInputImageUrl("https://djl-ai.s3.amazonaws.com/resources/images/kitten.jpg");
        Gson gson = new Gson();
        byte[] buf = gson.toJson(request).getBytes(StandardCharsets.UTF_8);

        InputStream is = new ByteArrayInputStream(buf);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        handler.handleRequest(is, os, context);
        String result = os.toString(StandardCharsets.UTF_8.name());

        Type type = new TypeToken<List<Classifications.Classification>>() {}.getType();
        List<Classifications.Classification> list = gson.fromJson(result, type);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.get(0).getClassName(), "n02123045 tabby, tabby cat");
    }
}
