/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package permafrost.tundra.org.springframework.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Represents a set of character entity references defined by the
 * HTML 4.0 standard.
 *
 * <p>A complete description of the HTML 4.0 character set can be found
 * at http://www.w3.org/TR/html4/charset.html.
 *
 * @author Juergen Hoeller
 * @author Martin Kersten
 * @author Craig Andrews
 * @since 1.2.1
 */
class HtmlCharacterEntityReferences {
	/**
	 * Character Entity References defined by the HTML 4.0 standard.
	 * A complete description of the HTML 4.0 character set can be found at:
	 * http://www.w3.org/TR/html4/charset.html
	 */
	private static final Map<Integer, String> ENTITY_REFERENCES = new TreeMap<Integer, String>();

	static {
		// Character entity references for ISO 8859-1 characters

		ENTITY_REFERENCES.put(160, "nbsp");
		ENTITY_REFERENCES.put(161, "iexcl");
		ENTITY_REFERENCES.put(162, "cent");
		ENTITY_REFERENCES.put(163, "pound");
		ENTITY_REFERENCES.put(164, "curren");
		ENTITY_REFERENCES.put(165, "yen");
		ENTITY_REFERENCES.put(166, "brvbar");
		ENTITY_REFERENCES.put(167, "sect");
		ENTITY_REFERENCES.put(168, "uml");
		ENTITY_REFERENCES.put(169, "copy");
		ENTITY_REFERENCES.put(170, "ordf");
		ENTITY_REFERENCES.put(171, "laquo");
		ENTITY_REFERENCES.put(172, "not");
		ENTITY_REFERENCES.put(173, "shy");
		ENTITY_REFERENCES.put(174, "reg");
		ENTITY_REFERENCES.put(175, "macr");
		ENTITY_REFERENCES.put(176, "deg");
		ENTITY_REFERENCES.put(177, "plusmn");
		ENTITY_REFERENCES.put(178, "sup2");
		ENTITY_REFERENCES.put(179, "sup3");
		ENTITY_REFERENCES.put(180, "acute");
		ENTITY_REFERENCES.put(181, "micro");
		ENTITY_REFERENCES.put(182, "para");
		ENTITY_REFERENCES.put(183, "middot");
		ENTITY_REFERENCES.put(184, "cedil");
		ENTITY_REFERENCES.put(185, "sup1");
		ENTITY_REFERENCES.put(186, "ordm");
		ENTITY_REFERENCES.put(187, "raquo");
		ENTITY_REFERENCES.put(188, "frac14");
		ENTITY_REFERENCES.put(189, "frac12");
		ENTITY_REFERENCES.put(190, "frac34");
		ENTITY_REFERENCES.put(191, "iquest");
		ENTITY_REFERENCES.put(192, "Agrave");
		ENTITY_REFERENCES.put(193, "Aacute");
		ENTITY_REFERENCES.put(194, "Acirc");
		ENTITY_REFERENCES.put(195, "Atilde");
		ENTITY_REFERENCES.put(196, "Auml");
		ENTITY_REFERENCES.put(197, "Aring");
		ENTITY_REFERENCES.put(198, "AElig");
		ENTITY_REFERENCES.put(199, "Ccedil");
		ENTITY_REFERENCES.put(200, "Egrave");
		ENTITY_REFERENCES.put(201, "Eacute");
		ENTITY_REFERENCES.put(202, "Ecirc");
		ENTITY_REFERENCES.put(203, "Euml");
		ENTITY_REFERENCES.put(204, "Igrave");
		ENTITY_REFERENCES.put(205, "Iacute");
		ENTITY_REFERENCES.put(206, "Icirc");
		ENTITY_REFERENCES.put(207, "Iuml");
		ENTITY_REFERENCES.put(208, "ETH");
		ENTITY_REFERENCES.put(209, "Ntilde");
		ENTITY_REFERENCES.put(210, "Ograve");
		ENTITY_REFERENCES.put(211, "Oacute");
		ENTITY_REFERENCES.put(212, "Ocirc");
		ENTITY_REFERENCES.put(213, "Otilde");
		ENTITY_REFERENCES.put(214, "Ouml");
		ENTITY_REFERENCES.put(215, "times");
		ENTITY_REFERENCES.put(216, "Oslash");
		ENTITY_REFERENCES.put(217, "Ugrave");
		ENTITY_REFERENCES.put(218, "Uacute");
		ENTITY_REFERENCES.put(219, "Ucirc");
		ENTITY_REFERENCES.put(220, "Uuml");
		ENTITY_REFERENCES.put(221, "Yacute");
		ENTITY_REFERENCES.put(222, "THORN");
		ENTITY_REFERENCES.put(223, "szlig");
		ENTITY_REFERENCES.put(224, "agrave");
		ENTITY_REFERENCES.put(225, "aacute");
		ENTITY_REFERENCES.put(226, "acirc");
		ENTITY_REFERENCES.put(227, "atilde");
		ENTITY_REFERENCES.put(228, "auml");
		ENTITY_REFERENCES.put(229, "aring");
		ENTITY_REFERENCES.put(230, "aelig");
		ENTITY_REFERENCES.put(231, "ccedil");
		ENTITY_REFERENCES.put(232, "egrave");
		ENTITY_REFERENCES.put(233, "eacute");
		ENTITY_REFERENCES.put(234, "ecirc");
		ENTITY_REFERENCES.put(235, "euml");
		ENTITY_REFERENCES.put(236, "igrave");
		ENTITY_REFERENCES.put(237, "iacute");
		ENTITY_REFERENCES.put(238, "icirc");
		ENTITY_REFERENCES.put(239, "iuml");
		ENTITY_REFERENCES.put(240, "eth");
		ENTITY_REFERENCES.put(241, "ntilde");
		ENTITY_REFERENCES.put(242, "ograve");
		ENTITY_REFERENCES.put(243, "oacute");
		ENTITY_REFERENCES.put(244, "ocirc");
		ENTITY_REFERENCES.put(245, "otilde");
		ENTITY_REFERENCES.put(246, "ouml");
		ENTITY_REFERENCES.put(247, "divide");
		ENTITY_REFERENCES.put(248, "oslash");
		ENTITY_REFERENCES.put(249, "ugrave");
		ENTITY_REFERENCES.put(250, "uacute");
		ENTITY_REFERENCES.put(251, "ucirc");
		ENTITY_REFERENCES.put(252, "uuml");
		ENTITY_REFERENCES.put(253, "yacute");
		ENTITY_REFERENCES.put(254, "thorn");
		ENTITY_REFERENCES.put(255, "yuml");

		// Character entity references for symbols, mathematical symbols, and Greek letters

		ENTITY_REFERENCES.put(402, "fnof");
		ENTITY_REFERENCES.put(913, "Alpha");
		ENTITY_REFERENCES.put(914, "Beta");
		ENTITY_REFERENCES.put(915, "Gamma");
		ENTITY_REFERENCES.put(916, "Delta");
		ENTITY_REFERENCES.put(917, "Epsilon");
		ENTITY_REFERENCES.put(918, "Zeta");
		ENTITY_REFERENCES.put(919, "Eta");
		ENTITY_REFERENCES.put(920, "Theta");
		ENTITY_REFERENCES.put(921, "Iota");
		ENTITY_REFERENCES.put(922, "Kappa");
		ENTITY_REFERENCES.put(923, "Lambda");
		ENTITY_REFERENCES.put(924, "Mu");
		ENTITY_REFERENCES.put(925, "Nu");
		ENTITY_REFERENCES.put(926, "Xi");
		ENTITY_REFERENCES.put(927, "Omicron");
		ENTITY_REFERENCES.put(928, "Pi");
		ENTITY_REFERENCES.put(929, "Rho");
		ENTITY_REFERENCES.put(931, "Sigma");
		ENTITY_REFERENCES.put(932, "Tau");
		ENTITY_REFERENCES.put(933, "Upsilon");
		ENTITY_REFERENCES.put(934, "Phi");
		ENTITY_REFERENCES.put(935, "Chi");
		ENTITY_REFERENCES.put(936, "Psi");
		ENTITY_REFERENCES.put(937, "Omega");
		ENTITY_REFERENCES.put(945, "alpha");
		ENTITY_REFERENCES.put(946, "beta");
		ENTITY_REFERENCES.put(947, "gamma");
		ENTITY_REFERENCES.put(948, "delta");
		ENTITY_REFERENCES.put(949, "epsilon");
		ENTITY_REFERENCES.put(950, "zeta");
		ENTITY_REFERENCES.put(951, "eta");
		ENTITY_REFERENCES.put(952, "theta");
		ENTITY_REFERENCES.put(953, "iota");
		ENTITY_REFERENCES.put(954, "kappa");
		ENTITY_REFERENCES.put(955, "lambda");
		ENTITY_REFERENCES.put(956, "mu");
		ENTITY_REFERENCES.put(957, "nu");
		ENTITY_REFERENCES.put(958, "xi");
		ENTITY_REFERENCES.put(959, "omicron");
		ENTITY_REFERENCES.put(960, "pi");
		ENTITY_REFERENCES.put(961, "rho");
		ENTITY_REFERENCES.put(962, "sigmaf");
		ENTITY_REFERENCES.put(963, "sigma");
		ENTITY_REFERENCES.put(964, "tau");
		ENTITY_REFERENCES.put(965, "upsilon");
		ENTITY_REFERENCES.put(966, "phi");
		ENTITY_REFERENCES.put(967, "chi");
		ENTITY_REFERENCES.put(968, "psi");
		ENTITY_REFERENCES.put(969, "omega");
		ENTITY_REFERENCES.put(977, "thetasym");
		ENTITY_REFERENCES.put(978, "upsih");
		ENTITY_REFERENCES.put(982, "piv");
		ENTITY_REFERENCES.put(8226, "bull");
		ENTITY_REFERENCES.put(8230, "hellip");
		ENTITY_REFERENCES.put(8242, "prime");
		ENTITY_REFERENCES.put(8243, "Prime");
		ENTITY_REFERENCES.put(8254, "oline");
		ENTITY_REFERENCES.put(8260, "frasl");
		ENTITY_REFERENCES.put(8472, "weierp");
		ENTITY_REFERENCES.put(8465, "image");
		ENTITY_REFERENCES.put(8476, "real");
		ENTITY_REFERENCES.put(8482, "trade");
		ENTITY_REFERENCES.put(8501, "alefsym");
		ENTITY_REFERENCES.put(8592, "larr");
		ENTITY_REFERENCES.put(8593, "uarr");
		ENTITY_REFERENCES.put(8594, "rarr");
		ENTITY_REFERENCES.put(8595, "darr");
		ENTITY_REFERENCES.put(8596, "harr");
		ENTITY_REFERENCES.put(8629, "crarr");
		ENTITY_REFERENCES.put(8656, "lArr");
		ENTITY_REFERENCES.put(8657, "uArr");
		ENTITY_REFERENCES.put(8658, "rArr");
		ENTITY_REFERENCES.put(8659, "dArr");
		ENTITY_REFERENCES.put(8660, "hArr");
		ENTITY_REFERENCES.put(8704, "forall");
		ENTITY_REFERENCES.put(8706, "part");
		ENTITY_REFERENCES.put(8707, "exist");
		ENTITY_REFERENCES.put(8709, "empty");
		ENTITY_REFERENCES.put(8711, "nabla");
		ENTITY_REFERENCES.put(8712, "isin");
		ENTITY_REFERENCES.put(8713, "notin");
		ENTITY_REFERENCES.put(8715, "ni");
		ENTITY_REFERENCES.put(8719, "prod");
		ENTITY_REFERENCES.put(8721, "sum");
		ENTITY_REFERENCES.put(8722, "minus");
		ENTITY_REFERENCES.put(8727, "lowast");
		ENTITY_REFERENCES.put(8730, "radic");
		ENTITY_REFERENCES.put(8733, "prop");
		ENTITY_REFERENCES.put(8734, "infin");
		ENTITY_REFERENCES.put(8736, "ang");
		ENTITY_REFERENCES.put(8743, "and");
		ENTITY_REFERENCES.put(8744, "or");
		ENTITY_REFERENCES.put(8745, "cap");
		ENTITY_REFERENCES.put(8746, "cup");
		ENTITY_REFERENCES.put(8747, "int");
		ENTITY_REFERENCES.put(8756, "there4");
		ENTITY_REFERENCES.put(8764, "sim");
		ENTITY_REFERENCES.put(8773, "cong");
		ENTITY_REFERENCES.put(8776, "asymp");
		ENTITY_REFERENCES.put(8800, "ne");
		ENTITY_REFERENCES.put(8801, "equiv");
		ENTITY_REFERENCES.put(8804, "le");
		ENTITY_REFERENCES.put(8805, "ge");
		ENTITY_REFERENCES.put(8834, "sub");
		ENTITY_REFERENCES.put(8835, "sup");
		ENTITY_REFERENCES.put(8836, "nsub");
		ENTITY_REFERENCES.put(8838, "sube");
		ENTITY_REFERENCES.put(8839, "supe");
		ENTITY_REFERENCES.put(8853, "oplus");
		ENTITY_REFERENCES.put(8855, "otimes");
		ENTITY_REFERENCES.put(8869, "perp");
		ENTITY_REFERENCES.put(8901, "sdot");
		ENTITY_REFERENCES.put(8968, "lceil");
		ENTITY_REFERENCES.put(8969, "rceil");
		ENTITY_REFERENCES.put(8970, "lfloor");
		ENTITY_REFERENCES.put(8971, "rfloor");
		ENTITY_REFERENCES.put(9001, "lang");
		ENTITY_REFERENCES.put(9002, "rang");
		ENTITY_REFERENCES.put(9674, "loz");
		ENTITY_REFERENCES.put(9824, "spades");
		ENTITY_REFERENCES.put(9827, "clubs");
		ENTITY_REFERENCES.put(9829, "hearts");
		ENTITY_REFERENCES.put(9830, "diams");

		// Character entity references for markup-significant and internationalization characters

		ENTITY_REFERENCES.put(34, "quot");
		ENTITY_REFERENCES.put(38, "amp");
		ENTITY_REFERENCES.put(39, "#39");
		ENTITY_REFERENCES.put(60, "lt");
		ENTITY_REFERENCES.put(62, "gt");
		ENTITY_REFERENCES.put(338, "OElig");
		ENTITY_REFERENCES.put(339, "oelig");
		ENTITY_REFERENCES.put(352, "Scaron");
		ENTITY_REFERENCES.put(353, "scaron");
		ENTITY_REFERENCES.put(376, "Yuml");
		ENTITY_REFERENCES.put(710, "circ");
		ENTITY_REFERENCES.put(732, "tilde");
		ENTITY_REFERENCES.put(8194, "ensp");
		ENTITY_REFERENCES.put(8195, "emsp");
		ENTITY_REFERENCES.put(8201, "thinsp");
		ENTITY_REFERENCES.put(8204, "zwnj");
		ENTITY_REFERENCES.put(8205, "zwj");
		ENTITY_REFERENCES.put(8206, "lrm");
		ENTITY_REFERENCES.put(8207, "rlm");
		ENTITY_REFERENCES.put(8211, "ndash");
		ENTITY_REFERENCES.put(8212, "mdash");
		ENTITY_REFERENCES.put(8216, "lsquo");
		ENTITY_REFERENCES.put(8217, "rsquo");
		ENTITY_REFERENCES.put(8218, "sbquo");
		ENTITY_REFERENCES.put(8220, "ldquo");
		ENTITY_REFERENCES.put(8221, "rdquo");
		ENTITY_REFERENCES.put(8222, "bdquo");
		ENTITY_REFERENCES.put(8224, "dagger");
		ENTITY_REFERENCES.put(8225, "Dagger");
		ENTITY_REFERENCES.put(8240, "permil");
		ENTITY_REFERENCES.put(8249, "lsaquo");
		ENTITY_REFERENCES.put(8250, "rsaquo");
		ENTITY_REFERENCES.put(8364, "euro");
	}

