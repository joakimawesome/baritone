/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.fluent.Request;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WynnAPI {

    public static Map<String, Integer> getProfData(String api, String charClass, int charLevel) throws IOException {
        String minecraftName = getMinecraftName();
        String result = Request.Get(api + "player/" + minecraftName + "/stats").execute().returnContent().asString();
        JsonObject data = JsonParser.parseString(result).getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
        JsonObject characters = data.getAsJsonObject("characters");
        JsonObject selectedChar = null;

        for (Map.Entry<String, JsonElement> entry : characters.entrySet()) {
            JsonObject charObj = entry.getValue().getAsJsonObject();
            if (charObj.get("type").getAsString().equals(charClass) && charObj.get("level").getAsInt() == charLevel) {
                selectedChar = charObj;
                break;
            }
        }

        if (selectedChar == null) {
            throw new IllegalArgumentException("Character of type " + charClass + " and level " + charLevel + " not found.");
        }

        JsonObject profs = selectedChar.getAsJsonObject("professions");
        Map<String, Integer> gatherProfs = new HashMap<>();
        Set<String> allowedProfs = Set.of("farming", "fishing", "mining", "woodworking");

        profs.entrySet().stream()
                .filter(entry -> allowedProfs.contains(entry.getKey()))
                .forEach(entry -> gatherProfs.put(entry.getKey(), entry.getValue().getAsJsonObject().get("level").getAsInt()));

        return gatherProfs;
    }

    private static String getMinecraftName() {
        String userHome = System.getProperty("user.home");
        Path minecraftPath = Paths.get(userHome, "AppData", "Roaming", ".minecraft", "usercache.json");
        try (FileReader reader = new FileReader(minecraftPath.toString())) {
            JsonArray userCacheArray = JsonParser.parseReader(reader).getAsJsonArray();
            JsonObject firstUser = userCacheArray.get(0).getAsJsonObject();
            return firstUser.get("name").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, Integer> profData = getProfData("https://api.wynncraft.com/v2/", "HUNTER", 657);
        System.out.println(profData);
    }
}

