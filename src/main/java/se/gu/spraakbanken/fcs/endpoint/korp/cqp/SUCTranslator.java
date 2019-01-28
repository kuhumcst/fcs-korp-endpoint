/**
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt GNU General Public License v3
 */
package se.gu.spraakbanken.fcs.endpoint.korp.cqp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUException;

public class SUCTranslator {
	private static final Map<String, List<String>> TO_SUC = createToSuc();
	private static final Map<String, List<String>> TO_UD17 = createToUd17();

	private static Map<String, List<String>> createToSuc() {
		Map<String, List<String>> suc = new HashMap<String, List<String>>();
		suc.put("NOUN", Arrays.asList("N"));
		suc.put("PROPN", Arrays.asList("PROPN"));
		suc.put("ADJ", Arrays.asList("ADJ"));
		suc.put("VERB", Arrays.asList("V"));
		suc.put("NUM", Arrays.asList("NUM"));
		suc.put("PRON", Arrays.asList("PRON"));
		suc.put("ADV", Arrays.asList("ADV"));
		suc.put("ADP", Arrays.asList("PREP"));
		suc.put("CCONJ", Arrays.asList("CONJ"));
		suc.put("SCONJ", Arrays.asList("CONJ"));
		suc.put("INTJ", Arrays.asList("INTERJ"));
		suc.put("PART", Arrays.asList("UNIK"));
		suc.put("OTHER", Arrays.asList("X"));
		return Collections.unmodifiableMap(suc);
	}

	private static Map<String, List<String>> createToUd17() {
		Map<String, List<String>> ud17 = new HashMap<String, List<String>>();
		ud17.put("N", Arrays.asList("NOUN"));
		ud17.put("PROPN", Arrays.asList("PROPN"));
		ud17.put("V", Arrays.asList("VERB"));
		ud17.put("ADJ", Arrays.asList("ADJ"));
		ud17.put("NUM", Arrays.asList("NUM"));
		ud17.put("PRON", Arrays.asList("PRON"));
		ud17.put("ADV", Arrays.asList("ADV"));
		ud17.put("PREP", Arrays.asList("ADP"));
		ud17.put("CONJ", Arrays.asList("CCONJ", "SCONJ"));
		ud17.put("INTERJ", Arrays.asList("INTJ"));
		ud17.put("UNIK", Arrays.asList("PART"));
		ud17.put("X", Arrays.asList("OTHER"));

		return Collections.unmodifiableMap(ud17);
	}

	/*
	 * @param ud17Pos The UD-17 PoS code
	 * 
	 * @return A list of translated codes in SUC PoS.
	 *
	 */
	public static List<String> toSUC(final String ud17Pos) throws SRUException {
		List<String> res = null;

		res = TO_SUC.get(ud17Pos.toUpperCase());

		if (res == null) {
			throw new SRUException(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
					"unknown UD-17 PoS code in query: " + ud17Pos);
		}
		return res;
	}

	/*
	 * @param sucPos The SUC PoS code
	 * 
	 * @return A list of translated codes in UD-17 PoS.
	 *
	 */
	public static List<String> fromSUC(final String sucPos) throws SRUException {
		List<String> res = null;
		String pos = (sucPos != null) ? sucPos : "OTHER";

		int iop = pos.indexOf(".");
		res = TO_UD17.get((iop != -1 ? pos.substring(0, iop) : pos).toUpperCase());
		if (res == null) {
			throw new SRUException(SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN,
					"unknown PoS code from search engine: "
							+ (iop != -1 ? pos.substring(0, iop) : pos));
		}
		return res;
	}
}
