package pdl.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pdl.backend.IAClient;

@RestController
public class ImageController {

  @Autowired private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private IAClient iaClient;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @PostConstruct
  public void testInjection() {
    System.out.println("IAClient injecté ? " + (iaClient != null));
  }

  /**
   * Get image by ID, retrieves an image from the database based on the provided identifier.
   *
   * @param id - the unique identifier of the image
   * @returns ResponseEntity containing the image data if found, or an error message with HTTP
   *     status 404 if the image is not found
   *     <p>HTTP Responses: - 200 OK: image is successfully retrieved - 404 NOT FOUND: no image
   *     exists with the given ID
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      return ResponseEntity.ok().contentType(image.get().getFormat()).body(image.get().getData());
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Get image details by ID. It retrieves metadata information of an image from the database based
   * on the provided identifier.
   *
   * @param id - the unique identifier of the image
   * @return ResponseEntity containing a JSON object with image details if found, or an error
   *     message with HTTP status 404 if the image is not found
   *     <p>HTTP Responses: - 200 OK: image details are successfully retrieved - 404 NOT FOUND: no
   *     image exists with the given ID
   *     <p>JSON response format : { "id": <image_id>, "name": "<image_name>", "type":
   *     "<image_format>", "size": <image_size> }
   */
  @RequestMapping(
      value = "/images/{id}/details",
      method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> getImageDetails(@PathVariable("id") long id) {
    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      ObjectNode nodes = mapper.createObjectNode();
      nodes.put("id", image.get().getId());
      nodes.put("name", image.get().getName());
      nodes.put("type", image.get().getFormat().toString());
      nodes.put("size", image.get().getSize());
      return ResponseEntity.ok(nodes);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Delete an image by from the database based on the provided identifier.
   *
   * @param id - the unique identifier of the image to be deleted
   * @return ResponseEntity with a success message if the image is found and deleted, or an error
   *     message with HTTP status 404 if the image does not exist
   *     <p>HTTP Responses: - 200 OK: image was successfully deleted - 404 NOT FOUND: No image
   *     exists with the given ID
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      imageDao.delete(image.get());
      return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Upload a new image file (JPEG or PNG) to the server.
   *
   * @param file - image file to be uploaded
   * @param redirectAttributes - attributes for a redirect scenario (not used in this method)
   * @return ResponseEntity with a success message if the image is uploaded successfully, or an
   *     error message with the appropriate HTTP status in case of failure
   *     <p>HTTP Responses: - 200 OK: The image was successfully uploaded. - 204 NO CONTENT: The
   *     file could not be read. - 415 UNSUPPORTED MEDIA TYPE: The file format is not JPEG or PNG.
   *     <p>The uploaded image is stored in the database with its original filename and binary data.
   */
  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    String contentType = file.getContentType();
    if (contentType == null
        || (!contentType.equals(MediaType.IMAGE_JPEG_VALUE)
            && !contentType.equals(MediaType.IMAGE_PNG_VALUE))) {
      return new ResponseEntity<>(
          "Only JPEG or PNG file format supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      // Save to images folder
      Path uploadPath = Paths.get("src/main/resources/images");
      Files.createDirectories(uploadPath);
      String cleanName = file.getOriginalFilename().trim();
      Path imagePath = uploadPath.resolve(cleanName);
      Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

      // Call IA
      Map<String, Object> result = iaClient.analyzeFilePath(file.getOriginalFilename());

      // Create and save image in database
      String desc = (String) result.get("caption");
      Gson gson = new Gson();
      List<String> keywords =
          gson.fromJson(
              gson.toJson(result.get("keywords")), new TypeToken<List<String>>() {}.getType());

      Image image = new Image(cleanName, file.getBytes());
      image.setDesc(desc);
      image.setKeywords(keywords);
      System.out.println("✅ DESC: " + image.getDesc());
      System.out.println("✅ KEYWORDS: " + keywords);

      imageDao.create(image);

      return new ResponseEntity<>("Image uploaded and processed", HttpStatus.OK);

    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file", HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(
          "Error during processing: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get the list of all images by retrieving metadata information for all stored images, returns it
   * as a JSON array.
   *
   * @return ArrayNode containing a list of image details
   *     <p>HTTP Responses: - 200 OK: list of images is successfully retrieved
   *     <p>JSON Response Format: [ { "id": <image_id>, "name": "<image_name>", "type":
   *     "<image_format>", "size": <image_size> }, ... ]
   */
  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() {
    List<Image> images = imageDao.retrieveAll();
    ArrayNode nodes = mapper.createArrayNode();
    for (Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId() != null ? image.getId() : -1);
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getFormatAsString());
      objectNode.put("size", image.getSize());
      nodes.add(objectNode);
    }
    return nodes;
  }

  /**
   * This method compares two images using their histograms (cours distances entre images). The
   * Euclidean distance is used to quantify the similarity between the histograms.
   *
   * @param hist1 - first histogram represented as a 2D integer array (hue x saturation)
   * @param hist2 - second histogram represented as a 2D integer array (hue x saturation)
   * @return Euclidean distance between the two histograms (a lower value indicates higher
   *     similarity)
   */
  private double calculateEuclideanDistance(int[] hist1, int[] hist2) {
    double sum = 0;
    for (int i = 0; i < 360; i++) {
      // Calculation difference between the values of the two histograms
      double diff = hist1[i] - hist2[i];
      sum += diff * diff; // Adding the square of the difference to the sum
    }
    // Returns the square root of the sum, which gives the Euclidean distance
    return Math.sqrt(sum);
  }

  /**
   * Finds the N most similar images to a given image based on a specified descriptor.
   *
   * @param id - unique identifier of the reference image
   * @param N - number of similar images to retrieve (optional, defaults to 5)
   * @param descriptor - the descriptor to use for similarity comparison (e.g., "histogram")
   * @return ResponseEntity containing a JSON array of image details for the most similar images
   *     <p>HTTP Responses: - 200 OK: Successfully retrieved the list of similar images - 400 BAD
   *     REQUEST: Invalid parameters (N <= 0 or invalid descriptor) - 404 NOT FOUND: The reference
   *     image with the given ID does not exist - 500 INTERNAL SERVER ERROR: An unexpected error
   *     occurred while processing the request
   *     <p>Endpoint: GET /images/{id}/similar?number=N&descriptor=DESCR
   *     <p>JSON Response Format: [ { "id": <image_id>, "name": "<image_name>", "type":
   *     "<image_format>", "size": <image_size>, "distance": <image_distance> }, ... ]
   */
  @RequestMapping(
      value = "/images/{id}/similar",
      method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> findSimilarImages(
      @PathVariable("id") long id,
      @RequestParam(value = "number", defaultValue = "5") int N,
      @RequestParam(value = "descriptor", defaultValue = "histogram") String descriptor) {

    if (N <= 0) return new ResponseEntity<>("Number must be positive", HttpStatus.BAD_REQUEST);
    if (!descriptor.equals("histogram")) // maybe will add others later
    return new ResponseEntity<>(
          "Invalid descriptor. Only 'histogram' is supported", HttpStatus.BAD_REQUEST);

    try {
      // verify reference image exists
      Optional<Image> referenceImage = imageDao.retrieve(id);
      if (!referenceImage.isPresent()) {
        return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
      }

      // get histogram of the reference image
      Gson gson = new Gson();
      String inputHistoJson =
          jdbcTemplate.queryForObject("SELECT histo FROM images WHERE id = ?", String.class, id);
      int[] inputHist = gson.fromJson(inputHistoJson, int[].class);

      // get all other images with their histograms
      List<Image> allImages = imageDao.retrieveAll();
      List<Object[]> distances = new ArrayList<>();

      // calculate dist
      for (Image img : allImages) {
        if (img.getId() == id) continue;

        String imgHistJson =
            jdbcTemplate.queryForObject(
                "SELECT histo FROM images WHERE id = ?", String.class, img.getId());
        int[] imgHist = gson.fromJson(imgHistJson, int[].class);

        double distance = calculateEuclideanDistance(inputHist, imgHist);
        distances.add(new Object[] {img, distance});
      }

      distances.sort((val1, val2) -> Double.compare((Double) val1[1], (Double) val2[1]));

      // top N
      int limit = Math.min(N, distances.size());
      ArrayNode nodes = mapper.createArrayNode();

      for (int i = 0; i < limit; i++) {
        Image similarImage = (Image) distances.get(i)[0];
        double distance = (Double) distances.get(i)[1];
        ObjectNode imageNode = mapper.createObjectNode();
        imageNode.put("id", similarImage.getId());
        imageNode.put("name", similarImage.getName());
        imageNode.put("type", similarImage.getFormat().toString());
        imageNode.put("size", similarImage.getSize());
        imageNode.put("distance", distance);
        nodes.add(imageNode);
      }

      return ResponseEntity.ok(nodes);

    } catch (Exception e) {
      return new ResponseEntity<>(
          "Error processing request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Search images based on a query string (e.g., keywords).
   *
   * @param query - the search query string to filter images
   * @return ResponseEntity containing a JSON array of matching image details
   *     <p>HTTP Responses: - 200 OK: Successfully retrieved the list of matching images - 400 BAD
   *     REQUEST: Query string is empty or invalid
   *     <p>Endpoint: GET /images/search?query=SEARCH_TEXT
   *     <p>JSON Response Format: [ { "id": <image_id>, "name": "<image_name>", "type":
   *     "<image_format>", "size": <image_size> }, ... ]
   */
  @RequestMapping(
      value = "/images/search",
      method = RequestMethod.GET,
      produces = "application/json")
  public ResponseEntity<?> searchImages(@RequestParam(value = "query") String query) {
    if (query == null || query.trim().isEmpty()) {
      return new ResponseEntity<>("Query cannot be empty", HttpStatus.BAD_REQUEST);
    }

    List<Image> matchedImages = imageDao.findByKeyword(query.toLowerCase().trim());

    ArrayNode nodes = mapper.createArrayNode();

    for (Image image : matchedImages) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getFormatAsString());
      objectNode.put("size", image.getSize());
      nodes.add(objectNode);
    }

    return ResponseEntity.ok(nodes);
  }
}
