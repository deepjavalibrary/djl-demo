package com.examples;

import ai.djl.ModelException;
import ai.djl.util.Utils;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.kinesisvideo.parser.examples.GetMediaWorker;
import com.amazonaws.kinesisvideo.parser.utilities.FrameVisitor;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideo;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoClientBuilder;
import com.amazonaws.services.kinesisvideo.model.StartSelector;
import com.amazonaws.services.kinesisvideo.model.StartSelectorType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final String STREAM_NAME = "testKvs";
    private static final Regions REGION = Regions.US_EAST_1;

    public static void main(String[] args) throws IOException, ModelException {
        AmazonKinesisVideoClientBuilder amazonKinesisVideoBuilder =
                AmazonKinesisVideoClientBuilder.standard();
        amazonKinesisVideoBuilder.setRegion(REGION.getName());
        amazonKinesisVideoBuilder.setCredentials(new SystemPropertiesCredentialsProvider());
        AmazonKinesisVideo amazonKinesisVideo = amazonKinesisVideoBuilder.build();

        Path outDir = Paths.get("build/out");
        Utils.deleteQuietly(outDir);
        Files.createDirectories(outDir);

        FrameVisitor frameVisitor = FrameVisitor.create(new DjlImageVisitor());

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        GetMediaWorker getMediaWorker =
                GetMediaWorker.create(
                        REGION,
                        new SystemPropertiesCredentialsProvider(),
                        STREAM_NAME,
                        new StartSelector().withStartSelectorType(StartSelectorType.NOW),
                        amazonKinesisVideo,
                        frameVisitor);
        executorService.submit(getMediaWorker);
    }
}
