package login.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigHelper {
    public static Map<String, List<String>> settingsFromCommandLine(String[] args) throws Exception {
        String currentKey = null;
        ArrayList<String> values = new ArrayList<>();
        Map<String, List<String>> settings = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                currentKey = arg.substring(1);
            } else {
                if (currentKey == null && i == args.length - 1) {
                    insertNewSettings(settings, loadConfigFile(arg), false);
                } else if (currentKey == null) {
                    throw new Exception("Found value without defined key!");
                } else {
                    values.add(arg);
                    settings.put(currentKey, values);
                    values = new ArrayList<>();
                    currentKey = null;
                }
            }
        }

        return settings;
    }

    private static Map<String, List<String>> loadConfigFile(String configJson) {
        String jsonStr = "";
        try {
            jsonStr = new String(Files.readAllBytes(Paths.get(configJson)));
        } catch (IOException e) {
            System.out.printf("Could not open config file %s. Caused by %s\n", configJson, e.getMessage());
        }

        JSONObject json = new JSONObject(jsonStr);
        Map<String, List<String>> settings = new HashMap<>();
        for (String key :
                json.keySet()) {
            List<String> values = new ArrayList<>();
            JSONArray valArray = json.optJSONArray(key);
            String valString = json.optString(key);
            if (valArray != null) {
                valArray.forEach((o -> values.add((String) o)));
            } else if (valString != null) {
                values.add(valString);
            }

            settings.put(key, values);
        }

        return settings;
    }

    private static void insertNewSettings(Map<String, List<String>> settings, Map<String, List<String>> newSettings, boolean overrideExisting) {
        for (String key :
                newSettings.keySet()) {
            if (!settings.containsKey(key) || overrideExisting) {
                settings.put(key, newSettings.get(key));
            }
        }
    }
}
