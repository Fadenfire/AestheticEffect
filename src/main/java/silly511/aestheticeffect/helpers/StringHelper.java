package silly511.aestheticeffect.helpers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public final class StringHelper {
	
	/**
	 * Splits a string by a char, ignoring any chars inside " ".
	 * 
	 * @param splitBy Char to split by.
	 * @param input The string to split.
	 * 
	 * @return Array of strings. Each is trimmed.
	 */
	public static String[] splitIngoreQuotes(char splitBy, String input) {
		List<String> list = Lists.newArrayList();
		StringBuilder builder = new StringBuilder();
		boolean isInsideQuotes = false;
		
		for (char c : input.toCharArray()) {
			if (c == splitBy && !isInsideQuotes) {
				list.add(builder.toString().trim());
				builder = new StringBuilder();
				
				continue;
			} else if (c == '"')
				isInsideQuotes = !isInsideQuotes;
			
			builder.append(c);
		}
		
		list.add(builder.toString().trim());
		return list.toArray(new String[0]);
	}
	
	/**
	 * Replaces all instances of a regex in a string. Ignores any occurrences of the regex that's inside " ".
	 * 
	 * @param regex The regular expression.
	 * @param replacement The replacement.
	 * @param input The input string.
	 * 
	 * @return The resulting string.
	 */
	public static String replaceIngoreQuotes(String regex, String replacement, String input) {
		Matcher m = Pattern.compile(regex).matcher(input);
		
		if (m.find()) {
			StringBuilder builder = new StringBuilder();
			boolean isInsideQuotes = false;
			
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				
				if (i == m.start()) {
					if (!isInsideQuotes) {
						builder.append(String.format(replacement, m.group()));
						i += m.end() - m.start() - 1;
					}
					
					if (!m.find()) {
						builder.append(input.substring(++i));
						break;
					}
					
					if (!isInsideQuotes) continue;
				} else if (c == '"')
					isInsideQuotes = !isInsideQuotes;
				
				builder.append(c);
			}
			
			return builder.toString();
		}
		
		return input;
	}

}
