package pl.coderslab;

import com.github.slugify.Slugify;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {
    public static final String MAIN_PAGE = "https://www.infoworld.com/";
    public static final String SUB_PAGE = "category/java/";
    public static final String FOLDER = "pages";


    public static void main(String[] args) {

        Elements selectDoc = documentElements(MAIN_PAGE.concat(SUB_PAGE), "div.article h3 a");

        Map<String, String> linksMap = selectDoc
                .stream()
                .collect(Collectors.toMap(e -> String.join("-", UUID.randomUUID().toString(), Slugify.builder().build().slugify(e.text()))
                        , e -> MAIN_PAGE.concat(e.attr("href"))));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        linksMap.forEach((nameLink, link) -> executorService.execute(() -> extractArticle(nameLink, link)));
        executorService.shutdown();
    }

    public static Elements documentElements(String website, String cssQuery) {
        try {
            Document document = Jsoup.connect(website).get();
            return document.select(cssQuery);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void extractArticle(String nameLink, String link) {
        try {
            Elements selectArticle = documentElements(link, "div[id=drr-container]");
            FileUtils.writeStringToFile(new File(Paths.get(FOLDER, nameLink.concat(".txt")).toString()), selectArticle.text(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}