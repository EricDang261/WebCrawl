package homework1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.MalformedURLException;

public class WebCrawl {
    public static void main(String[] args) throws Exception {
        String url = args[0];
        int hops = Integer.parseInt(args[1]);
        start(url, hops);
        HashSet<String> set = new HashSet<>(); // make sure that we dont visit the same website
        addURL(url, set);
        String statusCode = webCrawler(url, hops, set);
        System.out.println(statusCode);
    }

    private static void start(String strUrl, int hops) throws Exception{
        System.out.println("Simple Web Crawl");
        System.out.println("URL of the website: " + strUrl + " and hops is set to " + hops + ". ");
        HttpURLConnection connection = getRequest(strUrl);
        int statusCode = getResponseCode(connection);
        System.out.println("Status code: " + statusCode);
        connection.disconnect();
    }

    // function to return string of the website
    private static String getWebsiteString(HttpURLConnection connection) throws Exception{
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;

        while((line = reader.readLine()) != null) { // while buffered reader has a lines that are not null
            result.append(line + "\n");
            System.out.println(line);
        }
        reader.close(); // close the reader buffer
        return result.toString();
    }

    // function to parse client input into url string and number of hops(int)
    // make url object from the input string
    // make connection to the fetch resource
    private static HttpURLConnection getRequest(String strUrl) throws Exception {
        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private static String webCrawler(String strUrl, int hops, HashSet<String> set)throws Exception{
        String initialUrl = "";
        String priorWebsiteString = "";
        String websiteString = "";
        // initialize hops counter
        int hops_counter = 1;

        for(int i = 0; i <= hops; i++)
        {
            try{
                HttpURLConnection connection = getRequest(strUrl);
                int statusCode = getResponseCode(connection);
                websiteString = getWebsiteString(connection);
                connection.disconnect();
                initialUrl = strUrl; // reassign the prior website
                if(statusCode >= 200 && statusCode < 300 && i < hops){ // status is 2XX
                    List<String> list = regExFunction(websiteString);// create list of all urls within body of the site
                    for(int j = 0; j <= list.size(); j++){ // for each urls
                        if(!set.contains(list.get(j)) && i!=0){ // check if the set does not contain url
                            initialUrl = strUrl; // re-assign
                            priorWebsiteString= websiteString; // re-assign
                            strUrl = list.get(j); // set the url to the website in the list
                            addURL(strUrl, set); // add the url to the set
                            connection = getRequest(strUrl); // set up connection for the website
                            statusCode = getResponseCode(connection); // get the status
                            if(statusCode >= 300 && statusCode < 400){ // 3XX: redirect
                                String redirectURL = connection.getHeaderField("Location"); // set the website to the header
                                System.out.println("Hop " + hops_counter + ", Redirect url: " + strUrl + " -> " + redirectURL);
                                System.out.println("Status code: " + statusCode);
                                i = hops_counter;
                            } else{
                                System.out.println("Hop " + hops_counter + " is url: " + strUrl + ".");  // else print useful info
                                System.out.println("Status code: " + statusCode);
                                i = hops_counter;
                                hops_counter++;
                            }
                            if(i == hops_counter){
                                // add extra line
                                System.out.println();
                            }
                            connection.disconnect();
                            break;
                        }
                    }
                } else if(statusCode >= 300 && statusCode < 400 && i <= hops){ // website has been redirect
                    if(statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == HttpURLConnection.HTTP_MOVED_PERM){
                        strUrl = connection.getHeaderField("Location");
                        i = hops_counter;
                        hops_counter++;
                    }
                }
            }catch(MalformedURLException e){
                System.out.println("Last URL is malformed: " + strUrl);
                System.out.println("Because the last hop was malformed link, the last working website is: " + initialUrl);
                System.out.println("******************** HTML String Below ********************");
                return priorWebsiteString;
            }catch (Exception e) {
                System.out.println("\nThe last hop is not a working link, the last working website is: " + initialUrl);
                System.out.println("******************** HTML String Below ********************");
                return websiteString;
            }
        }
        System.out.println("******************** HTML String Below ********************");
        return priorWebsiteString;
    }

    // for security purpose
    private static int getResponseCode(HttpURLConnection connection)throws Exception{
        return connection.getResponseCode();
    }

    // uses regex to parse HTML String to find next websites
    private static List<String> regExFunction(String input) {
        Pattern p = Pattern.compile("a href=\"(.*?)\""); //  create pattern on body href tags
        Matcher m = p.matcher(input);
        List<String> list = new ArrayList<>();
        while(m.find()){
            list.add(m.group(1));
        }
        return list;
    }

    private static void addURL(String strURL, HashSet set){
        if(strURL.charAt(strURL.length() -1) == '/'){ // url ends with '/'
            set.add(strURL); // ad the url
            set.add(strURL.substring(0,strURL.length()-1)); // add the url without the '/'
        }else{
            set.add(strURL);
            set.add(strURL + '/');
        }
    }
}