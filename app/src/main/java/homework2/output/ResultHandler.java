package homework2.output;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import homework2.parser.WikiParser;
import homework2.search.WikiConfig;

/**
 * Интерактивный просмотр результатов поиска.
 */
public class ResultHandler {

    public static final int DEFAULT_PAGE_SIZE = WikiConfig.DEFAULT_PAGE_SIZE;
    public static final String API_URL = WikiConfig.API_URL;

    private final BufferedReader reader;
    private final PrintStream out;
    private final int pageSize;
    private final String wikiHost;

    public ResultHandler() {
        this(new BufferedReader(new InputStreamReader(System.in)), System.out,
                DEFAULT_PAGE_SIZE, API_URL);
    }

    public ResultHandler(BufferedReader reader, PrintStream out, int pageSize, String wikiHost) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize должен быть >= 1");
        }
        this.reader = reader;
        this.out = out;
        this.pageSize = pageSize;
        this.wikiHost = wikiHost;
    }

    /**
     * Основной цикл: показывает текущую страницу, читает команду, выполняет её.
     */
    public void handle(List<WikiParser.SearchResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            out.println("Ничего не найдено.");
            return;
        }

        int totalPages = (results.size() + pageSize - 1) / pageSize;
        int page = 0;

        while (true) {
            printPage(results, page, totalPages);

            out.print("> ");
            out.flush();

            String line = reader.readLine();
            if (line == null) {
                return; // EOF (Ctrl+D)
            }
            String cmd = line.trim().toLowerCase();

            switch (cmd) {
                case "" -> { /* пустая строка — просто перерисовать */ }
                case "exit", "выход" -> { return; }
                case "fd", "next" -> {
                    if (page + 1 < totalPages) page++;
                    else out.println("Это последняя страница.");
                }
                case "bk", "prev" -> {
                    if (page > 0) page--;
                    else out.println("Это первая страница.");
                }
                default -> handleNumberOrUnknown(cmd, line, results);
            }
        }
    }

    private void handleNumberOrUnknown(String cmd, String rawLine,
                                       List<WikiParser.SearchResult> results) {
        try {
            int num = Integer.parseInt(cmd);
            if (num < 1 || num > results.size()) {
                out.println("Номер должен быть в диапазоне 1.." + results.size());
                return;
            }
            WikiParser.SearchResult r = results.get(num - 1);
            URI uri = buildUri(r.getTitle());
            out.println("Открываю: " + uri);
            if (!openInBrowser(uri)) {
                out.println("Не удалось запустить браузер. Скопируйте ссылку вручную:");
                out.println("  " + uri);
            }
        } catch (NumberFormatException e) {
            out.println("Неизвестная команда: '" + rawLine + "'. " +
                    "Введите номер, fd, bk или exit.");
        }
    }

    private void printPage(List<WikiParser.SearchResult> results, int page, int totalPages) {
        out.println();
        out.println("Результаты поиска (страница " + (page + 1) + " из " + totalPages + "):");
        int from = page * pageSize;
        int to = Math.min(from + pageSize, results.size());
        for (int i = from; i < to; i++) {
            WikiParser.SearchResult r = results.get(i);
            out.println("  " + (i + 1) + ". " + r.getTitle());
            String snippet = r.getSnippet();
            if (snippet != null && !snippet.isEmpty()) {
                out.println("     " + truncate(snippet, 120));
            }
        }
        out.println();
        out.println("[fd] следующие " + pageSize
                + "   [bk] предыдущие " + pageSize
                + "   [exit] к поиску");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /**
     * Строит абсолютный URL статьи по её заголовку.
     * Многокомпонентный конструктор URI сам percent-кодирует кириллицу и спецсимволы.
     */
    private URI buildUri(String title) {
        String path = "/wiki/" + title.replace(' ', '_');
        try {
            return new URI("https", wikiHost, path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректный заголовок: " + title, e);
        }
    }

    /**
     * Открывает ссылку в браузере. Сначала пробует {@link Desktop#browse},
     * затем — системные команды (macOS, Windows, Linux).
     */
    private boolean openInBrowser(URI uri) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                    return true;
                }
            }
        } catch (Exception ignored) {
            // падаем в фолбэк ниже
        }
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder(
                        "rundll32", "url.dll,FileProtocolHandler", uri.toString());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", uri.toString());
            } else {
                pb = new ProcessBuilder("xdg-open", uri.toString());
            }
            pb.redirectErrorStream(true);
            pb.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
