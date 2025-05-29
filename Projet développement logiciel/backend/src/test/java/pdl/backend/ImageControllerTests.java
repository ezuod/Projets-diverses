package pdl.backend;

import static org.easymock.EasyMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pdl.backend.service.Image;
import pdl.backend.service.ImageController;
import pdl.backend.service.ImageDao;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

  // @Autowired
  // private JdbcTemplate jdbcTemplate;

  private MockMvc mockMvc;
  private IAClient iaClient;

  private ImageDao imageDao;
  private ImageController imageController;

  @BeforeEach
  public void setup() {
    imageDao = createMock(ImageDao.class); // Mock created with EasyMock
    iaClient = createMock(IAClient.class); // Mock created with EasyMock
    imageController = new ImageController(imageDao);
    ReflectionTestUtils.setField(imageController, "mapper", new ObjectMapper());
    ReflectionTestUtils.setField(imageController, "iaClient", iaClient);
    mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
  }

  // @BeforeAll
  // public static void reset() {
  //   // reset Image class static counter
  //   ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
  // }

  @Test
  @Order(1)
  public void getImageListShouldReturnSuccess() throws Exception {
    expect(imageDao.retrieveAll()).andReturn(Collections.emptyList());
    replay(imageDao);

    mockMvc
        .perform(get("/images"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(imageDao);
  }

  // besoin test 1.2
  @Test
  @Order(2)
  public void getImageListWithImgShouldReturnSuccess() throws Exception {
    Image img1 = new Image("test1.jpeg", new byte[0], "test1*test1");
    Image img2 = new Image("test2.png", new byte[0], "test2*test2");
    img1.setId(1L);
    img2.setId(2L);

    expect(imageDao.retrieveAll()).andReturn(List.of(img1, img2));
    replay(imageDao);

    mockMvc
        .perform(get("/images"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(img1.getId()))
        .andExpect(jsonPath("$[0].name").value("test1.jpeg"))
        .andExpect(jsonPath("$[0].type").value(MediaType.IMAGE_JPEG.toString()))
        .andExpect(jsonPath("$[0].size").value("test1*test1"))
        .andExpect(jsonPath("$[1].id").value(img2.getId()))
        .andExpect(jsonPath("$[1].name").value("test2.png"))
        .andExpect(jsonPath("$[1].type").value(MediaType.IMAGE_PNG.toString()))
        .andExpect(jsonPath("$[1].size").value("test2*test2"));

    verify(imageDao);
  }

  @Test
  @Order(3)
  public void getImageShouldReturnNotFound() throws Exception {
    expect(imageDao.retrieve(1L)).andReturn(Optional.empty());
    replay(imageDao);

    mockMvc.perform(get("/images/1")).andDo(print()).andExpect(status().isNotFound());

    verify(imageDao);
  }

  @ParameterizedTest
  @CsvSource({"test.jpeg, image/jpeg", "test.png, image/png"})
  @Order(4)
  public void getImageShouldReturnSuccess(String fileName, String mediaType) throws Exception {
    byte[] imageData = new byte[10];
    Image img = new Image(fileName, imageData, "test*test");
    img.setId(0L);
    MediaType media = MediaType.valueOf(mediaType);

    expect(imageDao.retrieve(0L)).andReturn(Optional.of(img));
    replay(imageDao);

    mockMvc
        .perform(get("/images/0"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(media))
        .andExpect(content().bytes(imageData));

    verify(imageDao);
  }

  @Test
  @Order(5)
  public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
    mockMvc.perform(delete("/images")).andDo(print()).andExpect(status().isMethodNotAllowed());
  }

  @Test
  @Order(6)
  public void deleteImageShouldReturnNotFound() throws Exception {
    expect(imageDao.retrieve(1L)).andReturn(Optional.empty());
    replay(imageDao);

    mockMvc.perform(delete("/images/1")).andDo(print()).andExpect(status().isNotFound());

    verify(imageDao);
  }

  @ParameterizedTest
  @CsvSource({"test.jpeg, image/jpeg", "test.png, image/png"})
  @Order(7)
  public void deleteImageShouldReturnSuccess(String fileName) throws Exception {
    Image img = new Image(fileName, new byte[0], "test*test");
    img.setId(0L); // Set id to 0
    expect(imageDao.retrieve(0L)).andReturn(Optional.of(img));
    imageDao.delete(img);
    expectLastCall();
    replay(imageDao);

    mockMvc.perform(delete("/images/0")).andDo(print()).andExpect(status().isOk());

    verify(imageDao);
  }

  @Test
  @Order(8)
  public void createImageShouldReturnSuccess() throws Exception {

    MockMultipartFile jpegFile =
        new MockMultipartFile(
            "file",
            "test.jpeg",
            "image/jpeg",
            Files.readAllBytes(new ClassPathResource("test.jpeg").getFile().toPath()));
    MockMultipartFile pngFile =
        new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            Files.readAllBytes(new ClassPathResource("test.png").getFile().toPath()));
    MockMultipartFile jpgFile =
        new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            Files.readAllBytes(new ClassPathResource("test.jpg").getFile().toPath()));

    expect(iaClient.analyzeFilePath(anyString()))
        .andReturn(Map.of("caption", "Une image", "keywords", List.of("mot1", "mot2")))
        .times(3);

    // Simulates image creation for each format
    imageDao.create(anyObject(Image.class));
    expectLastCall().times(3);
    replay(iaClient, imageDao);

    // Test upload JPEG
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/images").file(jpegFile))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Image uploaded and processed"));

    // Test upload PNG
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/images").file(pngFile))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Image uploaded and processed"));

    // Test upload jpg
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/images").file(jpgFile))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Image uploaded and processed"));

    verify(iaClient, imageDao);
  }

  @Test
  @Order(9)
  public void createImageShouldReturnUnsupportedMediaType() throws Exception {
    MockMultipartFile multipartFile =
        new MockMultipartFile("file", "test.jpg", "text/plain", "Test".getBytes());

    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType());
  }

  // besoin 5
  @Test
  @Order(12)
  public void getImageListWithNonImageFilesShouldReturnEmptyList() throws Exception {
    File testDir = new File("images");
    if (!testDir.exists()) {
      testDir.mkdir();
    }
    File nonImageFile = new File(testDir, "test.txt");
    nonImageFile.createNewFile();

    expect(imageDao.retrieveAll()).andReturn(Collections.emptyList());
    replay(imageDao);

    mockMvc
        .perform(get("/images"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(imageDao);
    nonImageFile.delete();
  }
}
