import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsoupFindByIdSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        if(args.length == 3) {
            String resourcePath = args[0];
            String secondResourcePath = args[1];
            String targetElementId = args[2];

            Optional<Element> buttonOpt = findElementById(new File(resourcePath), targetElementId);
            Optional<Map<String, String>> originalElementAttributes = buttonOpt.map(button ->
                    button.attributes().asList().stream()
                            .collect(
                                    Collectors.toMap(attr -> attr.getKey(), attr -> attr.getValue()))
            );

            if (originalElementAttributes.isPresent()) {
                String xmlPath = null;
                Optional<Elements> elements = findElementsByUrl(new File(secondResourcePath), originalElementAttributes.get().get("href"));
                if (elements.isPresent()) {
                    Map<String, Integer> buttons = findSimilarityScore(elements.get());
                    int maxValueInMap = (Collections.max(buttons.values()));

                    for (Map.Entry<String, Integer> entry : buttons.entrySet()) {
                        if (entry.getValue() == maxValueInMap) {
                            xmlPath = entry.getKey();
                        }
                    }
                }
                if (xmlPath == null) {
                    LOGGER.info("Not found similar element on another page");
                } else {
                    LOGGER.info("Path to similar element on another page -> {}", xmlPath);
                }
            } else {
                LOGGER.info("Element was not found by id - > {}", targetElementId);
            }
        } else {
            LOGGER.error("Required elements was not provided. " +
                    "Please use example: " +
                    "java -cp <your_bundled_app>.jar <input_origin_file_path> " +
                    "<input_other_sample_file_path> <element_id>");
        }
    }

    private static Optional<Element> findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.getElementById(targetElementId));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        } catch (java. lang. NullPointerException exception){
            return Optional.empty();
        }
    }

    private static Optional<Elements> findElementsByUrl(File htmlFile, String url) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            Elements links = doc.select("a[href*=#ok]");
            return Optional.of(links);

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        } catch (java. lang. NullPointerException exception){
            return Optional.empty();
        }
    }

    private static Map<String, Integer> findSimilarityScore(Elements elements){
        Map<String, Integer> buttons = new HashMap<String, Integer>();
        for (Element element : elements) {
            Map<String, String> atributes = element.attributes().asList().stream().collect(
                    Collectors.toMap(attr -> attr.getKey(), attr -> attr.getValue()));
            int counter = 0;
            if ("btn btn-success".equals(atributes.get("class"))) counter += 1;
            if ("Make-Button".equals(atributes.get("title"))) counter += 1;
            StringBuilder absPath = new StringBuilder();
            Elements parents = element.parents();

            for (int j = parents.size() - 1; j >= 0; j--) {
                Element parent = parents.get(j);
                absPath.append("/");
                absPath.append(parent.tagName());
                absPath.append("[");
                absPath.append(parent.siblingIndex());
                absPath.append("]");
            }
            buttons.put(absPath.toString(), counter);
        }
        return buttons;
    }

}
