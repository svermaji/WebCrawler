package org.sv.webcrawler;

import org.sv.webcrawler.util.MyLogger;
import org.sv.webcrawler.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sv.webcrawler.util.Utils.*;

public class WebCrawler {

    private String[] validUrlEnds = {".asp", ".html", ".htm", ".jsp"};

    private final String HREF_PATTERN = "<a[\\h]+href[\\h]*=[\\h]*\"(.*?)\"";
    private final String LINK_PATTERN = "<link(.*?)(href)[\\\\h]*=[\\\\h]*\\\"(.*?)\\\"";
    private final String SCRIPT_PATTERN = "<script(.*?)(src)[\\\\h]*=[\\\\h]*\\\"(.*?)\\\"";

    private List<String> patternNames;

    //private String domain = "http://www.serosoft.in";
    private String domain = "file:///" + Utils.getCurrentDir() + "/resource/sero.html";
    private String domainToChk = domain;

    private MyLogger logger;
    private ResourceDownLoader resourceDownLoader;
    private String html;
    private List<String> pageLinks, linksVisited, linksToVisit, ignoredLinks;
    private List<Pattern> patterns;

    public static void main(String[] args) {
        WebCrawler t = new WebCrawler(args);
        t.startCrawl();
    }

    public WebCrawler(String[] args) {
        logger = MyLogger.createLogger("web-crawler.log");

        if (args != null && args.length > 0) {
            domain = args[0];
        }

        init();
    }

    private void init() {
        resourceDownLoader = new ResourceDownLoader(logger);

        pageLinks = new ArrayList<>();
        linksVisited = new ArrayList<>();
        linksToVisit = new ArrayList<>();
        ignoredLinks = new ArrayList<>();

        patternNames = new ArrayList<>();
        patternNames.add(HREF_PATTERN);
        patternNames.add(LINK_PATTERN);
        patternNames.add(SCRIPT_PATTERN);

        patterns = new ArrayList<>();
        preparePatterns();

        //TODO: set domain properly
        setDomainToCheck();

        //linksToVisit.add(domain);
        //TODO: remove link from here
        linksVisited.add(domain);
    }

    private void preparePatterns() {
        patternNames.forEach(s ->
                patterns.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)));
        logger.log("Total patterns: " + patterns.size());
    }

    private void startCrawl() {
        //TODO: correct it
        //linksToVisit.forEach(s -> {
        linksVisited.forEach(s -> {
            if (!linksVisited.contains(s)) {
                linksVisited.add(s);
                //linksToVisit.remove(s);
            }
            html = resourceDownLoader.downloadAsString(s);
            //logger.log(html);
            pageLinks = processPatterns();
            logger.log("Page links information");
            log(pageLinks);
            processPageLinks(pageLinks);
        });
        logger.log("Links visited information");
        log(linksVisited);
        logger.log("Links to visit information");
        log(linksToVisit);
        logger.log("Ignored links information");
        log(ignoredLinks);
    }

    private void processPageLinks(List<String> pageLinks) {
        pageLinks.forEach(s -> {
            if (isProperUrl(s)) {
                if (!linksVisited.contains(s) && !linksToVisit.contains(s)) {
                    linksToVisit.add(s);
                }
            } else {
                ignoredLinks.add(s);
            }
        });
    }

    private boolean isProperUrl(String url) {
        for (String end : validUrlEnds) {
            if (url.endsWith(end)) {
                return true;
            }
        }
        return false;
    }

    private void log(List<String> links) {
        logger.log("Total links = " + links.size());
        logger.log("Links = " + getLinks(links));
    }

    private String getLinks(List<String> list) {
        return list.toString().replaceAll(COMMA_SPACE, System.lineSeparator());
    }

    private ArrayList<String> processPatterns() {

        ArrayList<String> links = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                String url = formatUrl(extractUrl(matcher.group()));
                if (isEligible(url) && !links.contains(url)) {
                    links.add(url);
                }
            }
        }

        return links;
    }

    private void setDomainToCheck() {
        if (domain.startsWith(HTTP) || domain.startsWith(HTTPS)) {
            if (domain.contains(WWW)) {
                // +1 for DOT
                domainToChk = domain.substring(domain.indexOf(WWW) + WWW.length() + 1);
            } else {
                domainToChk = domain.startsWith(HTTP) ? domain.substring(domain.indexOf(HTTP) + HTTP.length()) :
                        domain.substring(domain.indexOf(HTTPS) + HTTPS.length());
            }
        }

        logger.log("domain = " + domain);
        logger.log("domainToChk = " + domainToChk);
    }

    private String formatUrl(String url) {
        return hasValue(url) && !(url.startsWith(HTTP) || url.startsWith(HTTPS)) ? domain + url : url;
    }

    private boolean isEligible(String url) {
        return hasValue(url) && url.contains(domainToChk);
    }

    private String extractUrl(String str) {
        str = str.toLowerCase();
        // although as per pattern only those matching strings
        // will come out
        int hrefIdx = str.indexOf(Utils.HREF);
        if (hrefIdx == -1) {
            // for script case
            hrefIdx = str.indexOf(Utils.SRC);
        }
        if (hrefIdx > -1) {
            String DQ = Utils.DOUBLE_QUOTE;
            int fIdx = str.indexOf(DQ, hrefIdx);
            if (fIdx > -1) {
                int sIdx = str.indexOf(DQ, fIdx + 1);
                if (sIdx > -1 && sIdx > fIdx) {
                    return str.substring(fIdx + 1, sIdx);
                }
            }
        }
        return "";
    }

}