	static final char REFERENCE_START = '&';
	static final String DECIMAL_REFERENCE_START = "&#";
	static final String HEX_REFERENCE_START = "&#x";
	static final char REFERENCE_END = ';';
	static final char CHAR_NULL = (char) -1;

	private final String[] characterToEntityReferenceMap = new String[3000];
	private final Map<String, Character> entityReferenceToCharacterMap = new HashMap<String, Character>(252);

	/**
	 * Returns a new set of character entity references reflecting the HTML 4.0 character set.
	 */
	public HtmlCharacterEntityReferences() {
		for (Map.Entry<Integer, String> entry : ENTITY_REFERENCES.entrySet()) {
			int referredChar = entry.getKey();
			String reference = entry.getValue();

			if (!(referredChar < 1000 || (referredChar >= 8000 && referredChar < 10000))) {
				throw new IllegalArgumentException("Invalid reference to special HTML entity: " + referredChar);
			}
			int index = (referredChar < 1000 ? referredChar : referredChar - 7000);

			this.characterToEntityReferenceMap[index] = REFERENCE_START + reference + REFERENCE_END;
			this.entityReferenceToCharacterMap.put(reference, (char) referredChar);
		}
	}

	/**
	 * Return the number of supported entity references.
	 */
	public int getSupportedReferenceCount() {
		return this.entityReferenceToCharacterMap.size();
	}

