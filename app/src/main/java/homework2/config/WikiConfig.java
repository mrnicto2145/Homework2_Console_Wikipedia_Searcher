package homework2.config;
import io.github.cdimascio.dotenv.Dotenv;

public class WikiConfig {
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String API_URL, USER_AGENT;
    public static final int MAX_RESULTS, MAX_LINE_LENGTH, MIN_LINE_LENGTH, DEFAULT_PAGE_SIZE, FETCH_TIMEOUT;

    static {
        API_URL = DOTENV.get("WIKI_API_URL", "https://ru.wikipedia.org/w/api.php");
        MAX_RESULTS = Integer.parseInt(DOTENV.get("MAX_SEARCH_RESULTS", "10"));
        MAX_LINE_LENGTH = Integer.parseInt(DOTENV.get("MAX_LINE_LENGTH", "300"));
        MIN_LINE_LENGTH = Integer.parseInt(DOTENV.get("MIN_LINE_LENGTH", "2"));
        DEFAULT_PAGE_SIZE = Integer.parseInt(DOTENV.get("DEFAULT_PAGE_SIZE", "10"));
        USER_AGENT = DOTENV.get("USER_AGENT", "WikiFetcher/1.0 (https://example.org; contact@example.org)");
        FETCH_TIMEOUT = Integer.parseInt(DOTENV.get("FETCH_TIMEOUT", "15"));
    }
}