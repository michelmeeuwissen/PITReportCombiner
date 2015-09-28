package org.pitest.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class CopyDirVisitor extends SimpleFileVisitor<Path> {

    private Path fromPath;
    private Path toPath;
    private StandardCopyOption copyOption;


    public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.copyOption = copyOption;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        File f = toPath.resolve(fromPath.relativize(file)).toFile();
        if(f.exists() && f.getName().equals("index.html")){
            f = new File(toPath.resolve(fromPath.relativize(file)).toString()+System.nanoTime()+".html");
            Files.copy(file, f.toPath(), copyOption);
        }else{
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
        }
        return FileVisitResult.CONTINUE;
    }
}