	/**
	 * Return true if the given character is mapped to a supported entity reference.
	 */
	public boolean isMappedToReference(char character) {
		return isMappedToReference(character, HtmlUtils.DEFAULT_CHARACTER_ENCODING);
	}

	/**
	 * Return true if the given character is mapped to a supported entity reference.
	 */
	public boolean isMappedToReference(char character, String encoding) {
		return (convertToReference(character, encoding) != null);
	}

	/**
	 * Return the reference mapped to the given character, or {@code null} if none found.
	 */
	public String convertToReference(char character) {
	   return convertToReference(character, HtmlUtils.DEFAULT_CHARACTER_ENCODING);
	}

	/**
	 * Return the reference mapped to the given character, or {@code null} if none found.
	 * @since 4.1.2
	 */
	public String convertToReference(char character, String encoding) {
		if (encoding.startsWith("UTF-")){
			switch (character){
				case '<':
					return "&lt;";
				case '>':
					return "&gt;";
				case '"':
					return "&quot;";
				case '&':
					return "&amp;";
				case '\'':
					return "&#39;";
			}
		}
		else if (character < 1000 || (character >= 8000 && character < 10000)) {
			int index = (character < 1000 ? character : character - 7000);
			String entityReference = this.characterToEntityReferenceMap[index];
			if (entityReference != null) {
				return entityReference;
			}
		}
		return null;
	}

	/**
	 * Return the char mapped to the given entityReference or -1.
	 */
	public char convertToCharacter(String entityReference) {
		Character referredCharacter = this.entityReferenceToCharacterMap.get(entityReference);
		if (referredCharacter != null) {
			return referredCharacter;
		}
		return CHAR_NULL;
	}

}
