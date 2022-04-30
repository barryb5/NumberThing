import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Main {
    private static final String newLine  = System.getProperty("line.separator");

    public static void main(String[] args) throws IOException, CsvException {
        long startTime = System.nanoTime();
        int total = 0;
        File csvFile = new File("/home/barry/projects/java/NumberThing/out/CodesTest.csv");

        CSVReader reader = new CSVReader(new FileReader(csvFile));
        List<String[]> csvBody = reader.readAll();

        CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true));

        if (!csvBody.get(csvBody.size() - 1)[0].equals("Code")) {
            total = Integer.parseInt(csvBody.get(csvBody.size() - 1)[0]);
        }
        if (csvBody.isEmpty() == true) {
//            System.out.println(csvBody.get(0)[0]);
            String[] nextLine = {"Code", "Name", "Artist", "Pages", "Date"};
            writer.writeNext(nextLine);
        }
        writer.close();
        reader.close();

        System.out.println(total);


        reader = new CSVReader(new FileReader(csvFile));
        writer = new CSVWriter(new FileWriter(csvFile, true));
        csvBody = reader.readAll();

        for (int i = total + 1; i < 70000; i++) {
            String code = "" + i;
            URL url = new URL("https://nhentai.net/g/" + code + "/");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();

            System.out.println(responseCode);
            if (responseCode == 503) {
                // Website blocked
                System.out.println("Website not working");
                System.exit(0);
            } else if(responseCode != 200)
                System.out.println(i + " Is not a code because: HttpResponseCode: " + responseCode);
            else {
                String readStream = readStream(connection.getInputStream());
//                System.out.println(readStream);


                System.out.println(i + " is a code");
                total++;

                // Should probably check that it has all this stuff

                if (readStream.contains("itemprop=\"name\" content=\"") && readStream.contains("\" /><meta itemprop=\"image\"") && readStream.contains("span class=\"name\">") && readStream.contains("</span><span class")) {

                    // All the stuff, could maybe also try splitting through the semicolons
                    String name = readStream.substring(readStream.indexOf("itemprop=\"name\" content=\"") + 25, readStream.indexOf("\" /><meta itemprop=\"image\""));
                    String artist = "";
                    if (readStream.contains("/artist/")) {
                        artist = readStream.substring(readStream.indexOf("/artist/"));
                        artist = artist.substring(artist.indexOf("span class=\"name\">") + 18, artist.indexOf("</span><span class"));
                    } else {
                        System.out.println("No Artist");
                        artist = "Artist name hidden";
                    }
                    String pagesHere = readStream.substring(readStream.indexOf("Pages:"), readStream.length() - 1);
                    int pages = Integer.parseInt(pagesHere.substring(pagesHere.indexOf("span class=\"name\">") + 18, pagesHere.indexOf("</span></a></span></div><div")));

                    // Make better + make sure it works
                    String datish = readStream.substring(readStream.indexOf("datetime=\"") + 10, readStream.indexOf("datetime=\"") + 45);
                    Date date = Date.from(Instant.parse(datish.substring(0, datish.indexOf("\">"))));


                    String[] tags;
                    if (readStream.contains("/tag/") && readStream.contains("/\" class=\"tag")) {
                        tags = readStream.split("/tag/");

                        for (int j = 0; j < tags.length; j++) {
                            if (tags[j].contains("/\" class=\"tag")) {
                                tags[j] = tags[j].substring(0, tags[j].indexOf("/\" class=\"tag"));
                            }
                        }
                    } else {
                        System.out.println("No tags");
                        tags = new String[1];
                        tags[0] = "No Tags";
                    }
//                    System.out.println(name + " " + artist + " " + pages + " " + date);



                    // Put it into the spreadsheet
                    String[] data = {code, name, artist, Integer.toString(pages), date.toString()};
                    System.out.println(Arrays.toString(data));
                    writer.writeNext(data);

//                    System.out.println("Data here");
//                    System.out.println(code);
//                    System.out.println(name);
                }

            }

        }
        System.out.println("There are " + (csvBody.size()-1) + " existing codes out of 150");
        writer.close();

        long totalTime = System.nanoTime() - startTime;
        totalTime = totalTime/1000000000;
        System.out.println(totalTime + " seconds elapsed");
    }

    private static String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine + newLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();


    }



}
