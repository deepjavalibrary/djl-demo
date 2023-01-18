module org.examples.module {
    exports org.examples.module;

    requires jdk.crypto.cryptoki;
    requires org.slf4j;
    requires org.apache.commons.compress;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.slf4j;
    requires com.google.gson;
    requires com.sun.jna;
    requires ai.djl.api;
    requires ai.djl.pytorch_engine;
    requires ai.djl.pytorch_model_zoo;
    requires ai.djl.opencv;
}
