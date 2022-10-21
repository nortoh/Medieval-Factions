/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package factionsplusplus.utils;

import factionsplusplus.services.DataService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class TabCompleteTools {
    public static Stream<String> getOnlinePlayers() {
		return Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName);
	}

    public static List<String> getAllFactions(DataService dataService) {
        List<String> factionNames = new ArrayList<String>();
        dataService.getFactions().forEach(faction -> factionNames.add(faction.getName()));
        return factionNames;
    }

	public static String joinArgsBeyond(int index, String delim, String[] args) {
		++index;
		String[] data = new String[args.length - index];
		System.arraycopy(args, index, data, 0, data.length);
		return String.join(delim, data);
	}

	public static List<String> filterStartingWith(String prefix, Stream<String> stream) {
		return stream.filter((s) -> {
			return s != null && !s.isEmpty() && s.toLowerCase().startsWith(prefix.toLowerCase());
		}).collect(Collectors.toList());
	}

	public static List<String> filterStartingWith(String prefix, Collection<String> strings) {
		return filterStartingWith(prefix, strings.stream());
	}

	public static List<String> tackOnBeginningAndEndQuotes(List<String> targetedList) {
		final List<String> changed = new ArrayList<>();
		for(String string : targetedList) {
			String changedString = '"' + string + '"';
			changed.add(changedString);
		}

		return changed;
	}

	public static String removeBeginningAndEndQuotes(String targetedString) {
		return targetedString.replace("\"", "");
	}

    public static List<String> filterStartingWithAddQuotes(String prefix, List<String> strings) {
        return filterStartingWith(prefix, tackOnBeginningAndEndQuotes(strings).stream());
    }

    public static List<String> completeSingleOption(String arg, String option) {
        return filterStartingWith(arg, Collections.singletonList(option));
    }

    public static List<String> allOnlinePlayersMatching(String arg) {
        return allOnlinePlayersMatching(arg, false);
    }

    public static List<String> allOnlinePlayersMatching(String arg, Boolean addCancel) {
        List<String> onlinePlayers = getOnlinePlayers().toList();
        if (addCancel) onlinePlayers.add("cancel");
        return filterStartingWith(arg, onlinePlayers.stream());
    }

    public static List<String> allFactionsMatching(String arg, DataService dataService) {
        return filterStartingWith(arg, getAllFactions(dataService));
    }

    public static List<String> completeMultipleOptions(String arg, String... parameters) {
        return filterStartingWith(arg, Arrays.asList(parameters));
    }
}