package jarvis.jarvis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HowDoI implements Responder {
    public static final String GOOGLE_PREFIX = "https://www.google.hu/search?q=";
    private static final String SITE_SEARCH = "site:stackoverflow.com ";
    public static final String DEFAULT_REPLY = "Sorry, I don't know anything about that";
    private Util util;

    public HowDoI(Util util) {
        this.util = util;
    }

    @Override
    public boolean supports(HumanMessage message) {
        return match(message).find();
    }

    @Override
    public BotMessage respond(HumanMessage message) {
        String searchUrl = getGoogleSearchUrl(message);
        String stackoverflowUrl = searchOnGoogle(searchUrl);
        String answer = loadStackoverflow(stackoverflowUrl);



        return new BotMessage(DEFAULT_REPLY);
    }

    private String getGoogleSearchUrl(HumanMessage message) {
        Matcher matcher = match(message);
        matcher.find();

        try {
            return GOOGLE_PREFIX +
                    URLEncoder.encode(SITE_SEARCH + matcher.group(1), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String searchOnGoogle(String searchUrl) {
        String html = util.http(searchUrl);
        Document google = Jsoup.parse(html);

        Element soLink = google.select(".r a").first();
        if (soLink != null) {
            return soLink.attr("href");
        }
        return null;
    }

    private String loadStackoverflow(String stackoverflowUrl) {
        if (stackoverflowUrl != null)
            return util.http(stackoverflowUrl);

        return null;
    }

    private Matcher match(HumanMessage message) {
        Pattern pattern = getPattern();
        Matcher matcher = pattern.matcher(message.getContent());

        return matcher;
    }

    private Pattern getPattern() {
        return Pattern.compile("^how do i ([^?]+)", Pattern.CASE_INSENSITIVE);
    }
}
