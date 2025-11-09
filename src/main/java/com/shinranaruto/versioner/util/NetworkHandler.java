package com.shinranaruto.versioner.util;

import com.shinranaruto.versioner.VersionerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class NetworkHandler {

    public static String readToString(String targetURL) throws IOException {
        URL url = new URL(targetURL);
        var connection = url.openConnection();
        connection.setConnectTimeout(VersionerConfig.GENERAL.versionCheckerConnectTimeout.get());
        connection.setReadTimeout(VersionerConfig.GENERAL.versionCheckerReadTimeout.get());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString().trim();
        }
    }
}