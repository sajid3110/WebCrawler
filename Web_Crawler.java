package IsaProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Web_Crawler {

    private static ArrayList<String> words;
    private static ArrayList<String> links;
    private static ArrayList<String> linksToVisit;
    private static ArrayList<Double> values;
    private static ArrayList<String> fileNames;

    private static String input;

    static String lineSeparator = System.getProperty("line.separator");

    private static File indexFile = new File("/home/sajid/NetBeansProjects/IR/files/indexFile.txt");
    private static File permuterm = new File("/home/sajid/NetBeansProjects/IR/files/permuterm.txt");
    private static File[] allFiles;

    Timer timer = new Timer();
    TimerTask myTask = new TimerTask() {
        public void run() {
            System.out.println("index file updated");
            initialise();
        }
    };

    public void start() {
        timer.scheduleAtFixedRate(myTask, 0, 40000);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(System.in);
        Web_Crawler u = new Web_Crawler();
        u.start();
        while (true) {
            input = "";
            System.out.println("Enter the Query : ");
            String in = s.nextLine();
            in = in.toLowerCase();
            ArrayList<String> al;
            StringTokenizer st = new StringTokenizer(in);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.contains("*")) {
                    al = new ArrayList<>();
                    al = wildcard(token);
                    for (int i = 0; i < al.size(); i++) {
                        if (!input.contains(al.get(i))) {
                            input = input + al.get(i) + " ";
                        }
                    }
                } else {
                    if (!input.contains(token)) {
                        input = input + token + " ";
                    }
                }
            }
           
            System.out.println("Query to be searched : " + input);
            cosine();
        }

    }

    private static void initialise() {
        words = new ArrayList<>();
        linksToVisit = new ArrayList<>();
        File folder = new File("/home/sajid/NetBeansProjects/IR/files/doc1");
        if (folder.isDirectory()) {
            allFiles = folder.listFiles();
            for (int i = 0; i < allFiles.length; i++) {
                linksToVisit.add(allFiles[i].getAbsolutePath().toString());
            }

            int i = 0;
            while (!linksToVisit.isEmpty() && i < linksToVisit.size()) {
                links = new ArrayList<>();
                File f = new File(linksToVisit.get(i));
                String s = readFile(f.getAbsolutePath());
                StringTokenizer tokens = new StringTokenizer(s);
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    if (token.charAt(0) == '/') {
                        File checkFile = new File(token);
                        if (checkFile.exists()) {
                            links.add(token);
                            if (!linksToVisit.contains(token)) {
                                linksToVisit.add(token);
                            }
                        } else {
                            System.out.println("File not found : " + token + " in " + f.getName());
                        }
                    } else {
                        token = token.toLowerCase();
                        if (!words.contains(token)) {
                            words.add(token);
                        }
                    }
                }
                i++;
            }
        }
        values = new ArrayList<>();
        if (!indexFile.exists()) {
            try {
                indexFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Web_Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!permuterm.exists()) {
            try {
                permuterm.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Web_Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 0; i < linksToVisit.size(); i++) {
            String data = readFile(linksToVisit.get(i));
            StringTokenizer dataTokenizer;
            for (int j = 0; j < words.size(); j++) {
                double k = 0.0;
                dataTokenizer = new StringTokenizer(data);
                while (dataTokenizer.hasMoreTokens()) {
                    if (words.get(j).equalsIgnoreCase(dataTokenizer.nextToken())) {
                        k++;
                    }
                }
                values.add(k);
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
            BufferedWriter bw1 = new BufferedWriter(new FileWriter(permuterm));
            readFileNames();
            for (int i = 0; i < fileNames.size(); i++) {
                bw.append("\t" + fileNames.get(i));
            }

            bw.append(lineSeparator);

            int k = 0, l = 0;
            for (int i = 0; i < words.size(); i++) {
                String a[] = new String[words.get(i).length() + 1];
                bw.append(words.get(i) + "\t");
                bw1.write(words.get(i));
                for (int j = 0; j < values.size() / words.size(); j++) {
                    bw.append(values.get(l) + "\t");
                    l = l + words.size();
                }
                k++;
                l = k;
                bw.append(lineSeparator);

                a = PermuTerm(words.get(i));
                for (int j = 0; j < a.length; j++) {
                    bw1.write("\t" + a[j]);
                }
                bw1.append(lineSeparator);

            }

            bw.close();
            bw1.close();
        } catch (IOException ex) {
            Logger.getLogger(Web_Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
    }

    public static void cosine() {
        readFileNames();
        ArrayList<ArrayList> lists1 = new ArrayList<>();
        ArrayList<Double> a1;
        int k = 0;
        int l = 0;
        while (k < values.size() / words.size()) {
            int i;
            a1 = new ArrayList<>();
            for (i = l; i < l + words.size(); i++) {
                a1.add(values.get(i));
            }
            lists1.add(a1);
            l = i;
            k++;
        }

        for (int i = 0; i < lists1.size(); i++) {
            a1 = lists1.get(i);
            for (int j = 0; j < a1.size(); j++) {
                if (a1.get(j) > 0) {
                    a1.set(j, (1.0 + Math.log10(a1.get(j))));
                } else {
                    a1.set(j, 0.0);
                }
            }
            lists1.set(i, a1);
        }

        for (int i = 0; i < lists1.size(); i++) {
            a1 = lists1.get(i);
            double sum = 0;
            for (int j = 0; j < a1.size(); j++) {
                sum += (a1.get(j) * a1.get(j));
            }
            double d = Math.sqrt(sum);
            for (int j = 0; j < a1.size(); j++) {
                a1.set(j, (a1.get(j) / d));
            }
        }

        ArrayList<Double> query = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String nextWord = words.get(i);
            double count = 0;
            StringTokenizer st = new StringTokenizer(input);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equalsIgnoreCase(nextWord)) {
                    count++;
                }
            }
            if (count > 0) {
                query.add((1 + Math.log10(count)));
            } else {
                query.add(0.0);
            }
        }
        double res = 0;
        for (int i = 0; i < query.size(); i++) {
            res += (query.get(i) * query.get(i));
        }
        double d = Math.sqrt(res);
        for (int i = 0; i < query.size(); i++) {
            query.set(i, (query.get(i) / d));
        }

        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < lists1.size(); i++) {
            double sum = 0;
            ArrayList<Double> doc = lists1.get(i);
            for (int j = 0; j < doc.size(); j++) {
                sum += (doc.get(j) * query.get(j));
            }
            result.add(sum);
        }

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size() - 1; j++) {
                double val1 = result.get(j);
                double val2 = result.get(j + 1);
                String v1 = fileNames.get(j);
                String v2 = fileNames.get(j + 1);
                if (val2 > val1) {
                    result.set(j, val2);
                    result.set(j + 1, val1);
                    fileNames.set(j, v2);
                    fileNames.set(j + 1, v1);
                }
            }
        }

        System.out.println("-------------------Ranking of the Documents----------------");
        for (int i = 0; i < fileNames.size(); i++) {
            System.out.println(fileNames.get(i));
        }

    }

    private static void readFileNames() {
        fileNames = new ArrayList<>();
        for (int i = 0; i < linksToVisit.size(); i++) {
            File f = new File(linksToVisit.get(i));
            fileNames.add(f.getName());
        }
    }

    private static String readFile(String path) {
        StringBuffer sb = new StringBuffer();
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Web_Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Web_Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public static String[] PermuTerm(String s) {
        int n = s.length();
        String a[] = new String[n + 1];
        String c1 = "", c2 = s;
        a[0] = s + "$";
        for (int i = 1; i <= n; i++) {
            c1 += s.charAt(i - 1);
            c2 = s.substring(i);
            a[i] = c2 + "$" + c1;
        }
        return a;
    }

    public static ArrayList wildcard(String token) throws FileNotFoundException, IOException {
        String line, key;
        ArrayList<String> al = new ArrayList<>();
        int n = token.length();
        String a = "";
        String c1 = "", c2 = input;
        for (int i = 0; i < n; i++) {
            c1 += token.charAt(i);
            c2 = token.substring(i + 1);
            a = c2 + "$" + c1;
            if (token.charAt(i) == '*') {
                break;
            }
        }

        if (a.charAt(a.length() - 1) == '*') {
            a = a.substring(0, a.length() - 1);
        }

        FileReader fr = new FileReader(permuterm);
        BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            key = st.nextToken();
            while (st.hasMoreTokens()) {
                String g = st.nextToken().toString();
                if (g.startsWith(a)) {
                    al.add(key);
                }
            }
        }
        return al;
    }
}
