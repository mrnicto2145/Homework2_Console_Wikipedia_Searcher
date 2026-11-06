package homework2.output;

import java.awt.Desktop;
import java.net.URI;

/**
 * Строит ссылку на статью по pageid и открывает её в браузере по умолчанию.
 */
public class BrowserOpener {

    private final String host;

    /** @param apiUrl URL API, например https://ru.wikipedia.org/w/api.php — из него берётся хост */
    public BrowserOpener(String apiUrl) {
        String h = URI.create(apiUrl).getHost();
        if (h == null) {
            throw new IllegalArgumentException("Не удалось определить хост из " + apiUrl);
        }
        this.host = h;
    }

    /** Ссылка вида https://ru.wikipedia.org/w/index.php?curid=11099 */
    public URI articleUri(long pageId) {
        return URI.create("https://" + host + "/w/index.php?curid=" + pageId);
    }

    /**
     * Открывает ссылку: сначала через {@link Desktop}, при неудаче — системной командой.
     *
     * @return true, если браузер удалось запустить
     */
    public boolean open(URI uri) {
        return openWithDesktop(uri) || openWithSystemCommand(uri);
    }

    private boolean openWithDesktop(URI uri) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                    return true;
                }
            }
        } catch (Exception ignored) {
            // переходим к запасному варианту
        }
        return false;
    }

    private boolean openWithSystemCommand(URI uri) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String url = uri.toString();
        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", url);
        } else {
            pb = new ProcessBuilder("xdg-open", url);
        }
        try {
            pb.redirectErrorStream(true).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
