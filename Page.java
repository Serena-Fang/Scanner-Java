/******************************************************************************
 *  Compilation:  javac Page.java
 *  Execution:    java -cp :./jsoup.jar Page http://www.nytimes.com
 *  Dependencies: LinkedList.java jsoup.jar
 *  
 *  Models a "Web Page", consisting of a URL, a title, the text of the body,
 *  and the list of links within. The program uses jsoup for accessing and
 *  manipulating web pages. In particular, part of the code for accessing
 *  and extracting links are from this example:
 *   https://jsoup.org/cookbook/extracting-data/example-list-links 
 *
 ******************************************************************************/

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Page {
    private static Matcher matcher;
    private static final String DOMAIN_NAME_PATTERN
    = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,15}";
    private static Pattern patrn = Pattern.compile(DOMAIN_NAME_PATTERN);

    private String URL;
    private String theTitle;    
    private Document doc; 
    private String theText;
    private double relevance;

    public Page(String url) throws IOException {
    URL = url;
    doc = Jsoup
        .connect(URL)
        .userAgent("Jsoup client")
        .timeout(30000).get();

    theText = doc.select("body").text();
    theTitle = doc.title();

    // System.out.println("opened " + theTitle + "\n" + theText.substring(0, Math.min(1000,theText.length())));
    }
    
    public double relate(String[] a,String[] b){
        double count = 0.0;
        double result = 0.0;
        Stemmer s = new Stemmer();
        for(String e: a){
            for(String f: b){
                if(s.stem(f).equals(s.stem(e)))
                result++;
            }
            count++;
    	}
        double r = result/count;
        relevance = r;
        return r;
    }
    
    // public  void setRelevance(Double t){
    //     relevance = t;
    // }
    
    public String getTitle() { return theTitle; }
    public String getText() { return theText; }
    public String getURL() { return URL; }
    public Double getRelevance() { return relevance;}

    public Iterable<String> adjacentURL() {
    LinkedList<String> domains = new LinkedList<String>();
    Elements links = doc.select("a[href]");
    
    for (Element link : links) {
        String attr = link.attr("href");
        String domainName;
        // System.out.println("trying " + attr);
        if (attr.startsWith("http") || attr.startsWith("https"))
        domainName = attr;
        else
        if (attr.startsWith("/"))
            domainName = URL + attr;
        else
            domainName = URL + "/" + attr;
        if (domains.exists(domainName) || domainName.equals(URL))
        continue;
        domains.addLast(domainName);
    }
    return domains;
    }

    public Iterable<Page> adjacentTo() {
    // This retrieves all the pages that can be accessed
    // through the links of the current Page.
    LinkedList<Page> theLinks = new LinkedList<Page>();
    LinkedList<String> domains = new LinkedList<String>();
    Elements links = doc.select("a[href]");
    
    for (Element link : links) {
        String attr = link.attr("href");
        String domainName;
        // System.out.println("trying " + attr);
        if (attr.startsWith("http") || attr.startsWith("https"))
        domainName = attr;
        else
        if (attr.startsWith("/"))
            domainName = URL + attr;
        else
            domainName = URL + "/" + attr;
        try {
        if (domains.exists(domainName) || domainName.equals(URL))
            continue;
        domains.addLast(domainName);
        theLinks.addLast(new Page(domainName));
        }
        catch (IOException | IllegalArgumentException e) {
        //System.out.println("unretievable: " + domainName + "; " + e.getMessage());
        }
        if(theLinks.size()>100)
        {
        	return theLinks;
        }
    }
    return theLinks;
    }
    

    
    public static String getDomainName(String url) {
    String domainName = "";
    matcher = patrn.matcher(url);
    if (matcher.find()) 
        domainName = matcher.group(0).toLowerCase().trim();
    return domainName;
    }

    public static void main(String[] args) throws IOException {
    boolean original = false;
    if (original) { // this is what was in the original program
        Page p = new Page(args[0]);
        // An illustration of adjacentTo()
        System.out.println(p.getText());
        for (Page v: p.adjacentTo())
        System.out.println(v.getURL());
    }
    else { // the new iterable adjacentURL() may be a better design
        Page p = new Page(args[0]);
        // An illustration of adjacentURL()
        System.out.println(p.getText());
        for (String v: p.adjacentURL()) {
        try {
            Page n = new Page(v);
            System.out.println(n.getURL());
        }
        catch (IOException | IllegalArgumentException e) {
            System.out.println("unretievable: " + v + "; " + e.getMessage());
            continue;
        }
        }

    }
    }
}
