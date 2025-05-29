package pdl.backend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pdl.backend.service.Image;
import pdl.backend.service.ImageDao;

@Component
@Profile("!test")
public class ImageStartupProcessor {

  @Autowired private ImageDao imageDao;

  @Autowired private IAClient iaClient;

  private final String uploadFolder = "src/main/resources/images";

  /**
   * Initializes the image processing system by scanning the upload folder for image files,
   * analyzing them using an AI client, and storing their metadata in the database if they are not
   * already present.
   *
   * <p>Steps performed:
   *
   * <ul>
   *   <li>Checks if the upload folder exists and is a directory.
   *   <li>Retrieves all image names from the database for comparison.
   *   <li>Iterates through files in the upload folder, filtering for valid image extensions (.jpg,
   *       .jpeg, .png).
   *   <li>Skips files that are already present in the database.
   *   <li>Analyzes new image files using the AI client to extract captions and keywords.
   *   <li>Stores the image data, caption, and keywords in the database.
   * </ul>
   *
   * <p>Logs are generated for the following scenarios:
   *
   * <ul>
   *   <li>Missing or invalid upload folder.
   *   <li>No files in the database for comparison.
   *   <li>Files that are skipped because they are already processed.
   *   <li>Errors during AI analysis or file reading.
   *   <li>Successful database insertions.
   * </ul>
   *
   * <p>Note: Only files with extensions .jpg, .jpeg, and .png are processed.
   */
  @PostConstruct
  public void init() {
    File folder = new File(uploadFolder);
    if (!folder.exists() || !folder.isDirectory()) {
      System.out.println("⚠️ Dossier d'upload introuvable : " + uploadFolder);
      return;
    }

    List<Image> allImages = imageDao.findAll();
    List<String> dbNames =
        allImages.stream().map(Image::getName).filter(n -> n != null).map(String::trim).toList();

    if (dbNames.isEmpty()) {
      System.out.println("Aucun fichier en BDD pour comparaison.");
    }

    for (File file : folder.listFiles()) {
      if (!file.isFile()) continue;

      String filename = file.getName().trim();
      String filepath = file.getAbsolutePath().trim();

      // Permitted extensions
      if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
        continue;
      }

      // Checks if the image is already in base
      boolean alreadyExists =
          imageDao.retrieveAll().stream()
              .map(Image::getName)
              .map(String::trim)
              .anyMatch(name -> name.equalsIgnoreCase(filename));

      if (alreadyExists) {
        System.out.println("⏩ Image déjà traitée : " + filename);
        continue;
      }

      System.out.println("Analyse de : " + file.getAbsolutePath());

      Map<String, Object> result = iaClient.analyzeFilePath(filepath);

      if (result == null || !result.containsKey("caption") || !result.containsKey("keywords")) {
        System.out.println("❌ Erreur : réponse IA invalide pour " + filename);
        continue;
      }

      String caption = (String) result.get("caption");
      Object rawKeywords = result.get("keywords");

      if (caption == null || rawKeywords == null) {
        System.out.println("❌ Erreur : éléments manquants dans la réponse IA pour " + filename);
        continue;
      }

      List<String> keywords =
          new Gson()
              .fromJson(new Gson().toJson(rawKeywords), new TypeToken<List<String>>() {}.getType());

      try {
        Image image = new Image(filename, Files.readAllBytes(file.toPath()));
        image.setDesc(caption);
        image.setKeywords(keywords);
        imageDao.create(image);
        System.out.println("✅ Insertion BDD : " + filename);
      } catch (IOException e) {
        System.out.println("❌ Erreur lecture image : " + filename);
        e.printStackTrace();
      }
    }
  }
}
