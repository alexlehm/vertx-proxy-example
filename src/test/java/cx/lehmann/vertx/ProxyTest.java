package cx.lehmann.vertx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ProxyTest {

  private static final Logger log = LoggerFactory.getLogger(ProxyTest.class);

  private Vertx vertx = Vertx.vertx();

  HttpServer server;

  @Test
  public void test(TestContext context) {
    log.info("test starting");
    Async async = context.async();

    HttpClientOptions options = new HttpClientOptions().setSsl(true).setTrustAll(true).setDefaultPort(4443)
        .setProxyOptions(new ProxyOptions().setHost("localhost").setPort(8080).setUsername("user").setPassword("pw"));

    HttpClient client = vertx.createHttpClient(options);

    client.get("/", resp -> {
      log.info("response code: " + resp.statusCode());
      resp.bodyHandler(data -> {
        log.info("body text: " + data.toString());
        context.assertEquals("this is the reply", data.toString());
      });
      resp.exceptionHandler(th -> context.fail(th));
      resp.endHandler(v -> async.complete());
    }).exceptionHandler(th -> context.fail(th)).end();
  }

  @Before
  public void startServers(TestContext context) {
    Async async1 = context.async();

    JksOptions jksOptions = new JksOptions().setPath("keystore.jks").setPassword("password");
    HttpServerOptions options = new HttpServerOptions().setHost("localhost").setPort(4443).setSsl(true)
        .setKeyStoreOptions(jksOptions);
    server = vertx.createHttpServer(options);
    server.requestHandler(request -> request.response().end("this is the reply"));
    server.listen(v -> async1.complete());

  }

  @After
  public void stopServers(TestContext context) {
    server.close();
  }

}
