package com.example.dataset;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class S3ImageDataset implements Serializable {

    private static final ImageFactory FACTORY = ImageFactory.getInstance();

    private static S3ImageDataset dataset;

    private String bucketName;
    private String prefix;
    private transient S3Client s3;

    private S3ImageDataset(String bucketName, String prefix, S3Client s3) {
        this.bucketName = bucketName;
        this.prefix = prefix;
        this.s3 = s3;
    }

    public static synchronized S3ImageDataset getInstance(
            String bucketName, String prefix, S3Client s3) {
        if (dataset == null) {
            dataset = new S3ImageDataset(bucketName, prefix, s3);
        }
        return dataset;
    }

    public List<List<Image>> listImages(int batchSize) {
        if (s3 == null) {
            s3 = S3Client.create();
        }

        List<List<String>> images = new ArrayList<>();
        ListObjectsRequest req =
                ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).build();

        ListObjectsResponse resp = s3.listObjects(req);
        List<String> batch = new ArrayList<>(batchSize);
        for (S3Object obj : resp.contents()) {
            String key = obj.key();
            if (key.endsWith(".jpg")) {
                batch.add(key);
            }
            if (batch.size() == batchSize) {
                images.add(batch);
                batch = new ArrayList<>(batchSize);
            }
        }
        if (!batch.isEmpty()) {
            images.add(batch);
        }

        return new DeferredList(images);
    }

    public Image downloadImage(String keyName) throws IOException {
        GetObjectRequest req = GetObjectRequest.builder().bucket(bucketName).key(keyName).build();
        try (ResponseInputStream<GetObjectResponse> is = s3.getObject(req)) {
            return FACTORY.fromInputStream(is);
        }
    }

    private class DeferredList extends AbstractList<List<Image>> {

        private List<List<String>> list;

        public DeferredList(List<List<String>> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public List<Image> get(int index) {
            List<String> item = list.get(index);
            List<Image> ret = new ArrayList<>(item.size());
            for (String key : item) {
                try {
                    ret.add(downloadImage(key));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to download image: " + key);
                }
            }
            return ret;
        }
    }
}
