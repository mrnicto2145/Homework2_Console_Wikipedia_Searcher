package homework2.output;

import homework2.parser.WikiParser.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Цикл навигации по результатам: читает команды пользователя,
 * переключает страницы и открывает выбранную статью.
 */
public class ResultHandler {

    private final BufferedReader reader;
    private final ResultPrinter printer;
    private final BrowserOpener browserOpener;
    private final int pageSize;

    public ResultHandler(BufferedReader reader, ResultPrinter printer,
                         BrowserOpener browserOpener, int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize должен быть >= 1");
        }
        this.reader = reader;
        this.printer = printer;
        this.browserOpener = browserOpener;
        this.pageSize = pageSize;
    }

    public void handle(List<SearchResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            printer.printNothingFound();
            return;
        }

        int totalPages = (results.size() + pageSize - 1) / pageSize;
        int page = 0;

        while (true) {
            printer.printPage(results, page, totalPages, pageSize);
            printer.printPrompt();

            String line = reader.readLine();
            if (line == null) {
                return; // EOF
            }
            String cmd = line.trim().toLowerCase();

            switch (cmd) {
                case "" -> { /* пустая строка — перерисовать страницу */ }
                case "exit", "выход" -> { return; }
                case "fd", "next" -> {
                    if (page + 1 < totalPages) {
                        page++;
                    } else {
                        printer.printMessage("Это последняя страница.");
                    }
                }
                case "bk", "prev" -> {
                    if (page > 0) {
                        page--;
                    } else {
                        printer.printMessage("Это первая страница.");
                    }
                }
                default -> handleSelection(cmd, line, results);
            }
        }
    }

    private void handleSelection(String cmd, String rawLine, List<SearchResult> results) {
        int number;
        try {
            number = Integer.parseInt(cmd);
        } catch (NumberFormatException e) {
            printer.printMessage("Неизвестная команда: '" + rawLine.trim()
                    + "'. Введите номер, fd, bk или exit.");
            return;
        }
        if (number < 1 || number > results.size()) {
            printer.printMessage("Номер должен быть в диапазоне 1.." + results.size());
            return;
        }
        openArticle(results.get(number - 1));
    }

    private void openArticle(SearchResult result) {
        if (result.getPageId() <= 0) {
            printer.printMessage("У статьи нет pageid — открыть её не получится.");
            return;
        }
        URI uri = browserOpener.articleUri(result.getPageId());
        printer.printMessage("Открываю: " + uri);
        if (!browserOpener.open(uri)) {
            printer.printMessage("Не удалось запустить браузер. Скопируйте ссылку вручную:");
            printer.printMessage("  " + uri);
        }
    }
}