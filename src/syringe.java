import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class syringe {

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();

        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        rd.close();

        return result.toString();
    }

    public static ArrayList getCharacters(int n) {
        ArrayList<String> characters = new ArrayList<String>();

        int charStart, charEnd, aniStart, aniEnd, picStart, picEnd;
        String charName, aniName, picLink, webpage, character;

        try {
            for (int i = 0; i < n * 2; i++) {
                charStart = aniStart = picStart = -1;

                // replacing unreadable chars
                webpage = getHTML("https://myanimelist.net/character.php?limit=" + 50 * i);

                aniStart = webpage.indexOf("<td class=\"animeography\">", aniStart + 1);

                for (int j = 0; j < 50; j++) {
                    // extracting character name
                    charStart = webpage.indexOf("\"fs14 fw-b\">", charStart + 1);
                    charEnd = webpage.indexOf("</a>", charStart + 1);
                    charName = webpage.substring(charStart + 12, charEnd);

                    // extracting anime name
                    aniStart = webpage.indexOf("<td class=\"animeography\">", aniStart + 1) + 2;
                    aniEnd = webpage.indexOf("</a>", aniStart + 1);
                    aniName = webpage.substring(aniStart, aniEnd);
                    aniName = aniName.substring(aniName.lastIndexOf("\">")+2);
                    if (aniName.length() > 200) continue;

                    // extracting profile picture link
                    picStart = webpage.indexOf("src=\"https://cdn.myanimelist.net/r/50x78/images/",
                            picStart + 1) + 41;
                    if (webpage.charAt(picStart) == 'q') continue;
                    picEnd = webpage.indexOf(".", picStart + 1);
                    picLink = webpage.substring(picStart, picEnd);

                    // adding all information to arraylist
                    character = (charName + " (" + aniName + ") http://cdn.myanimelist.net/" + picLink + ".jpg")
                            .replaceAll("&#039;", "'")
                            .replaceAll("&quot;", "\"")
                            .replaceAll("&amp;", "&");

                    // adding character to arraylist and printing it if not repeat
                    if (!characters.contains(character)) {
                        characters.add(character);
                        System.out.println(character);
                    }            
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return characters;
    }

    public static ArrayList createXML(ArrayList characters, int n) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            // creating and filling "package" element
            Element rootElement = doc.createElement("package");

            Attr attr = doc.createAttribute("name");
            attr.setValue("Anime character syringe #" + n);
            rootElement.setAttributeNode(attr);

            attr = doc.createAttribute("version");
            attr.setValue("4");
            rootElement.setAttributeNode(attr);

            attr = doc.createAttribute("id");
            attr.setValue("3228");
            rootElement.setAttributeNode(attr);

            attr = doc.createAttribute("date");
            attr.setValue("27.11.1999");
            rootElement.setAttributeNode(attr);

            attr = doc.createAttribute("publisher");
            attr.setValue("Time2Hack");
            rootElement.setAttributeNode(attr);

            attr = doc.createAttribute("difficulty");
            attr.setValue("10");
            rootElement.setAttributeNode(attr);

            doc.appendChild(rootElement);

            // info elements
            Element info = doc.createElement("info");
            rootElement.appendChild(info);

            // authors elements
            Element authors = doc.createElement("authors");
            info.appendChild(authors);

            // author element
            Element author = doc.createElement("author");
            author.appendChild(doc.createTextNode("Time2Hack"));
            authors.appendChild(author);

            // rounds elements
            Element rounds = doc.createElement("rounds");
            rootElement.appendChild(rounds);

            // round element
            Element round = doc.createElement("round");
            attr = doc.createAttribute("name");
            attr.setValue(":syringe:");
            round.setAttributeNode(attr);
            rounds.appendChild(round);

            // themes element
            Element themes = doc.createElement("themes");
            round.appendChild(themes);

            // theme element
            Element theme;

            // questions element
            Element questions;

            // question elements
            Element question;

            // scenario elements
            Element scenario;

            // atom elements
            Element atom;

            // right elements
            Element right;

            // answer elements
            Element answer;

            for (int j = 0; j < 10; j++) {
                // creating theme
                theme = doc.createElement("theme");
                attr = doc.createAttribute("name");
                attr.setValue(":syringe:");
                theme.setAttributeNode(attr);
                themes.appendChild(theme);

                // creating questions
                questions = doc.createElement("questions");
                theme.appendChild(questions);

                // creating 10 questions
                for (int k = 0; k < 10; k++) {
                    // get random character
                    int randomIndex = (int) (Math.random() * characters.size());
                    String character = characters.get(randomIndex).toString();

                    // remove that character from list
                    characters.remove(randomIndex);


                    // creating question
                    question = doc.createElement("question");
                    attr = doc.createAttribute("price");
                    attr.setValue("1");
                    question.setAttributeNode(attr);
                    questions.appendChild(question);

                    // creating scenario
                    scenario = doc.createElement("scenario");
                    question.appendChild(scenario);

                    // creating atom
                    atom = doc.createElement("atom");
                    attr = doc.createAttribute("type");
                    attr.setValue("image");
                    atom.setAttributeNode(attr);
                    atom.appendChild(doc.createTextNode(character.substring(character.lastIndexOf(" ") + 1, character.length())));

                    scenario.appendChild(atom);

                    // creating right
                    right = doc.createElement("right");
                    question.appendChild(right);

                    // creating answer
                    answer = doc.createElement("answer");
                    answer.appendChild(doc.createTextNode(character.substring(0, character.lastIndexOf(" "))));
                    right.appendChild(answer);
                }
            }

            // write content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\Time2Hack\\Desktop\\Syringe\\test\\content.xml"));

            transformer.transform(source, result);

            System.out.println("content.xml for \"Anime characters syringe #" + n + "\" is successfully done!");

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return characters;
    }

    public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);

        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk=zipFile.renameTo(tempFile);
        if (!renameOk)
        {
            throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (File f : files) {
                if (f.getName().equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles) {
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));

                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }

        // Close the streams
        zin.close();

        // Compress the files
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(files[i].getName()));

            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
            in.close();
        }

        // Complete the ZIP file
        out.close();
        tempFile.delete();
    }

    public static void main(String args[]) throws Exception {
        ArrayList<String> characterBuffer = new ArrayList<String>();

        // scanning from keyboard number of syringes that user wants to create
        System.out.println("How many syringes you want to create?");
        Scanner keyboard = new Scanner(System.in);
        int count = keyboard.nextInt();
        System.out.println(" ");
        characterBuffer = getCharacters(count+3);

        // removing excess characters
        int bufsize = characterBuffer.size();
        for (int i = 0; i < bufsize - count * 100; i++) {
            characterBuffer.remove(characterBuffer.size() - 1);
        }

        System.out.println(" ");

        // creating syringes
        for (int i = 0; i < count; i++) {
            // rewriting collection of characters after deleting characters
            characterBuffer = createXML(characterBuffer, i+1);

            // copying file sample.siq as Syringe.siq
            File source = new File("C:\\Users\\Time2Hack\\Desktop\\Syringe\\test\\sample.siq");
            File dest = new File("C:\\Users\\Time2Hack\\Desktop\\Syringe\\test\\Anime characters syringe #" + (i+1) + ".siq");
            Files.copy(source.toPath(), dest.toPath());

            // putting content.xml to Syringe.siq
            File[] files = {new File("C:\\Users\\Time2Hack\\Desktop\\Syringe\\test\\content.xml")};
            addFilesToExistingZip(dest, files);
            System.out.println("\"Anime characters syringe #" + (i+1) + "\" is successfully done!\n");
        }
    }
}
