package org.pitest.report;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ReportConcat {
    private List<File> pitReportDirs;

    public static void main(String[] args) throws ParameterException {

        List<String> PARAMS = Arrays.asList(new String[]{"--projectRoot", "--copyTo", "--pitDirName", "--force", "--minimumCoverage"});
        Map<String, String> parameters = parseParameters(args, PARAMS);

        String projectRoot = getMandatoryParam(parameters, "--projectRoot");
        String copyTo = getMandatoryParam(parameters, "--copyTo");
        String pitDirName = getOptionalParam(parameters, "--pitDirName", "pit-reports");

        String minimumCoverage = getOptionalParam(parameters, "--minimumCoverage", "70");

        String force = getOptionalParam(parameters, "--force", "false");

        File copyToFile = new File(copyTo);
        if (copyToFile.exists() && copyToFile.listFiles().length > 0 && !"true".equals(force)) {
            System.out.println("copyTo is not empty and will be cleared. Add parameter --force true to avoid this message. Are you sure to continue? (Y) ");
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();
            scan.close();
            if (!"Y".equalsIgnoreCase(input)) {
                System.out.println("Bye!");
                System.exit(0);
            }
        }
        ReportConcat c = new ReportConcat();
        int mutationCoverage = c.run(projectRoot, copyTo, pitDirName);
        System.out.println("Mutation coverage: " + mutationCoverage + "%");
        if (StringUtil.isNumeric(minimumCoverage)) {
            if (Integer.parseInt(minimumCoverage) > mutationCoverage) {
                System.out.println("Mutation coverage is to low needs to be min " + minimumCoverage + "%");
                System.exit(1);
            }
        }
        System.exit(0);
    }

    private int run(String root, String to, String pitReportDir) {
        deleteDir(to);
        pitReportDirs = new ArrayList<File>();

        File f = new File(root);
        findDirectories(f, pitReportDir);

        File toDir = new File(to);
        if (!toDir.exists()) {
            try {
                Files.createDirectory(toDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (File file : pitReportDirs) {
            copyFilesToDir(file, toDir);
        }
        return concatHTMLFiles(to);
    }

    private int concatHTMLFiles(String root) {
        //Get index.html
        File htmlRoot = new File(root + "/index.html");
        if (htmlRoot.exists()) {
            //found duplicate html files
            File rootFile = new File(root);
            Element packageBody = new Element(Tag.valueOf("tbody"), "");
            try {
                Document docRoot = Jsoup.parse(htmlRoot, "UTF-8", "");
                Elements tbodies = docRoot.select("tbody");
                CoverageBody coverageBody = new CoverageBody();
                if (tbodies.size() == 2) {
                    coverageBody = new CoverageBody(tbodies.get(0));

                    packageBody = tbodies.get(1);
                }
                DupHTMLFilter filter = new DupHTMLFilter();
                File[] files = rootFile.listFiles(filter);
                for (File file : files) {
                    Document docChild = Jsoup.parse(file, "UTF-8", "");
                    Elements tbodiesChild = docChild.select("tbody");
                    CoverageBody coverageBodyChild = new CoverageBody(tbodiesChild.get(0));
                    coverageBody.append(coverageBodyChild);
                    if (tbodiesChild.size() == 2) {
                        packageBody.append(tbodiesChild.get(1).html());
                    }
                }
                String newCoverageBody = "<tr><td>" + coverageBody.getNumberOfClasses() + "</td>" +
                        "<td>" + coverageBody.getLineCoverageprocentage() + "%<div class=\"coverage_bar\"><div class=\"coverage_complete\" style=\"width:" + coverageBody.getLineCoverageprocentage() + "%\"></div>" +
                        "<div class=\"coverage_ledgend\">" + coverageBody.getLineCoverageActual() + "/" + coverageBody.getLineCoverageTotal() + "" +
                        "</div></div></td><td>" + coverageBody.getMutationCoverageprocentage() + "%<div class=\"coverage_bar\">" +
                        "<div class=\"coverage_complete\" style=\"width:" + coverageBody.getMutationCoverageprocentage() + "%\"></div>" +
                        "<div class=\"coverage_ledgend\">" + coverageBody.getMutationCoverageActual() + "/" + coverageBody.getMutationCoverageTotal() + "</div></div></td></tr>";
                tbodies.get(0).html(newCoverageBody);
                FileUtils.writeStringToFile(htmlRoot, docRoot.outerHtml(), "UTF-8");

                return coverageBody.getMutationCoverageprocentage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void deleteDir(String to) {
        try {
            FileUtils.deleteDirectory(new File(to));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findDirectories(File root, String pitReportDir) {

        FileFilter directoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        File[] files = root.listFiles(directoryFilter);
        for (File file : files) {
            if (pitReportDir.equals(file.getName())) {
                pitReportDirs.add(file);
            }
            findDirectories(file, pitReportDir);
        }
    }

    private void copyFilesToDir(File root, File to) {
        try {
            Files.walkFileTree(root.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new CopyDirVisitor(root.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMandatoryParam(Map<String, String> parameters,
                                           String key) throws ParameterException {
        if (parameters.containsKey(key)) {
            return parameters.get(key);
        } else {
            throw new ParameterException("Missing parameter: " + key, ParameterException.MISSING_PARAMETER);
        }
    }

    public static String getOptionalParam(Map<String, String> parameters, String key, String def) {
        if (parameters.containsKey(key)) {
            return parameters.get(key);
        } else {
            return def;
        }
    }

    public static Map<String, String> parseParameters(String[] args, List<String> params) throws ParameterException {
        Map<String, String> parameters = new HashMap<String, String>();
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            if (args.length < i + 1) {
                throw new ParameterException("invalid number of parameters.", ParameterException.INVALID_PARAMETER);
            }
            String value = args[i + 1];
            if (params.contains(key) && !parameters.containsKey(key)) {
                parameters.put(key, value);
            } else {
                throw new ParameterException("invalid parameter: " + key + ".", ParameterException.INVALID_PARAMETER);
            }
        }
        return parameters;
    }
}