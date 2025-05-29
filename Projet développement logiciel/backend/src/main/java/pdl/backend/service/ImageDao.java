package pdl.backend.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

  private JdbcTemplate jdbcTemplate;
  private final Map<Long, Image> images = new HashMap<>();

  @PostConstruct
  public void resetSchemaOnStartup() {
    System.out.println("[ImageDao] Reset de la table images...");
    jdbcTemplate.execute("DROP TABLE IF EXISTS images");
    jdbcTemplate.execute(
        """
            CREATE TABLE images (
                id SERIAL PRIMARY KEY,
                histo TEXT,
                description TEXT,
                keywords TEXT
            );
        """);
    images.clear();
  }

  @Autowired
  public ImageDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    return Optional.ofNullable(images.get(id));
  }

  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  @Override
  public void create(final Image img) {
    // Backup to disk
    try {
      String path = "src/main/resources/images";
      File parent = new File(path);
      if (!parent.exists()) parent.mkdirs();

      File newImg = new File(parent, img.getName().trim());
      if (!newImg.exists()) newImg.createNewFile();

      Files.write(newImg.toPath(), img.getData());
    } catch (IOException e) {
      throw new RuntimeException("Error saving to /images: " + e.getMessage(), e);
    }

    // SQL insertion with ID retrieval
    Gson gson = new Gson();
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO images (histo, description, keywords) VALUES (?, ?, ?)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, img.calcHistogram());
          ps.setString(2, img.getDesc());
          ps.setString(3, gson.toJson(img.getKeywords()));
          return ps;
        },
        keyHolder);

    // Retrieve and store the generated ID
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && keys.containsKey("id")) {
      Number generatedId = (Number) keys.get("id");
      img.setId(generatedId.longValue());
      images.put(img.getId(), img);
    } else {
      throw new RuntimeException(
          "Échec de récupération de l'ID généré pour l'image " + img.getName());
    }
  }

  @Override
  public void update(final Image img, final String[] params) {
    String old = img.getName();
    String updated = Objects.requireNonNull(params[0], "Name cannot be null");
    // update in disk
    String path = "src/main/resources/images";
    File parent = new File(path);
    File oldFile = new File(parent, old);
    File newFile = new File(parent, updated);
    oldFile.renameTo(newFile);
    // update name of the image and sql
    img.setName(updated);
    jdbcTemplate.update(
        "UPDATE images SET histo = ? WHERE id = ?", img.calcHistogram(), img.getId());
    // update hasmap
    images.put(img.getId(), img);
  }

  @Override
  public void delete(final Image img) {
    // remove from disk
    String path = "src/main/resources/images";
    File file = new File(path, img.getName());
    file.delete();
    // delete index in sql
    jdbcTemplate.update("DELETE FROM images WHERE id = ?", img.getId());
    // remove from hashmap
    images.remove(img.getId());
  }

  public List<Image> findAll() {
    String sql = "SELECT * FROM images";
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
          Image img = new Image(rs.getString("id"), rs.getBytes("histo"));
          img.setDesc(rs.getString("description"));

          String keywordsJson = rs.getString("keywords");
          if (keywordsJson != null && !keywordsJson.isEmpty()) {
            img.setKeywords(
                new Gson().fromJson(keywordsJson, new TypeToken<List<String>>() {}.getType()));
          }

          return img;
        });
  }

  /**
   * Finds a list of images that match the given keyword.
   *
   * <p>This method searches the database for images whose keywords field contains the specified
   * keyword. The search is performed using a SQL query with a "LIKE" pattern to match the keyword
   * within a JSON array of keywords stored in the database.
   *
   * @param keyword The keyword to search for in the images' keywords field.
   * @return A list of {@link Image} objects that match the given keyword. Each image includes its
   *     ID, description, keywords, and a generated name. The image data (byte array) is initialized
   *     as empty.
   */
  public List<Image> findByKeyword(String keyword) {
    String sql = "SELECT * FROM images WHERE keywords LIKE ?";
    String likePattern = "%\"" + keyword + "\"%";

    return jdbcTemplate.query(
        sql,
        ps -> ps.setString(1, likePattern),
        (rs, rowNum) -> {
          //
          String name = "img_" + rs.getLong("id") + ".jpeg";
          Image img = new Image(name, new byte[0]);

          img.setId(rs.getLong("id"));
          img.setDesc(rs.getString("description"));

          String keywordsJson = rs.getString("keywords");
          if (keywordsJson != null && !keywordsJson.isEmpty()) {
            img.setKeywords(
                new Gson().fromJson(keywordsJson, new TypeToken<List<String>>() {}.getType()));
          }

          img.setName(name);
          return img;
        });
  }
}
