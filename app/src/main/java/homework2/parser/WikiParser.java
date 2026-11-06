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
        if (root.has("error")) {
            String info = root.getAsJsonObject("error").get("info").getAsString();
            throw new IllegalStateException("Ошибка API: " + info);
        }

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
            long pageId = item.has("pageid") ? item.get("pageid").getAsLong() : -1;
            String snippetHtml = item.has("snippet") ? item.get("snippet").getAsString() : "";
            results.add(new SearchResult(pageId, title, cleanSnippet(snippetHtml)));
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
        private final long pageId;
        private final String title;
        private final String snippet;

        public SearchResult(long pageId, String title, String snippet) {
            this.pageId = pageId;
            this.title = title;
            this.snippet = snippet;
        }

        public long getPageId() { return pageId; }
        public String getTitle() { return title; }
        public String getSnippet() { return snippet; }

        @Override
        public String toString() {
            return "SearchResult{pageId=" + pageId + ", title='" + title + "', snippet='" + snippet + "'}";
        }
    }
}
