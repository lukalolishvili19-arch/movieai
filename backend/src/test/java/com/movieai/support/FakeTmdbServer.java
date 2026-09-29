package com.movieai.support;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/** Minimal in-process stand-in for the TMDB API, used by integration tests. */
public final class FakeTmdbServer {

    public record Response(int status, String body) {
    }

    public record RecordedRequest(String path, Map<String, String> query, String authorization) {
    }

    private final HttpServer server;
    private final Map<String, Deque<Response>> queued = new ConcurrentHashMap<>();
    private final Map<String, Response> routes = new ConcurrentHashMap<>();
    private final Map<String, java.util.function.Function<Map<String, String>, Response>> dynamic =
            new ConcurrentHashMap<>();
    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();

    private FakeTmdbServer(HttpServer server) {
        this.server = server;
    }

    public static FakeTmdbServer start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            FakeTmdbServer fake = new FakeTmdbServer(server);
            server.createContext("/", fake::handle);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            return fake;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/3";
    }

    public void reset() {
        routes.clear();
        dynamic.clear();
        queued.clear();
        requests.clear();
        on("/genre/movie/list", 200, TmdbFixtures.MOVIE_GENRES);
        on("/genre/tv/list", 200, TmdbFixtures.TV_GENRES);
    }

    /** Always answer {@code path} with the given response. */
    public void on(String path, int status, String body) {
        routes.put(path, new Response(status, body));
    }

    /** Answer {@code path} with a response computed from the query parameters. */
    public void onQuery(String path, java.util.function.Function<Map<String, String>, Response> handler) {
        dynamic.put(path, handler);
    }

    /** Answer the next call to {@code path} with this response, then fall back to {@link #on}. */
    public void once(String path, int status, String body) {
        queued.computeIfAbsent(path, p -> new ArrayDeque<>()).add(new Response(status, body));
    }

    public List<RecordedRequest> requests(String path) {
        return requests.stream().filter(r -> r.path().equals(path)).toList();
    }

    public int hits(String path) {
        return requests(path).size();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().replaceFirst("^/3", "");
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        requests.add(new RecordedRequest(path, query, exchange.getRequestHeaders().getFirst("Authorization")));
        Response response;
        Deque<Response> queue = queued.get(path);
        synchronized (this) {
            response = queue != null && !queue.isEmpty() ? queue.poll() : null;
        }
        if (response == null && dynamic.containsKey(path)) {
            response = dynamic.get(path).apply(query);
        }
        if (response == null) {
            response = routes.getOrDefault(path, new Response(404,
                    "{\"success\":false,\"status_code\":34,\"status_message\":\"The resource you requested could not be found.\"}"));
        }
        byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> query = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return query;
        }
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            query.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return query;
    }
}
