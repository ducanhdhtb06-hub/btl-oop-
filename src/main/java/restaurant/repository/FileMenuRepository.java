package restaurant.repository;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import restaurant.model.MenuItem;
import restaurant.model.MenuItemFactory;

/**
 * Lưu thực đơn trong file CSV (UTF-8, phân cách bằng {@code ;}). Đọc cả file khi khởi tạo,
 * ghi lại cả file mỗi lần lưu. Dòng lỗi bị bỏ qua và được ghi vào danh sách cảnh báo.
 */
public class FileMenuRepository implements Repository<MenuItem, String> {

    /** Dòng tiêu đề của file. */
    public static final String HEADER = "type;id;name;value;available;detail";

    private final Path file;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final List<String> warnings = new ArrayList<>();

    /**
     * Mở thực đơn từ file. Thiếu file thì tạo thư mục và thực đơn mẫu.
     * Không đọc được file thì dùng thực đơn mẫu và không ghi đè file.
     *
     * @param file đường dẫn file menu.csv
     */
    public FileMenuRepository(Path file) {
        this.file = file;
        if (Files.notExists(file)) {
            SampleMenu.items().forEach(item -> items.put(item.getId(), item));
            try {
                writeAll();
            } catch (IOException e) {
                warnings.add("Không ghi được " + file + ": " + e.getMessage());
            }
            return;
        }
        try {
            load();
        } catch (IOException e) {
            items.clear();
            warnings.add("Không đọc được " + file + ": " + e.getMessage() + ". Dùng thực đơn mẫu.");
            SampleMenu.items().forEach(item -> items.put(item.getId(), item));
        }
    }

    /**
     * Trả về các cảnh báo khi đọc file, mỗi cảnh báo bắt đầu bằng "Dòng N: ".
     *
     * @return danh sách cảnh báo chỉ đọc
     */
    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    @Override
    public List<MenuItem> findAll() {
        return List.copyOf(items.values());
    }

    @Override
    public Optional<MenuItem> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    /**
     * Lưu món rồi ghi lại cả file.
     *
     * @param item món cần lưu
     * @throws UncheckedIOException nếu không ghi được file
     */
    @Override
    public void save(MenuItem item) {
        items.put(item.getId(), item);
        try {
            writeAll();
        } catch (IOException e) {
            throw new UncheckedIOException("Không ghi được thực đơn vào " + file, e);
        }
    }

    private void load() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
            String row;
            int lineNo = 0;
            while ((row = reader.readLine()) != null) {
                lineNo++;
                if (row.isBlank() || (lineNo == 1 && row.startsWith("type;"))) {
                    continue;
                }
                try {
                    MenuItem item = MenuItemFactory.fromCsvRow(row, id -> Optional.ofNullable(items.get(id)));
                    if (items.containsKey(item.getId())) {
                        warnings.add("Dòng " + lineNo + ": trùng mã " + item.getId() + " với dòng phía trên, bỏ qua");
                    } else {
                        items.put(item.getId(), item);
                    }
                } catch (IllegalArgumentException e) {
                    warnings.add("Dòng " + lineNo + ": " + e.getMessage() + ", bỏ qua");
                }
            }
        }
    }

    private void writeAll() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (MenuItem item : items.values()) {
                writer.write(item.toCsvRow());
                writer.newLine();
            }
        }
    }
}
