/*
 * Source adapted from AuthMe
 * https://github.com/axieum/authme/blob/main/LICENCE.txt
 */

package com.bawnorton.neruina.report;

import com.bawnorton.neruina.Neruina;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GitHub;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class GithubAuthManager {
    private static GitHub github;
    private static boolean authenticating = false;

    private static final String CLIENT_ID = "1907e7c3f988a98face9";
    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";

    private static final int PORT = 25595;

    public static CompletableFuture<GitHub> getOrLogin() {
        if(github != null) {
            return CompletableFuture.completedFuture(github);
        }
        CountDownLatch latch = new CountDownLatch(1);
        if(authenticating) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    latch.await();
                    return github;
                } catch (InterruptedException e) {
                    throw new CancellationException("Interrupted");
                }
            });
        }

        authenticating = true;
        return CompletableFuture.supplyAsync(() -> {
            try {
                Neruina.LOGGER.info("Logging into GitHub...");
                return getAuthorisationCode();
            } catch (Exception e) {
                Neruina.LOGGER.error("Failed to login to GitHub");
                throw new CompletionException(e);
            }
        }).thenApply(code -> {
            try {
                Neruina.LOGGER.info("Getting OAuth token...");
                return getOAuthToken(code);
            } catch (Exception e) {
                Neruina.LOGGER.error("Failed to get OAuth token");
                throw new CompletionException(e);
            }
        }).thenApply(oauthCode -> {
            try {
                Neruina.LOGGER.info("Authenticating with GitHub...");
                github = GitHub.connectUsingOAuth(oauthCode);
                Neruina.LOGGER.info("Successfully authenticated with GitHub");
                return github;
            } catch (IOException e) {
                Neruina.LOGGER.error("Failed to login to GitHub");
                throw new CompletionException(e);
            }
        }).whenComplete((a, b) -> {
            authenticating = false;
            latch.countDown();
        });
    }

    private static String getAuthorisationCode() throws IOException {
        String state = RandomStringUtils.randomAlphabetic(8);
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> code = new AtomicReference<>();
        server.createContext("/github/callback", exchange -> {
            Map<String, String> query = URLEncodedUtils.parse(exchange.getRequestURI(), StandardCharsets.UTF_8)
                    .stream()
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
            if(!state.equals(query.get("state"))) {
                return;
            }

            code.set(query.getOrDefault("code", null));

            byte[] message = "Successfully Authenticated. You can now close this tab.".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, message.length);
            exchange.getResponseBody().write(message);
            exchange.getResponseBody().close();

            latch.countDown();
        });

        try {
            URIBuilder builder = new URIBuilder(GITHUB_AUTH_URL)
                    .addParameter("scope", "public_repo")
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("state", state);
            URI uri = builder.build();
            Util.getOperatingSystem().open(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            Neruina.LOGGER.info("Begun listening on \"http://localhost:{}/github/callback\" for Github authentication...", PORT);
            server.start();

            latch.await();
            String authCode = code.get();
            if(authCode == null) {
                throw new IOException("Failed to authenticate with GitHub");
            }
            return authCode;
        } catch (InterruptedException e) {
            Neruina.LOGGER.warn("Github authentication was interrupted", e);
            throw new CancellationException("Interrupted");
        } finally {
            server.stop(2);
        }
    }

    private static String getOAuthToken(String code) {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            HttpPost request = createRequest(code);

            HttpResponse response = client.execute(request);
            JsonObject json = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
            return json.get("access_token").getAsString();
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to get OAuth token");
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static HttpPost createRequest(String code) {
        HttpPost request = new HttpPost(GITHUB_TOKEN_URL);
        request.setEntity(new UrlEncodedFormEntity(
                List.of(
                        new BasicNameValuePair("client_id", CLIENT_ID),
                        new BasicNameValuePair("client_secret", Storage.get()),
                        new BasicNameValuePair("code", code)
                ),
                StandardCharsets.UTF_8
        ));
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        return request;
    }
}
