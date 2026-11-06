package homework2.search;
import io.github.cdimascio.dotenv.Dotenv;

public class WikiConfig {
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String API_URL;
    public static final int MAX_RESULTS, MAX_LINE_LENGTH, MIN_LINE_LENGTH;

    static {
        API_URL = DOTENV.get("WIKI_API_URL", "https://en.wikipedia.org/w/api.php");
        MAX_RESULTS = Integer.parseInt(DOTENV.get("MAX_SEARCH_RESULTS", "10"));
        MAX_LINE_LENGTH = Integer.parseInt(DOTENV.get("MAX_LINE_LENGTH", "2"));
        MIN_LINE_LENGTH = Integer.parseInt(DOTENV.get("MIN_LINE_LENGTH", "300"));
    }
}