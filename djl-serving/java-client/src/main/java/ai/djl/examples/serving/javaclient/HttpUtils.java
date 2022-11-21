/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package ai.djl.examples.serving.javaclient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {

    public static String postRequest(String url, Map<String, String> params, String contentType,
                                     String data, File file) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        if (params != null) {
            int i = 0;
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (i > 0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                i++;
            }
            url = sb.toString();
        }
        builder.uri(URI.create(url));

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        if (data != null) {
            builder.POST(HttpRequest.BodyPublishers.ofString(data));
        } else if (file != null) {
            builder.POST(HttpRequest.BodyPublishers.ofFile(file.toPath()));
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
