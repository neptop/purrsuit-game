package com.purrsuit.desktop.tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PackSprites {
    public static void main(String[] args) throws Exception {
        Path projectRoot = Paths.get("").toAbsolutePath().normalize();
        Path assetsDir = projectRoot.resolve("assets").normalize();
        Path inputDir = assetsDir.resolve("sprites").normalize();
        Path outputDir = assetsDir.resolve("atlas").normalize();

        if (!Files.isDirectory(inputDir)){
            throw new IllegalArgumentException("Input directory not found: " + inputDir);
        }
        Files.createDirectories(outputDir);


        TexturePacker.Settings s = new TexturePacker.Settings();
        s.paddingX = 2;
        s.paddingY = 2;
        s.edgePadding = true;
        s.duplicatePadding = true;
        s.useIndexes = false;
        s.filterMin = TextureFilter.Nearest;
        s.filterMag = TextureFilter.Nearest;
        s.maxWidth = 2048;
        s.maxHeight = 2048;

        TexturePacker.process(s, inputDir.toString(), outputDir.toString(), "sprites");
        System.out.println("Packed sprites to: " + outputDir.resolve("sprites.atlas"));
    }
}
