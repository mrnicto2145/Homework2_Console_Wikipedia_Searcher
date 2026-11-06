package homework2.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import homework2.search.WikiConfig;


//Обработчик ввода с консоли с валидацией поискового запроса к Википедии.
public class ConsoleInput {
    
    public static final int MIN_LENGTH = WikiConfig.MIN_LINE_LENGTH;
    public static final int MAX_LENGTH = WikiConfig.MAX_LINE_LENGTH;

    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleInput() {
        this(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }

    /** Конструктор для тестов и нестандартных потоков. */
    public ConsoleInput(BufferedReader reader, PrintStream out) {
        this.reader = reader;
        this.out = out;
    }

    /**
     * Запрашивает у пользователя валидный поисковый запрос.
     * При ошибке печатает сообщение и повторяет ввод.
     */
    public String readQuery() throws IOException {
        while (true) {
            out.print("Введите поисковый запрос (exit — выход): ");
            out.flush();

            String line = reader.readLine();
            if (line == null) {
                return null; // EOF
            }
            if (isExitCommand(line)) {
                return null;
            }

            String error = validate(line);
            if (error == null) {
                return normalize(line);
            }
            out.println("Ошибка: " + error + ". Попробуйте ещё раз.");
        }
    }

    /** Проверяет, является ли строка командой выхода. */
    public static boolean isExitCommand(String line) {
        if (line == null) {
            return false;
        }
        String t = line.trim().toLowerCase();
        return t.equals("exit") || t.equals("quit") || t.equals("выход");
    }

    /**
     * Проверяет корректность введённой строки.
     */
    public static String validate(String input) {
        if (input == null) {
            return "Ввод отсутствует";
        }
        String normalized = normalize(input);

        if (normalized.isEmpty()) {
            return "Пустой запрос";
        }
        if (normalized.length() < MIN_LENGTH) {
            return "Запрос слишком короткий. Минимальное количество символов: " + MIN_LENGTH;
        }
        if (normalized.length() > MAX_LENGTH) {
            return "Запрос слишком длинный. Максимальное количество символов: " + MAX_LENGTH;
        }
        for (int i = 0; i < normalized.length(); i++) {
            if (Character.isISOControl(normalized.charAt(i))) {
                return "Запрос содержит недопустимые управляющие символы";
            }
        }
        return null;
    }

    public static String normalize(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }
}
