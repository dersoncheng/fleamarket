package com.zhangyu.fleamarket.http.protoapi;

import android.text.TextUtils;

import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.rpc.http.exception.HttpException;
import com.wandoujia.rpc.http.processor.Processor;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by niejunhong on 14-11-3.
 */
public class GZipProtoHttpResponseProcessor implements Processor<HttpResponse, byte[], HttpException> {
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String GZIP = "gzip";
  private static final String GB2312 = "text/html; charset=GB2312";

  @Override
  public byte[] process(HttpResponse httpResponse) throws HttpException {
    InputStream inputstream;
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      switch (statusCode) {
        case 200:
          try {
            inputstream = httpResponse.getEntity().getContent();
            Header header = httpResponse.getFirstHeader(CONTENT_ENCODING);
            if (header != null && GZIP.equals(header.getValue())) {
              inputstream = new GZIPInputStream(inputstream);
            }
            return IOUtils.readBytes(inputstream);
          } catch (IOException e) {
            throw new HttpException(statusCode, e.getMessage());
          }
        default:
          String message = null;
          try {
            return IOUtils.readBytes(httpResponse.getEntity().getContent());
          } catch (IOException e) {
            // ignore
          }
          if (TextUtils.isEmpty(message)) {
            message = httpResponse.getStatusLine().toString();
          }
          throw new HttpException(statusCode, message);
      }
    } finally {
      try {
        httpResponse.getEntity().consumeContent();
      } catch (IOException e) {
      }
    }
  }

}

