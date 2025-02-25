package com.example.DestinationHashGenerator;



import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DestinationHashGenerator {

    private static String generateRandomString() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static String findDestinationKey(Object jsonObj) {
        if (jsonObj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) jsonObj;
            for (Object key : jsonObject.keySet()) {
                if (key.equals("destination")) {
                    return jsonObject.get(key).toString();
                } else {
                    String result = findDestinationKey(jsonObject.get(key));
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (jsonObj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObj;
            for (Object element : jsonArray) {
                String result = findDestinationKey(element);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <240350120110> <JSON file path>");
            return;
        }

        String prn = args[0].toLowerCase().replaceAll("\\s", "");
        String jsonFilePath = args[1];

        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            String destinationValue = findDestinationKey(jsonObject);
            if (destinationValue == null) {
                System.out.println("The key 'destination' was not found in the JSON file.");
                return;
            }

            String randomString = generateRandomString();
            String concatenatedString = prn + destinationValue + randomString;
            String md5Hash = generateMD5Hash(concatenatedString);

            System.out.println(md5Hash + ";" + randomString);
        } catch (IOException | ParseException e) {
            System.err.println("An error occurred while processing the JSON file.");
            e.printStackTrace();
        }
    }
}

