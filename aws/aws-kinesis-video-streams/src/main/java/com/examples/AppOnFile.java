package com.examples;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import com.amazonaws.kinesisvideo.parser.ebml.InputStreamParserByteSource;
import com.amazonaws.kinesisvideo.parser.mkv.MkvElementVisitException;
import com.amazonaws.kinesisvideo.parser.mkv.StreamingMkvReader;
import com.amazonaws.kinesisvideo.parser.utilities.FrameVisitor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class AppOnFile {
    private static final String FILENAME = "/path/to/video.mkv";

    public static void main(String[] args)
            throws IOException, ModelNotFoundException, MalformedModelException,
                    MkvElementVisitException {

        InputStream is = new FileInputStream(FILENAME);
        InputStreamParserByteSource ipbs = new InputStreamParserByteSource(is);
        StreamingMkvReader reader = StreamingMkvReader.createDefault(ipbs);

        File outDir = Paths.get("build/out").toFile();
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        for (File outFile : outDir.listFiles()) {
            outFile.delete();
        }

        FrameVisitor frameVisitor = FrameVisitor.create(new DjlImageVisitor());

        reader.apply(frameVisitor);
    }
}
