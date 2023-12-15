package wyelight.hungerless.config;

import wyelight.hungerless.Constants;

import java.io.*;
import java.util.Properties;

public class Config {
    protected static final Properties defaultConfig = new Properties();
    public static String fileName;
    public static boolean movementRework = true;
    public static boolean autoSwim = true;

    public Config(String fileName){
        Config.fileName = fileName;
    }

    public static void read() {
        Properties properties = new Properties(defaultConfig);
        try {
            FileReader configReader = new FileReader(fileName);
            properties.load(configReader);
            configReader.close();
        } catch (FileNotFoundException ignored) {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        movementRework = parseIntConfig(properties.getProperty(Constants.MOVEMENT_REWORK), 1) != 0;
        autoSwim = parseIntConfig(properties.getProperty(Constants.AUTO_SWIM), 1) != 0;
    }

    private static int parseIntConfig(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    public static void save() {
        try {
            File config = new File(fileName);
            //boolean existed = config.exists();
            File parentDir = config.getParentFile();
            if (!parentDir.exists())
                parentDir.mkdirs();

            FileWriter configWriter = new FileWriter(config);

            writeBoolean(configWriter, Constants.MOVEMENT_REWORK, movementRework);
            writeBoolean(configWriter, Constants.AUTO_SWIM, autoSwim);
            configWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeString(FileWriter configWriter, String name, String value) throws IOException {
        configWriter.write(name + '=' + value + '\n');
    }

    private static void writeBoolean(FileWriter configWriter, String name, boolean value) throws IOException {
        writeString(configWriter, name, value ? "1" : "0");
    }

    static {
        defaultConfig.setProperty(Constants.MOVEMENT_REWORK, "1");
        defaultConfig.setProperty(Constants.AUTO_SWIM, "1");
    }
}
