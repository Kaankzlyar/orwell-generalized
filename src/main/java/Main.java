import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://app.parlamento.pt/webutils/docs/doc.xml?path=N3jRPJFnnPw07UGV%2f4k0mwzgbG2Jl3wjBTiC8TM1giRsecyPDp4yf3k8FPfa4jKxK8cK%2bHg9qZBa2M5gomJKy%2fQtnk5tb5Z5r8BVFPJMhxvRcyvUOUPpY%2bt87ZuUHz9BvAd789KDSyluIGUKEnTFo9Dpn1Ykljq4eeqISeeUTJT5D1DUC%2f%2biDqKA553hfRTTsEe86A1ObPNZtwCqvv05dxSywmi%2ftOnvfCrhUXy1NfIbEKmFTBEf0i6Ucx0Up0bYMVphP5VomK8C%2bxF7onHEFOpMyRuY9v81e%2bKpmj%2fFjokJsrEnU%2baxPEeUTE9%2bmeOp2g6zC2kPSEZAn7%2fJWt4sioTjDjQ20aK58Q4QBUcFIkw%3d&fich=InformacaoBaseXV.xml&Inline=true"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        XMLObject object = new XMLObject(response.body(), true);

        object.printElementValues();
    }
}