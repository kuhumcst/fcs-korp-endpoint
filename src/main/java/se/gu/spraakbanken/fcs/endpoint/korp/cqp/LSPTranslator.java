/**
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
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

public class LSPTranslator {
    private static final Map<String, List<String>> TO_LSP = createToLsp();
    private static final Map<String, List<String>> TO_UD20 = createToUd20();

    private static Map<String, List<String>> createToLsp() {
	Map<String, List<String>> lsp = new HashMap<String, List<String>>();

	//DK-CLARIN
	lsp.put("NOUN", Arrays.asList("N"));
	lsp.put("PROPN", Arrays.asList("PROPN"));
	lsp.put("ADJ", Arrays.asList("ADJ"));
	lsp.put("VERB", Arrays.asList("V")); //  "vb" ?
	lsp.put("NUM", Arrays.asList("NUM"));
	lsp.put("PRON", Arrays.asList("PRON"));
	lsp.put("ADV", Arrays.asList("ADV")); // "AB" ?
	lsp.put("ADP", Arrays.asList("PREP"));
	lsp.put("CCONJ", Arrays.asList("CONJ", "SCONJ"));
	lsp.put("INTJ", Arrays.asList("INTERJ"));
	lsp.put("X", Arrays.asList("OTHER", "UNIK"));
	
	// Map remaining UD PoS-codes AUX, PART, DET, PUNCT, SYM 

	return Collections.unmodifiableMap(lsp);
    }

    private static Map<String, List<String>> createToUd20() {
	Map<String, List<String>> ud20 = new HashMap<String, List<String>>();
	
	//DK-CLARIN
	ud20.put("N", Arrays.asList("NOUN"));
	ud20.put("PROPN", Arrays.asList("PROPN"));
	ud20.put("V", Arrays.asList("VERB")); // "vb" ?
	ud20.put("ADJ", Arrays.asList("ADJ"));
	ud20.put("NUM", Arrays.asList("NUM"));
	ud20.put("PRON", Arrays.asList("PRON"));
	ud20.put("ADV", Arrays.asList("ADV"));
	ud20.put("PREP", Arrays.asList("ADP"));
	ud20.put("CONJ", Arrays.asList("CCONJ", "SCONJ"));
	ud20.put("INTERJ", Arrays.asList("INTJ"));
	ud20.put("OTHER", Arrays.asList("X"));

	// Map remaining UD PoS-codes AUX, PART, DET, PUNCT, SYM 

	return Collections.unmodifiableMap(ud20);
    }

    /*
     * @param ud20Pos The UD-20 PoS code
     * @return A list of translated codes in LSP PoS.
     *
     */
    public static List<String> toLSP(final String ud20Pos) throws SRUException {
	List<String> res = null;
	
	res = TO_LSP.get(ud20Pos.toUpperCase());
	
	if (res == null) {
	    throw new SRUException(
				   SRUConstants.SRU_QUERY_SYNTAX_ERROR,
				   "unknown UD-20 PoS code in query: " + ud20Pos);
	}
	return res;
    }

    /*
     * @param lspPos The LSP PoS code
     * @return A list of translated codes in UD-20 PoS.
     *
     */
    public static List<String> fromLSP(final String lspPos) throws SRUException {
	List<String> res = null;
	int iop = lspPos.indexOf(".");
 
	res = TO_UD20.get((iop != -1 ? lspPos.substring(0, iop) : lspPos).toUpperCase());
	if (res == null) {
	    throw new SRUException(
				   SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN,
				   "unknown PoS code from search engine: " + (iop != -1 ? lspPos.substring(0, iop) : lspPos));
	}
	return res;
    }
}
