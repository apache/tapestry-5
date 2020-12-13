// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.versionmigrator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.tapestry5.versionmigrator.internal.ArtifactChangeRefactorCommitParser;
import org.apache.tapestry5.versionmigrator.internal.PackageAndArtifactChangeRefactorCommitParser;
import org.apache.tapestry5.versionmigrator.internal.PackageChangeRefactorCommitParser;

public class Main 
{

    public static void main(String[] args) 
    {
        if (args.length == 0)
        {
            printHelp();
        }
        else 
        {
            TapestryVersion version = getTapestryVersion(args[1]);
            switch (args[0])
            {
                case "generate": 
                    createVersionFile(version);
                    break;

                case "upgrade": 
                    upgrade(version);
                    break;
                    
                default:
                    printHelp();
            }
        }
    }
    
    private static void upgrade(TapestryVersion version) 
    {
        
        String path = "/" + getFileRelativePath(getSimpleFileName(version));
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getResourceAsStream(path))
        {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<File> sourceFiles = getJavaFiles();
        
        System.out.println("Number of renamed or moved classes: " + properties.size());
        System.out.println("Number of source files found: " + sourceFiles.size());
        
        int totalCount = 0;
        int totalChanged = 0;
        for (File file : sourceFiles) 
        {
            boolean changed = upgrade(file, properties);
            if (changed) {
                totalChanged++;
                try {
                    System.out.println("Changed and upgraded file " + file.getCanonicalPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            totalCount++;
            if (totalCount % 100 == 0)
            {
                System.out.printf("Processed %5d out of %d files (%.1f%%)\n", 
                        totalCount, sourceFiles.size(), totalCount * 100.0 / sourceFiles.size());
            }
        }
        
        System.out.printf("Upgrade finished successfully. %s files changed out of %s.", totalChanged, totalCount);
        
    }
    
    private static boolean upgrade(File file, Properties properties)
    {
        Path path = Paths.get(file.toURI());
        String content;
        boolean changed = false;
        try {
            content = new String(Files.readAllBytes(path));
            String newContent = content;
            String newClassName;
            for (String oldClassName : properties.stringPropertyNames()) 
            {
                newClassName = properties.getProperty(oldClassName);
                newContent = newContent.replace(oldClassName, newClassName);
            }
            if (!newContent.equals(content))
            {
                changed = true;
                Files.write(path, newContent.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return changed;
    }
    
    private static List<File> getJavaFiles() 
    {
        ArrayList<File> files = new ArrayList<>();
        collectJavaFiles(new File("."), files);
        return files;
    }
    
    private static void collectJavaFiles(File currentFolder, List<File> javaFiles) 
    {
        File[] javaFilesInFolder = currentFolder.listFiles((f) -> f.isFile() && (f.getName().endsWith(".java") || f.getName().endsWith(".groovy")));
        for (File file : javaFilesInFolder) {
            javaFiles.add(file);
        }
        File[] subfolders = currentFolder.listFiles((f) -> f.isDirectory());
        for (File subfolder : subfolders) {
            collectJavaFiles(subfolder, javaFiles);
        }
    }

    private static void printHelp() 
    {
        System.out.println("Apache Tapestry version migrator options:");
        System.out.println("\t upgrade [version number]: updates references to classes which have been moved or renamed in Java source files in the current folder and its subfolders.");
        System.out.println("\t generate [version number]: analyzes version control and outputs information about moved classes.");
        System.out.println("Apache Tapestry versions available in this tool: " + 
                Arrays.stream(TapestryVersion.values())
                    .map(TapestryVersion::getNumber)
                    .collect(Collectors.joining(", ")));
    }

    private static TapestryVersion getTapestryVersion(String versionNumber) {
        final TapestryVersion tapestryVersion = Arrays.stream(TapestryVersion.values())
            .filter(v -> versionNumber.equals(v.getNumber()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Tapestry version: " + versionNumber + ". "));
        return tapestryVersion;
    }
    
    private static void createVersionFile(TapestryVersion version) 
    {
        final String commandLine = String.format("git diff --summary %s %s", 
                version.getPreviousVersionGitHash(), version.getVersionGitHash());
        final Process process;
        
        System.out.printf("Running command line '%s'\n", commandLine);
        List<String> lines = new ArrayList<>();
        try 
        {
            process = Runtime.getRuntime().exec(commandLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (
            final InputStream inputStream = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(inputStream);
            final BufferedReader reader = new BufferedReader(isr)) 
        {
            String line = reader.readLine();
            while (line != null)
            {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ClassRefactor> refactors = parse(lines);
        AtomicInteger packageChange = new AtomicInteger();
        AtomicInteger artifactChange = new AtomicInteger();
        AtomicInteger packageAndArtifactChange = new AtomicInteger();
        
        refactors.stream().forEach(r -> {
            if (r.isMovedBetweenArtifacts() && r.isRenamed()) {
                packageAndArtifactChange.incrementAndGet();
            }
            if (r.isMovedBetweenArtifacts()) {
                artifactChange.incrementAndGet();
            }
            if (r.isRenamed()) {
                packageChange.incrementAndGet();
            }
        });
        
        System.out.println("Stats:");
        System.out.printf("\t%d classes changed package or artifact\n", refactors.size()); 
        System.out.printf("\t%d classes changed packages\n", packageChange.get()); 
        System.out.printf("\t%d classes changed artifacts\n", artifactChange.get()); 
        System.out.printf("\t%d classes changed both package and artifact\n", packageAndArtifactChange.get()); 
        
        writeVersionFile(version, refactors);
        writeRefactorsFile(version, refactors);
    }
    
    private static void writeRefactorsFile(TapestryVersion version, List<ClassRefactor> refactors) 
    {
        File file = getFile("change-report-" + version.getNumber() + ".html");
        List<ClassRefactor> sorted = new ArrayList<>(refactors);
        sorted.sort(Comparator.comparing(
                ClassRefactor::isInternal).thenComparing(
                        ClassRefactor::getSimpleOldClassName));
        try (Formatter formatter = new Formatter(file))
        {
            formatter.format("<html>");
            formatter.format("\t<head>");
            formatter.format("\t\t<title>Changes introduced in Apache Tapestry %s</title>", version.getNumber());
            formatter.format("\t</head>");
            formatter.format("\t<body>");
            formatter.format("\t\t<table>");
            formatter.format("\t\t\t<thead>");
            formatter.format("\t\t\t\t<th>Old class name</th>");
            formatter.format("\t\t\t\t<th>Renamed or moved?</th>");
            formatter.format("\t\t\t\t<th>New package location</th>");
            formatter.format("\t\t\t\t<th>Moved artifacts?</th>");
            formatter.format("\t\t\t\t<th>Old artifact location</th>");            
            formatter.format("\t\t\t\t<th>New artifact location</th>");
            formatter.format("\t\t\t</thead>");
            formatter.format("\t\t\t<tbody>");
            sorted.stream().forEach(r -> {
                formatter.format("\t\t\t\t<tr>");
                formatter.format("\t\t\t\t\t<td>%s</td>", r.getSimpleOldClassName());
                boolean renamed = r.isRenamed();
                boolean movedBetweenArtifacts = r.isMovedBetweenArtifacts();
                formatter.format("\t\t\t\t\t<td>%s</td>", renamed ? "yes" : "no");
                formatter.format("\t\t\t\t\t<td>%s</td>", renamed ? r.getNewPackageName() : "");
                formatter.format("\t\t\t\t\t<td>%s</td>", movedBetweenArtifacts ? "yes" : "no");
                formatter.format("\t\t\t\t\t<td>%s</td>", movedBetweenArtifacts ? r.getSourceArtifact() : "");
                formatter.format("\t\t\t\t\t<td>%s</td>", movedBetweenArtifacts ? r.getDestinationArtifact() : "");
                formatter.format("\t\t\t\t\t</tr>");
            });
            formatter.format("\t\t\t</tbody>");
            formatter.format("\t\t</table>");            
            formatter.format("\t</body>");            
            formatter.format("</html>");
            System.out.println("Change report file successfully written to " + file.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeVersionFile(TapestryVersion version, List<ClassRefactor> refactors) 
    {
        Properties properties = new Properties();
        refactors.stream()
            .filter(ClassRefactor::isRenamed)
            .forEach(r -> properties.setProperty(r.getOldClassName(), r.getNewClassName()));
        
        final File file = getChangesFile(version);
        try (
                OutputStream outputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream))
        {
            properties.store(bufferedOutputStream, version.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Version file successfully written to " + file.getAbsolutePath());
    }

    private static File getChangesFile(TapestryVersion version) {
        String filename = getSimpleFileName(version);
        final File file = getFile(filename);
        return file;
    }

    private static String getSimpleFileName(TapestryVersion version) {
        return version.getNumber() + ".properties";
    }

    private static File getFile(String filename) {
        final String fileRelativePath = getFileRelativePath(filename);
        final File file = new File("src/main/resources/" + fileRelativePath);
        file.getParentFile().mkdirs();
        return file;
    }

    private static String getFileRelativePath(String filename) {
        final String fileRelativePath = 
                Main.class.getPackage().getName().replace('.', '/')
                + "/" + filename;
        return fileRelativePath;
    }

    private static List<ClassRefactor> parse(List<String> lines) 
    {
        System.out.println("Lines to process: " + lines.size());
        
        lines = lines.stream()
            .map(s -> s.trim())
            .filter(s -> s.startsWith("rename"))
            .filter(s -> !s.contains("test"))
            .filter(s -> !s.contains("package-info"))
            .filter(s -> !s.contains("/resources/"))
            .filter(s -> !s.contains("/filtered-resources/"))            
            .map(s -> s.replaceFirst("rename", "").trim())
            .collect(Collectors.toList());
        
        List<ClassRefactor> refactors = new ArrayList<>(lines.size());

        for (String line : lines) 
        {
            PackageAndArtifactChangeRefactorCommitParser packageAndArtifactParser = new PackageAndArtifactChangeRefactorCommitParser();
            ArtifactChangeRefactorCommitParser artifactParser = new ArtifactChangeRefactorCommitParser();
            PackageChangeRefactorCommitParser packageParser = new PackageChangeRefactorCommitParser();
            Optional<ClassRefactor> maybeMove = packageAndArtifactParser.apply(line);
            if (!maybeMove.isPresent()) {
                maybeMove = packageParser.apply(line);
            }
            if (!maybeMove.isPresent()) {
                maybeMove = artifactParser.apply(line);
            }
            ClassRefactor move = maybeMove.orElseThrow(() -> new RuntimeException("Commit not handled: " + line));
            refactors.add(move);
        }
        
        return refactors;
        
    }

}
