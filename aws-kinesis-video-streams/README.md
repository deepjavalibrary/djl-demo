# KVS-DJL-demo

This is a simple demo of DJL with AWS KVS. The demo runs object detection on each frame in the video.

## App

To run the App demo, first create a KVS stream on an AWS account. Edit App.java and set the `REGION`
 and `STREAM_NAME` to your created stream.
 
 The App demo only retrieves from the stream, so you will also need to separately put media to the
 stream as well. You can use the PutMediaDemo from the [Kinesis Java SDK](https://github.com/awslabs/amazon-kinesis-video-streams-producer-sdk-java). Edit the Demo to use the correct region and stream
 as well. You may also want to edit the video as well, because the default video does not detect
 any objects. You can find some additional videos [here](https://github.com/aws/amazon-kinesis-video-streams-parser-library/tree/master/src/test/resources).
 Then, follow the instructions in the [README](https://github.com/awslabs/amazon-kinesis-video-streams-producer-sdk-java#examples) to setup credentials and put to the stream.
 
 Once data is in KVS, you can then run the main function in App. In order for App to work, you need
 to follow the same credential setup you used for the PutMediaDemo (see [here](https://github.com/awslabs/amazon-kinesis-video-streams-producer-sdk-java#examples)).
 
 The demo will print out what objects are detected through standard out. It will also save annotated
 frames in the `out` directory with boxes drawn over all detected objects.
 
 ## AppOnFile
 
 You can also look at this demo which runs object detection for each frame in a file. Before running
 the demo, edit the `FILENAME` to the path of the video you want to run on. Then, it will output the detection on each frame and output to the `out` directory like the main App.
