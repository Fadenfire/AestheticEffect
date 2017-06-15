package silly511.aestheticeffect.helpers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public final class StringHelper {
	
	public static String[] splitIngoreQuotes(char splitBy, String input) {
		List<String> list = Lists.newArrayList();
		StringBuilder builder = new StringBuilder();
		boolean isInsideQuotes = false;
		
		for (char c : input.toCharArray())
			if (c == splitBy && !isInsideQuotes) {
				list.add(builder.toString());
				builder = new StringBuilder();
			} else if (c == '"') {
				isInsideQuotes = !isInsideQuotes;
				builder.append(c);
			} else builder.append(c);
		
		list.add(builder.toString());
		return list.toArray(new String[0]);
	}
	
	public static String replaceIngoreQuotes(String regex, String replacement, String input) {
		Matcher m = Pattern.compile(regex).matcher(input);
		
		if (m.find()) {
			StringBuilder builder = new StringBuilder();
			boolean isInsideQuotes = false, done = false;
			
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				
				if (!done && i == m.start()) {
					if (!isInsideQuotes) {
						builder.append(String.format(replacement, m.group()));
						i += m.end() - m.start() - 1;
					} else builder.append(c);
					
					if (!m.find()) done = true;
				} else if (c == '"') {
					isInsideQuotes = !isInsideQuotes;
					builder.append(c);
				} else builder.append(c);
			}
			
			return builder.toString();
		}
		
		return input;
	}

}
