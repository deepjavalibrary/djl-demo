package one.entropy.demo.camel.djl;

import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.main.Main;

public class Demo extends EndpointRouteBuilder {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(Demo.class);
        main.run();
    }

    public void configure() throws Exception {
        from(undertow("http://0.0.0.0:8080/upload"))
            .process(exchange ->
                    exchange.getIn()
                        .setBody(exchange.getIn(AttachmentMessage.class)
                        .getAttachments().values().stream()
                        .findFirst().get().getInputStream()
                        .readAllBytes()))
            .to(djl("cv/image_classification")
                    .artifactId("ai.djl.mxnet:mlp:0.0.2"))
            .marshal().json(true);
    }
}
