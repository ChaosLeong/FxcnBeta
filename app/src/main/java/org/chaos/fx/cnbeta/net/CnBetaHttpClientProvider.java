/*
 * Copyright 2016 Chaos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chaos.fx.cnbeta.net;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Chaos
 *         6/29/16
 */

class CnBetaHttpClientProvider {

    static OkHttpClient newCnBetaHttpClient() {
        return new OkHttpClient.Builder()
                .cookieJar(new WebCookieJar())
                .addNetworkInterceptor(new RedirectsInterceptor())
                .addInterceptor(new HeaderInterceptor())
                .build();
    }

    private static class WebCookieJar implements CookieJar {

        private static final String TAG = "WebCookieJar";

        private Map<String, List<Cookie>> mCookies = new ArrayMap<>();

        @Override
        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
            String url = httpUrl.toString();
            if (url.startsWith(WebApi.COMMENT_JSON_URL)/* read comment*/) {
                // 由于只能存在一个正文页面，所以保存最新的页面的 Cookies 便足够
                mCookies.put(WebApi.COMMENT_JSON_URL, cookies);
            } else if (url.startsWith(WebApi.CAPTCHA_URL)) {
                // 获取验证码时会返回 key 为 'PHPSESSID' 的 cookie，
                // 提交评论需要将 'PHPSESSID' 返回至服务端
                List<Cookie> oldCookies = mCookies.get(WebApi.COMMENT_JSON_URL);
                List<Cookie> newCookies = new ArrayList<>(oldCookies);
                newCookies.addAll(cookies);
                mCookies.put(WebApi.COMMENT_JSON_URL, newCookies);
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
            String url = httpUrl.toString();
            if (url.startsWith(WebApi.CAPTCHA_BASE_URL) /* for picasso load captcha */
                    || url.startsWith(WebApi.COMMENT_BASE_URL + "/do")) {
                List<Cookie> cookies = mCookies.get(WebApi.COMMENT_JSON_URL);
                if (cookies != null) {
                    return cookies;
                }
                Log.w(TAG, "loadForRequest: return an empty cookies list for comment.");
            }
            return Collections.emptyList();
        }
    }

    private static class RedirectsInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            // cnBeta 改版后原 link 会报 301 直接挑战至新页面，由于新页面 url
            // 比旧 url 多了个 {topic}，暂未知参数从何而来。所以目前需要借助 OkHttp
            // 将 301 转为 308，这样才能获取跳转后的页面数据并传递给 Retrofit，
            // 301 转 308 相关请查看：https://github.com/square/retrofit/issues/1690
            if (response.code() == HttpURLConnection.HTTP_MOVED_PERM) {
                response = new Response.Builder()
                        .request(response.request())
                        .protocol(response.protocol())
                        .code(308)
                        .message(response.message())
                        .handshake(response.handshake())
                        .headers(response.headers())
                        .body(response.body())
                        .networkResponse(response.networkResponse())
                        .cacheResponse(response.cacheResponse())
                        .priorResponse(response.priorResponse())
                        .sentRequestAtMillis(response.sentRequestAtMillis())
                        .receivedResponseAtMillis(response.receivedResponseAtMillis())
                        .build();
            }
            return response;
        }
    }

    private static class HeaderInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            String url = chain.request().url().toString();
            Request request;
            if (url.startsWith(WebApi.HOST_URL + "/articles")
                    || url.startsWith(WebApi.HOT_HOST_URL + "/articles")) {
                request = chain.request();
            } else {
                Request.Builder builder = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .addHeader("Origin", "http://www.cnbeta.com")
                        .addHeader("Referer", "http://www.cnbeta.com/")
                        .addHeader("X-Requested-With", "XMLHttpRequest");
                request = builder.build();
            }
            return chain.proceed(request);
        }
    }
}
