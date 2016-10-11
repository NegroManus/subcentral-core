package de.subcentral.support.subcentralde;

import java.io.IOException;

import org.jsoup.nodes.Document;

public class SubCentralHttpApiPlayground {
    public static void main(String[] args) throws IOException {
        SubCentralHttpApi api = new SubCentralHttpApi();
        api.login("NegroManus", "sc-don13duck-", true);
        Document doc = api.getContent("https://www.subcentral.de/creative/votodo/votodo.php");
        api.logout();
        System.out.println(doc);
    }
}
