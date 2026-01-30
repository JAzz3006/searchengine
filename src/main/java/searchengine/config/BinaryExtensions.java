package searchengine.config;
import java.util.Set;

public class BinaryExtensions {
    public static final Set<String> EXTENSIONS = Set.of(
            // images
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".ico", ".tiff", ".avif", ".heic", ".heif", ".jfif",

            // audio
            ".mp3", ".wav", ".ogg", ".aac", ".flac", ".m4a",

            // video
            ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".webm", ".mpeg", ".mpg", ".3gp",

            // archives
            ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz",

            // documents
            ".pdf",
            ".doc", ".docx",
            ".xls", ".xlsx",
            ".ppt", ".pptx",
            ".odt", ".ods", ".odp",
            ".rtf", ".txt", ".csv", ".epub", ".djvu", ".fb2",
            ".json", ".xml",

            // executables / installers
            ".exe", ".msi", ".apk", ".dmg", ".deb", ".rpm",

            // fonts
            ".ttf", ".otf", ".woff", ".woff2",

            // пакеты
            ".jar", ".war", ".ear",

            // other common binaries
            ".bin", ".iso", ".img", ".torrent"
    );
}