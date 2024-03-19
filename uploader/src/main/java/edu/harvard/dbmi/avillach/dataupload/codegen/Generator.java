package edu.harvard.dbmi.avillach.dataupload.codegen;

import org.jooq.Configuration;
import org.jooq.codegen.GenerationTool;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Generator {
    public static void main(String[] args) throws Exception {
        URI uri = Generator.class.getClassLoader().getResource("jooq.xml").toURI();
        String s = Files.readString(Paths.get(uri));
        GenerationTool.generate(s);
    }
}
