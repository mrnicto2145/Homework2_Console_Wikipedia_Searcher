package homework2.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает JSON-ответ MediaWiki API (action=query&list=search)
 */
public class WikiParser {

    /**
     * @param json тело JSON-ответа от Wikipedia API
     * @return список результатов поиска (может быть пустым, но не null)
     */
    public List<SearchResult> parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonObject queryObj = root.getAsJsonObject("query");
        if (queryObj == null) {
            return List.of();
        }

        JsonArray searchArray = queryObj.getAsJsonArray("search");
        if (searchArray == null) {
            return List.of();
        }

        List<SearchResult> results = new ArrayList<>(searchArray.size());
        for (JsonElement element : searchArray) {
            JsonObject item = element.getAsJsonObject();
            String title = item.has("title") ? item.get("title").getAsString() : "";
            String snippetHtml = item.has("snippet") ? item.get("snippet").getAsString() : "";
            results.add(new SearchResult(title, cleanSnippet(snippetHtml)));
        }
        return results;
    }

    /**
     * Удаляет HTML-теги из сниппета (например, обёртки {@code <span class="searchmatch">…</span>}).
     */
    private String cleanSnippet(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]+>", "").trim();
    }

    // ----- Модель результата поиска -----
    public static class SearchResult {
        private final String title;
        private final String snippet;

        public SearchResult(String title, String snippet) {
            this.title = title;
            this.snippet = snippet;
        }

        public String getTitle() { return title; }
        public String getSnippet() { return snippet; }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "title='" + title + '\'' +
                    ", snippet='" + snippet + '\'' +
                    '}';
        }
    }
}
