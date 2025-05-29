package pdl.backend.service;

import boofcv.alg.color.ColorHsv;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.http.MediaType;

public class Image {
  private Long id;
  private String name;
  private byte[] data;
  private String size;
  private MediaType format;

  private List<String> keywords;
  private String description;

  public Image(String name, byte[] data) {
    // name
    this.name = name;
    // data
    this.data = data;
    // size
    this.id = null;
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      if (data == null || data.length == 0) {
        this.size = "unknown";
        return;
      }
      BufferedImage image = ImageIO.read(bis);
      if (image == null) {
        System.err.println("⚠️ Erreur : image nulle pour " + id);
        this.size = "unknown";
        return;
      }
      int width = image.getWidth();
      int height = image.getHeight();
      this.size = width + "*" + height;
    } catch (IOException e) {
      this.size = "unknown";
      throw new RuntimeException("Error calc. image size: " + e.getMessage(), e);
    }
    if (name != null && name.contains(".")) {
      String extension =
          name.substring(name.lastIndexOf('.') + 1)
              .toLowerCase(); // https://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
      switch (extension) {
        case "jpeg":
        case "jpg":
          this.format = MediaType.IMAGE_JPEG;
          break;
        case "png":
          this.format = MediaType.IMAGE_PNG;
          break;
        default:
          this.format = MediaType.APPLICATION_OCTET_STREAM;
          break;
      }
    } else {
      this.format = MediaType.APPLICATION_OCTET_STREAM;
    }
  }

  public Image(String name, byte[] data, String size) {
    this.name = name;
    this.data = data;
    this.size = size;
    String format_str = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
    if (format_str.equals("png")) this.format = MediaType.IMAGE_PNG;
    else if (format_str.equals("jpeg")) this.format = MediaType.IMAGE_JPEG;
  }

  public String getFormatAsString() {
    return format != null ? format.toString() : "unknown";
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public MediaType getFormat() {
    return format;
  }

  public String getSize() {
    return size;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }

  public void setKeywords(final List<String> words) {
    this.keywords = words;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setDesc(final String description) {
    this.description = description;
  }

  public String getDesc() {
    return description;
  }

  /**
   * Calculates the 2D histogram of an image based on hue and saturation. This method processes the
   * image data, extracts the hue and saturation values for each pixel, and stores the histogram in
   * a 2D array.
   *
   * @return JSON string representation of the computed 2D histogram
   *     <p>Process: TODO
   *     <p>Exceptions: - Throws "RuntimeException" if there is an error reading the image.
   */
  public String calcHistogram() {
    int[] h = new int[360];
    InputStream inputStream = new ByteArrayInputStream(data);
    BufferedImage input = null;

    try {
      input = ImageIO.read(inputStream);
      Planar<GrayU8> img = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);

      // calculate the histogram
      for (int y = 0; y < img.getHeight(); y++) {
        for (int x = 0; x < img.getWidth(); x++) {
          float r = img.getBand(0).get(x, y);
          float g = img.getBand(1).get(x, y);
          float b = img.getBand(2).get(x, y);
          float[] hsv = new float[3];
          ColorHsv.rgbToHsv(r, g, b, hsv);
          int i = (int) (hsv[0] * 180 / Math.PI);
          h[i]++;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error histogram: " + e.getMessage(), e);
    }
    return new Gson().toJson(h);
  }
}
