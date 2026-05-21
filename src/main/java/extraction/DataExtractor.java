package extraction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static config.Config.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class DataExtractor {

    public void extract() {
        Path sourcePath = SOURCE_PATH();
        List<SourceNode> sources = parseSources(sourcePath);
        storeData(sources);
    }

    protected abstract List<SourceNode> parseSources(Path sourcePath);

    protected abstract Path SOURCE_PATH();

    protected abstract String getName();

    private void storeData(List<SourceNode> sources) {
        try {
            Path sourceDir = TMP_DIR.resolve(DATA_DIR).resolve(getName().toLowerCase());
            Files.createDirectories(sourceDir);

            List<DownloadTask> tasks = collectDownloads(sourceDir, sources);

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .sslContext(trustAllSslContext())
                    .build();

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();
                for (DownloadTask task : tasks) {
                    futures.add(executor.submit(() -> {
                        try {
                            System.out.println('[' + getName() + " Extractor] Downloading: " + task.target() + task.key());
                            HttpResponse<byte[]> response = fetchData(httpClient, task.uri());
                            String format = inferFormat(response);
                            Files.write(task.target().resolve(task.key() + format), response.body());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
                for (var future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        for (var f : futures) {
                            f.cancel(true);
                        }
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store data: " + e.getMessage(), e);
        }
    }

    private List<DownloadTask> collectDownloads(Path parentDir, List<SourceNode> nodes) throws IOException {
        List<DownloadTask> tasks = new ArrayList<>();
        for (SourceNode node : nodes) {
            switch (node) {
                case SourceNode.SourceValue sv -> tasks.add(new DownloadTask(parentDir, sv.key(), sv.uri()));
                case SourceNode.SourceObject so -> {
                    Path dir = parentDir.resolve(so.key());
                    Files.createDirectories(dir);
                    tasks.addAll(collectDownloads(dir, so.children()));
                }
            }
        }
        return tasks;
    }

    /**
     * Infers the file format by the Content-Type http header
     * @param response
     * @return
     */
    private String inferFormat(HttpResponse<byte[]> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        return switch (contentType) {
            case String ct when ct.contains("application/xml") || ct.contains("text/xml") -> ".xml";
            case String ct when ct.contains("text/csv") -> ".csv";
            case String ct when ct.contains("application/json") -> ".json";
            default -> "";
        };
    }

    /**
    * Trusts all SSL certificates so it can run on the FEUP network§
    * @return
 */
    private static SSLContext trustAllSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            }, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create trust-all SSLContext", e);
        }
    }

    private static HttpResponse<byte[]> fetchData(HttpClient httpClient, URI uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(90))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Failed to download " + uri + ": HTTP " + status);
            }
            return response;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download " + uri + ": " + e.getMessage(), e);
        }
    }
}
