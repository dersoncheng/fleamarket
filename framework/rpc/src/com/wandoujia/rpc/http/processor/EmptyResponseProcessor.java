package com.wandoujia.rpc.http.processor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;

import com.wandoujia.rpc.http.exception.HttpException;

/**
 * Processor who just returns success or fail info.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 */
public class EmptyResponseProcessor implements Processor<HttpResponse, Void, ExecutionException> {

  @Override
  public Void process(HttpResponse httpResponse) throws ExecutionException {
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      switch (statusCode) {
        case 200:
          return null;
        default:
          throw new ExecutionException(
            new HttpException(statusCode, httpResponse.getStatusLine().toString()));
      }
    } finally {
      try {
        httpResponse.getEntity().consumeContent();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
