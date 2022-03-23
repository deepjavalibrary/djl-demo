module org.examples.module {
    exports org.examples.module;

    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.commons.compress;
    requires com.google.gson;
    requires com.sun.jna;
    requires ai.djl.api;
    requires org.bytedeco.javacpp;
}
