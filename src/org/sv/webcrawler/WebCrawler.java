package org.sv.webcrawler;

import org.sv.webcrawler.util.MyLogger;
import org.sv.webcrawler.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sv.webcrawler.util.Utils.*;

/**
 * A simple web crawler that will crawl through a website
 * considering root url provided
 */
public class WebCrawler {

    // extensions which are included during crawl and will be nested
    private String[] validUrlEnds = {".asp", ".html", ".htm", ".jsp"};

    // Patterns for href, link and script nodes to collect
    private final String HREF_PATTERN = "<a[\\h]+href[\\h]*=[\\h]*\"(.*?)\"";
    private final String LINK_PATTERN = "<link(.*?)(href)[\\\\h]*=[\\\\h]*\\\"(.*?)\\\"";
    private final String SCRIPT_PATTERN = "<script(.*?)(src)[\\\\h]*=[\\\\h]*\\\"(.*?)\\\"";

    private List<String> patternNames;
    private List<Pattern> patterns;

    // tested with couple of files, website visited and its view source is stored manually
    // ex. "/resource/serosoft.in.html";
    //private String domain = "http://www.serosoft.in";
    private String domain = "https://www.w3schools.com";
    //private String domain = "file:///" + Utils.getCurrentDir() + "/resource/serosoft.in.html";
    //private String domain = "file:///" + Utils.getCurrentDir() + "/resource/w3schools.html";
    private String domainToChk = domain;

    private MyLogger logger;
    private ResourceDownLoader resourceDownLoader;
    private String html;

    /**
     * pageLinks - will collect links on a specific page
     * linksVisited - once any link is visited, to avoid its repetition stored here
     * linksToVisit - links still pending to visit are here
     * ignoredLinks - links which do not satisfy validEndUrls condition are present here
     * tempLinksToProcess - temp holder to which refilled from linksToVisit
     */
    private List<String> pageLinks, linksVisited, linksToVisit, ignoredLinks, tempLinksToProcess;

    // After reading these many links stop reading
    private final int THRESHOLD = 10;
    // tempLinksToProcess will be filled with below number of items from linksToVisit at a time
    private final int LINK_TO_VISIT_CHUNK = 5;
    // Keep tracks of number of processed files count
    private int processedFilesCount = 0;

    public static void main(String[] args) {
        new WebCrawler(args);
    }

    /**
     * Starting point
     * @param args array
     */
    public WebCrawler(String[] args) {
        logger = MyLogger.createLogger("web-crawler.log");

        if (args != null && args.length > 0) {
            domain = args[0];
        }

        init();
    }

    /**
     * Initialize variables
     */
    private void init() {
        resourceDownLoader = new ResourceDownLoader(logger);

        pageLinks = new ArrayList<>();
        linksVisited = new ArrayList<>();
        linksToVisit = new ArrayList<>();
        ignoredLinks = new ArrayList<>();
        tempLinksToProcess = new ArrayList<>();

        patternNames = new ArrayList<>();
        patternNames.add(HREF_PATTERN);
        patternNames.add(LINK_PATTERN);
        patternNames.add(SCRIPT_PATTERN);

        patterns = new ArrayList<>();
        preparePatterns();

        setDomainToCheck();

        linksToVisit.add(domain);
        fillLinksToProcess();
    }

    /**
     * refills links to process and re-start crawling
     */
    private void fillLinksToProcess() {
        synchronized (WebCrawler.class) {
            tempLinksToProcess.clear();
            if (linksToVisit.size() > LINK_TO_VISIT_CHUNK) {
                for (int i = 0; i < LINK_TO_VISIT_CHUNK; i++) {
                    tempLinksToProcess.add(linksToVisit.get(i));
                }
            } else {
                tempLinksToProcess.addAll(linksToVisit);
            }
            linksToVisit.removeAll(tempLinksToProcess);

            // for verbose info
            /*logger.log("Temporary links to process information");
            logListInfo(tempLinksToProcess);
            logger.log("Links to visit information");
            logListInfo(linksToVisit);*/

            logger.log("Temporary links to process are: " + tempLinksToProcess.size());
            logger.log("Links still to visit are: " + linksToVisit.size());
        }

        if (tempLinksToProcess.size() > 0) {
            startCrawl();
        }
    }

    private void preparePatterns() {
        patternNames.forEach(s ->
            patterns.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)));
    }

    /**
     * Crawling process starts here
     */
    private void startCrawl() {
        for (String linkToProcess : tempLinksToProcess) {

            if (processedFilesCount >= THRESHOLD) {
                logger.log("Thresh hold reached, skipping remaining links");
                logListInfo(tempLinksToProcess);
                break;
            }

            processedFilesCount++;

            if (!linksVisited.contains(linkToProcess)) {
                linksVisited.add(linkToProcess);
            }
            html = resourceDownLoader.downloadAsString(linkToProcess);
            //logger.logListInfo(html);
            pageLinks = processPatternsFor(linkToProcess);
            //logger.log("Page links information");
            //logListInfo(pageLinks);
            processPageLinks(pageLinks);
        }

        logger.log("Counter [" + processedFilesCount + "], threshold [" + THRESHOLD + "]");
        if (processedFilesCount < THRESHOLD) {
            fillLinksToProcess();
        } else {
            logger.log("Final report");
            logger.log("============");
            logger.log("Links visited information");
            logListInfo(linksVisited);
            logger.log("----------------------------------------------------------");
            logger.log("Links to visit information");
            logListInfo(linksToVisit);
            logger.log("----------------------------------------------------------");
            logger.log("Ignored links information");
            logListInfo(ignoredLinks);
        }

    }

    /**
     * Links collected on a page are filtered here
     * @param pageLinks
     */
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
        return url.equals(domain);
    }

    private void logListInfo(List<String> links) {
        logger.log("Total links = " + links.size());
        logger.log("Links = " + getLinks(links));
    }

    private String getLinks(List<String> list) {
        return list.toString().replaceAll(COMMA_SPACE, System.lineSeparator());
    }

    private ArrayList<String> processPatternsFor(String processingLink) {

        String processingLinkParent = domain + FWD_SLASH;
        if (!processingLink.equals(domain)) {
            if (processingLink.contains(FWD_SLASH)) {
                processingLinkParent = processingLink.substring(0, processingLink.lastIndexOf(FWD_SLASH) + FWD_SLASH.length());
            }
        }

        ArrayList<String> links = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                String url = formatUrl(extractUrl(matcher.group()), processingLinkParent);
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

    private String formatUrl(String url, String prefix) {

        if (hasValue(url) && !(url.startsWith(HTTP) || url.startsWith(HTTPS))) {
            if (url.startsWith(FWD_SLASH)) {
                // relative to root folder
                return domain + url;
            }
            return prefix + url;

        }
        return url;
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