package com.miykeal.showCaseStandalone.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;

public class TermLoader {
	private static final String regexFilter	= "(\\s+?|.+?)[ ]*:[ ]*\\\"(\\s+?|.+?)\\\"";
	private static final String regexKey	= "$1";
	private static final String regexValue	= "$2";
	private static final String encoding	= "UTF8";
	
	private TermLoader () {}
	
	public static void loadTerms (String filename) throws IOException {
		
		File					file	= new File				(ShowCaseStandalone.get().getDataFolder(), filename);
		FileInputStream			fis		= new FileInputStream	(file);
		InputStreamReader		isr		= new InputStreamReader	(fis, encoding);
		BufferedReader			br		= new BufferedReader	(isr);
		
		String					line	= null;
		HashMap<String, String>	terms	= new HashMap<String, String>();
		
		while ((line = br.readLine()) != null) {
			String key		= line.replaceAll(regexFilter, regexKey);
			String value	= line.replaceAll(regexFilter, regexValue);

			terms.put(key, value);
		}
		
		br.close();
		isr.close();
		fis.close();
		
		for (String key : terms.keySet()) {
			for (Term term : Term.values())
				if (term.toString().equals(key))
					term.setTerm(terms.get(key));
		}
		
		for (Term term : Term.values())	// Searching for missing terms
			if (term.get() == null)
				ShowCaseStandalone.slog(Level.WARNING, "Undefined Term: "+term.toString());
	}
}
