package pdl.backend;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@SuppressWarnings("unchecked")
public class IAClient {
  private final RestTemplate restTemplate = new RestTemplate();

  public Map<String, Object> analyzeFilePath(String path) {
    String url = "http://localhost:8000/analyze-filepath";
    Map<String, String> body = Map.of("path", path);
    System.out.println("Envoi à " + url + " avec le chemin : " + path);

    Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);

    System.out.println("Réponse IA : " + response);
    return response;
  }
}
