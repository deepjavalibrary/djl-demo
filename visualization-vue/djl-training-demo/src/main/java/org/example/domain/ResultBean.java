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
package org.example.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String value;
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public ResultBean(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static ResultBean success() {
        return new ResultBean(0, "Success");
    }

    public static ResultBean failure() {
        return new ResultBean(-1, "Failure");
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public ResultBean add(String key, Object value) {
        data.put(key, value);
        return this;
    }
}
