package it.polpetta.libris.google.imageSearch.searchers;

import it.polpetta.libris.google.imageSearch.Coordinates;
import org.json.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by davide on 28/04/17.
 */
class URLSearcher implements ISearcher {

    private static final String googleImageSearch = "https://www.google.com/searchbyimage?&image_url=";
    private static final String userAgentProperty = "User-Agent";
    private static final String userAgentValue =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) " +
            "Chrome/23.0.1271.97 Safari/537.11";

    private Coordinates location = null;
    private URL link = null;


    URLSearcher (URL pathToImage, Coordinates location) {
        try {
            this.link = new URL( googleImageSearch + pathToImage.toString());

        } catch (MalformedURLException e) {

            System.err.println("The URL provided it's not correct!");
            e.printStackTrace();
        }
        this.location = location;
    }

    public JSONObject search() throws IOException {

        JSONObject res = new JSONObject();

        URLConnection toDownload = link.openConnection();
        toDownload.setRequestProperty(userAgentProperty, userAgentValue);

        BufferedReader in = new BufferedReader(new InputStreamReader(toDownload.getInputStream()));

        String htmlPage= "";
        boolean iterate = true;

        while (iterate) {
            String nextLine = in.readLine();

            if (nextLine == null) {
                iterate = false;
            } else {
                htmlPage += nextLine;
            }
        }

        // TODO parse the HTML page
        Document parsedPage = Parser.parse(htmlPage, link.toString());

        Elements body = parsedPage.body().children();

        res.put("best_guess", retrieveBestGuessFromHTML(body));
        res.put("links", retrieveLinksFromHTML(body));
        res.put("descriptions", retrieveDescriptionFromHTML(body));
        res.put("titles", retrieveTitleFromHTML(body));
        res.put("similar_images", retrieveSimilarImageFromHTML(body));

        return res;
    }

    private String retrieveBestGuessFromHTML(Elements body) {
        String bestGuessRes;
        Elements bestGuesses = body.select("a._gUb");
        bestGuessRes = bestGuesses.first().text();
        return bestGuessRes;
    }

    private ArrayList<String> retrieveLinksFromHTML(Elements body) {
        ArrayList<String> linkRes = new ArrayList<String>();
        Elements links = body.select("div.g");
        links = links.select("a[href]");
        for (Element link : links){
            if (!link.hasClass("_Fmb ab_button") &&
                    ! link.hasClass("fl") &&
                    ! link.hasClass("bia") &&
                    ! link.hasClass("duf3") &&
                    ! link.hasClass("iu-card-header") &&
                    ! link.hasAttr("data-rtid") &&
                    ! link.hasAttr("data-ved")) {
                String toCheck = link.attr("href");
                if (!linkRes.contains(toCheck)) {
                    try {
                        new URL(toCheck);
                        linkRes.add(toCheck);
                    } catch (MalformedURLException e) {}
                }

            }
        }
        return linkRes;
    }

    private ArrayList<String> retrieveDescriptionFromHTML(Elements body) {
        ArrayList<String> descRes = new ArrayList<String>();
        Elements descriptions = body.select("span.st");

        for (Element desc : descriptions)
            descRes.add(desc.text());

        return descRes;
    }

    private ArrayList<String> retrieveTitleFromHTML(Elements body) {
        ArrayList<String> titleRes = new ArrayList<String>();
        Elements titles = body.select("h3.r");

        for (Element title : titles)
            titleRes.add(title.text());

        return titleRes;
    }

    private ArrayList<String> retrieveSimilarImageFromHTML(Elements body) {
        ArrayList<String> imageRes = new ArrayList<String>();
        Elements images = body.select("div.rg_meta");

        JSONObject jsImage;
        String imageUrl;
        for (Element image : images){
            jsImage = new JSONObject(image.text());
            imageUrl = jsImage.getString("ou");
            try {
                new URL(imageUrl);
                imageRes.add(imageUrl);
            } catch (MalformedURLException e) {}
        }
        return imageRes;
    }
}