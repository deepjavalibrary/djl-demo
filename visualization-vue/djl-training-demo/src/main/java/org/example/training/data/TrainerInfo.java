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
package org.example.training.data;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TrainerInfo {

	public enum State {Undefined, Training, Validating}
	private ModelInfo modelInfo;
	private int epoch;
	private int trainingProgress;
	private int validatingProgress;
	private BigDecimal speed;
	private State state;
	private String engine;
	private List<String> devices;
	private List<String> metricNames;
	private Map<String, List<MetricInfo>> metrics;
	private int metricsSize;

}
