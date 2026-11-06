package homework2.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import homework2.parser.WikiParser;
import homework2.search.WikiConfig;

/**
 * Выполняет HTTP-запрос к MediaWiki API и передаёт тело ответа
 * в {@link WikiParser} для разбора.
 */
public class WikiFetcher {

    private static final String API_URL = WikiConfig.API_URL;
    private static final String USER_AGENT = WikiConfig.USER_AGENT;

    private final HttpClient httpClient;
    private final WikiParser parser;

    public WikiFetcher() {
        this(HttpClient.newHttpClient(), new WikiParser());
    }

    /** Конструктор для внедрения зависимостей (удобно для тестов). */
    public WikiFetcher(HttpClient httpClient, WikiParser parser) {
        this.httpClient = httpClient;
        this.parser = parser;
    }

    /**
     * Выполняет поиск в Википедии по строке пользователя.
     *
     * @param query поисковый запрос
     * @return список результатов
     * @throws Exception при сетевой ошибке или не-200 ответе
     */
    public List<WikiParser.SearchResult> search(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = API_URL
                + "?action=query"
                + "&list=search"
                + "&srsearch=" + encodedQuery
                + "&format=json"
                + "&srlimit=10"
                + "&srprop=snippet";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Wikipedia API вернул статус " + response.statusCode());
        }

        return parser.parse(response.body());
    }

    public static void main(String[] args) throws Exception {
        WikiFetcher fetcher = new WikiFetcher();
        for (WikiParser.SearchResult r : fetcher.search("Albert Einstein")) {
            System.out.println(r.getTitle());
            System.out.println("  " + r.getSnippet());
        }
    }
}
