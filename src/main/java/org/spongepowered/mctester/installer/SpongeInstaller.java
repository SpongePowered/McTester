package org.spongepowered.mctester.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import javax.net.ssl.HttpsURLConnection;

public class SpongeInstaller {

    private static final String SPONGE_JAR_GLOB = "glob:spongeforge-*-dev.jar";
    private static final String DOWNLOAD_LIST = "https://dl-api.spongepowered.org/v1/org.spongepowered/spongeforge/downloads?type=stable&minecraft=1.12.2";

    public void downloadLatestSponge(File downloadDirectory) {
        JsonArray downloadList = this.getDownloadList();
        URL jarUrl = this.extractURL(downloadList);
        System.err.println("Downloading Sponge: " + jarUrl);
        this.saveURLtoFile(jarUrl, downloadDirectory);
    }

    private void saveURLtoFile(URL url, File downloadDirectory) {
        String path = url.getPath();
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File outputFile = new File(downloadDirectory, fileName);

        if (outputFile.exists()) {
            System.err.println("Already downloaded latest SpongeForge: " + outputFile);
            return;
        }

        this.removeOtherSpongeBuilds(downloadDirectory);

        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Sponge jar!", e);
        }
    }

    private void removeOtherSpongeBuilds(File downloadDirectory) {
        System.err.println("Removing old Sponge build...");
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(SPONGE_JAR_GLOB);
        try {
            for (Path path : Files.newDirectoryStream(downloadDirectory.toPath(), (p) -> matcher.matches(p.getFileName()))) {
                System.err.println("Deleting old Sponge version: " + path);
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove old Sponge versions!", e);
        }

    }

    private JsonArray getDownloadList() {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(DOWNLOAD_LIST).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            return new JsonParser().parse(this.readResponse(conn)).getAsJsonArray();

        } catch (IOException e) {
            throw new RuntimeException("Error downloading latest SpongeForge!", e);
        }
    }

    private URL extractURL(JsonArray downloads) {
        JsonObject artifacts = downloads.get(0).getAsJsonObject().get("artifacts").getAsJsonObject();
        JsonObject jar = artifacts.get("dev").getAsJsonObject(); // We're in a deobufscated environment, so use the 'dev' jar
        try {
            return new URL(jar.get("url").getAsString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    // Copied from https://stackoverflow.com/a/1485730
    private String readResponse(final HttpURLConnection connection) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

}
