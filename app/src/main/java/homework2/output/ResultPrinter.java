package homework2.output;

import homework2.parser.WikiParser.SearchResult;

import java.io.PrintStream;
import java.util.List;

/**
 * Вывод результатов поиска и служебных сообщений в консоль.
 */
public class ResultPrinter {

    private static final int SNIPPET_MAX_LENGTH = 120;

    private final PrintStream out;

    public ResultPrinter(PrintStream out) {
        this.out = out;
    }

    /**
     * Печатает одну страницу результатов.
     *
     * @param page       номер страницы с нуля
     * @param totalPages общее число страниц
     * @param pageSize   сколько результатов на странице
     */
    public void printPage(List<SearchResult> results, int page, int totalPages, int pageSize) {
        out.println();
        out.println("Результаты поиска (страница " + (page + 1) + " из " + totalPages + "):");

        int from = page * pageSize;
        int to = Math.min(from + pageSize, results.size());
        for (int i = from; i < to; i++) {
            SearchResult result = results.get(i);
            out.println("  " + (i + 1) + ". " + result.getTitle());
            String snippet = result.getSnippet();
            if (snippet != null && !snippet.isEmpty()) {
                out.println("     " + truncate(snippet, SNIPPET_MAX_LENGTH));
            }
        }

        out.println();
        out.println("[номер] открыть   [fd] следующие " + pageSize
                + "   [bk] предыдущие " + pageSize
                + "   [exit] к поиску");
    }

    public void printPrompt() {
        out.print("> ");
        out.flush();
    }

    public void printNothingFound() {
        out.println("Ничего не найдено.");
    }

    public void printMessage(String message) {
        out.println(message);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
