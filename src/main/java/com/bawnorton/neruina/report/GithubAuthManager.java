/*
 * Source adapted from AuthMe
 * https://github.com/axieum/authme/blob/main/LICENCE.txt
 */

package com.bawnorton.neruina.report;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.exception.AbortedException;
import com.bawnorton.neruina.exception.InProgressException;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.thread.AbortableCountDownLatch;
import com.bawnorton.neruina.version.Texter;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
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
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

//? if >=1.21.11 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
 *///?}

public final class GithubAuthManager {
	private static final String CLIENT_ID = "1907e7c3f988a98face9";
	private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
	private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";

	private static final int PORT = 25595;

	private static final Map<UUID, LoginRecord> logins = new HashMap<>();

	public static boolean cancelLogin(ServerPlayer player) {
		LoginRecord record = logins.get(player.getUUID());
		if (record == null || !record.inProgress) {
			return false;
		}

		record.authLatch.abort();
		return true;
	}

	public static CompletableFuture<GitHub> getOrLogin(ServerPlayer player) {
		MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
		LoginRecord record = logins.computeIfAbsent(player.getUUID(), uuid -> new LoginRecord());
		if (record.gitHub != null) {
			messageHandler.sendToPlayer(
					player,
					Texter.translatable("commands.neruina.report.reporting"),
					false
			);

			return CompletableFuture.completedFuture(record.gitHub);
		}

		if (record.inProgress) {
			return CompletableFuture.failedFuture(new InProgressException("Login already in progress"));
		}

		messageHandler.sendToPlayer(
				player,
				Texter.translatable("commands.neruina.report.processing"),
				false,
				messageHandler.generateCancelLoginAction(player)
		);

		record.inProgress = true;
		record.authLatch = new AbortableCountDownLatch(1);
		record.loginLatch = new CountDownLatch(1);

		return CompletableFuture.supplyAsync(() -> {
			try {
				Neruina.LOGGER.info("Logging into GitHub...");
				return getAuthorisationCode(record);
			} catch (Exception e) {
				Neruina.LOGGER.error("Failed to login to GitHub");
				throw new CompletionException(e);
			}
		}).thenApply(code -> {
			if (code == null) {
				throw new CancellationException("Authentication timed out");
			}
			try {
				Neruina.LOGGER.info("Getting OAuth token...");
				return getOAuthToken(code);
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}).thenApply(oauthCode -> {
			try {
				Neruina.LOGGER.info("Authenticating with GitHub...");
				record.gitHub = GitHub.connectUsingOAuth(oauthCode);
				Neruina.LOGGER.info("Successfully Logged into GitHub as {}", record.gitHub.getMyself().getLogin());

				messageHandler.sendToPlayer(player,
						Texter.translatable("commands.neruina.report.authenticated"),
						false
				);

				return record.gitHub;
			} catch (IOException e) {
				Neruina.LOGGER.error("Failed to authenticate with GitHub");
				throw new CompletionException(e);
			}
		}).whenComplete((a, b) -> {
			record.inProgress = false;
			record.loginLatch.countDown();
		});
	}

	private static String getAuthorisationCode(LoginRecord record) throws IOException, AbortedException {
		String state = RandomStringUtils.randomAlphabetic(8);
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

		AtomicReference<String> code = new AtomicReference<>();
		server.createContext("/github/callback", exchange -> {
			Map<String, String> query = URLEncodedUtils.parse(exchange.getRequestURI(), StandardCharsets.UTF_8.toString())
					.stream()
					.collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
			if (!state.equals(query.get("state"))) {
				return;
			}

			code.set(query.getOrDefault("code", null));

			byte[] message = "Successfully Authenticated. You can now close this tab.".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, message.length);
			exchange.getResponseBody().write(message);
			exchange.getResponseBody().close();

			record.authLatch.countDown();
		});

		try {
			URIBuilder builder = new URIBuilder(GITHUB_AUTH_URL)
					.addParameter("scope", "public_repo gist")
					.addParameter("client_id", CLIENT_ID)
					.addParameter("state", state);
			URI uri = builder.build();
			Util.getPlatform().openUri(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		try {
			Neruina.LOGGER.info("Begun listening on \"http://localhost:{}/github/callback\" for Github authentication...", PORT);
			server.start();

			boolean success = record.authLatch.await(5, TimeUnit.MINUTES);
			if (!success) {
				return null;
			}
			String authCode = code.get();
			if (authCode == null) {
				throw new AbortedException("Failed to authenticate with GitHub");
			}
			return authCode;
		} catch (AbortedException e) {
			throw e;
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
			JsonObject json = GsonHelper.parse(EntityUtils.toString(response.getEntity()));
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

	static class LoginRecord {
		private CountDownLatch loginLatch;
		private AbortableCountDownLatch authLatch;
		private boolean inProgress;
		private @Nullable GitHub gitHub;
	}
}
