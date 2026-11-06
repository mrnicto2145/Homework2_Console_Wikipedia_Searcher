package homework2;

import homework2.api.WikiFetcher;
import homework2.config.WikiConfig;
import homework2.input.ConsoleInput;
import homework2.output.BrowserOpener;
import homework2.output.ResultHandler;
import homework2.output.ResultPrinter;
import homework2.parser.WikiParser.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Точка входа: связывает ввод запроса, обращение к API и просмотр результатов.
 */
public class App {

    private final ConsoleInput input;
    private final WikiFetcher fetcher;
    private final ResultHandler resultHandler;
    private final PrintStream out;

    public App(ConsoleInput input, WikiFetcher fetcher, ResultHandler resultHandler, PrintStream out) {
        this.input = input;
        this.fetcher = fetcher;
        this.resultHandler = resultHandler;
        this.out = out;
    }

    public static void main(String[] args) {
        PrintStream out = System.out;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, consoleCharset()));

        App app = new App(
                new ConsoleInput(reader, out),
                new WikiFetcher(),
                new ResultHandler(reader, new ResultPrinter(out),
                        new BrowserOpener(WikiConfig.API_URL), WikiConfig.DEFAULT_PAGE_SIZE),
                out);
        app.run();
    }

    /** Главный цикл: запрос → поиск → просмотр результатов, пока пользователь не выйдет. */
    public void run() {
        out.println("Поиск по Википедии. Для выхода введите exit.");
        while (true) {
            String query;
            try {
                query = input.readQuery();
            } catch (IOException e) {
                out.println("Ошибка чтения ввода: " + e.getMessage());
                break;
            }
            if (query == null) {
                break;
            }
            processQuery(query);
        }
        out.println("До свидания!");
    }

    private void processQuery(String query) {
        List<SearchResult> results;
        try {
            results = fetcher.search(query);
        } catch (IOException e) {
            out.println("Сетевая ошибка: не удалось связаться с Википедией (" + e.getMessage() + ").");
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            out.println("Запрос прерван.");
            return;
        } catch (Exception e) {
            out.println("Не удалось выполнить поиск: " + e.getMessage());
            return;
        }

        try {
            resultHandler.handle(results);
        } catch (IOException e) {
            out.println("Ошибка чтения ввода: " + e.getMessage());
        }
    }

    /** Кодировка консоли (важно для кириллицы в Windows). */
    private static Charset consoleCharset() {
        return System.console() != null ? System.console().charset() : Charset.defaultCharset();
    }
}
