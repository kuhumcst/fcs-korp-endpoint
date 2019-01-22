package se.gu.spraakbanken.fcs.endpoint.korp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import eu.clarin.sru.server.CQLQueryParser;
import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUQueryParserRegistry;
import eu.clarin.sru.server.SRUServerConfig;
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.FCSQueryParser;
import eu.clarin.sru.server.fcs.ResourceInfo;
import eu.clarin.sru.server.fcs.utils.SimpleEndpointDescriptionParser;
import eu.clarin.sru.server.utils.SRUServerServlet;
import se.gu.spraakbanken.fcs.endpoint.korp.cqp.FCSToCQPConverter;
import se.gu.spraakbanken.fcs.endpoint.korp.data.json.pojo.info.CorporaInfo;
import se.gu.spraakbanken.fcs.endpoint.korp.data.json.pojo.query.Query;

public class KorpEndpointSearchEngineTest {
	private static SRUServerConfig config;
	private static EndpointDescription sed;
	private static KorpEndpointSearchEngine kese;
	private static ServletTester tester;
	private static ServletHolder holder;
	private static HashMap<String, String> params;

	@BeforeClass
	public static void parseEndpointDescription() throws SRUConfigException {
		try {
			sed = SimpleEndpointDescriptionParser.parse(new File(
					"target/test-classes/se/gu/spraakbanken/fcs/endpoint/korp/korp-endpoint-description-test.xml")
							.toURI().toURL());
			assertEquals("http://clarin.eu/fcs/capability/basic-search",
					sed.getCapabilities().get(0).toString());
			assertEquals("http://clarin.eu/fcs/capability/advanced-search",
					sed.getCapabilities().get(1).toString());
		} catch (MalformedURLException mue) {
			throw new SRUConfigException("Malformed URL");
		}

		tester = new ServletTester();
		// tester.setContextPath("/");
		tester.setContextPath("http://localhost:8082/sru-server");
		tester.setResourceBase("src/main/webapp");
		tester.setClassLoader(SRUServerServlet.class.getClassLoader());

		holder = tester.addServlet(SRUServerServlet.class, "/sru");

		params = new HashMap<String, String>();
		params.put(SRUServerConfig.SRU_TRANSPORT, "http");
		params.put(SRUServerConfig.SRU_HOST, "127.0.0.1");
		params.put(SRUServerConfig.SRU_PORT, "8082");
		params.put(SRUServerConfig.SRU_DATABASE, "sru-server");
		params.put(SRUServerServlet.SRU_SERVER_CONFIG_LOCATION_PARAM,
				"src/main/webapp/WEB-INF/sru-server-config.xml");

		// try {
		// String baseUrl = tester.createSocketConnector(true);
		// } catch (ServletException e) {
		// throw new SRUConfigException("Failed to set context attribute.");
		// } catch (Exception e) {
		// throw new SRUConfigException("Failed to create socket connector.");
		// }
		try {
			// tester.setAttribute(SRUServerServlet.SRU_SERVER_CONFIG_LOCATION_PARAM,
			// "src/main/webapp/WEB-INF/sru-server-config.xml");
			System.out.println("tester.getAttribute():"
					+ tester.getAttribute(SRUServerServlet.SRU_SERVER_CONFIG_LOCATION_PARAM));
			// System.out.println(holder.getServlet().getServletConfig().getInitParameter(SRUServerServlet.SRU_SERVER_CONFIG_LOCATION_PARAM));
			tester.start();

		} catch (Exception e) {
			throw new SRUConfigException("Failed to start servlet.");
		}
	}

	@Before
	public void doInit() throws SRUConfigException, ServletException {
		URL url;
		try {
			url = // SRUServerServlet.class.getClassLoader().getResource("META-INF/sru-server-config.xml");
					new File("src/main/webapp/WEB-INF/sru-server-config.xml").toURI().toURL();

		} catch (MalformedURLException mue) {
			throw new SRUConfigException("Malformed URL");
		}
		if (url == null) {
			throw new ServletException("not found, url == null");
		}
		// other runtime configuration, usually obtained from Servlet context

		config = SRUServerConfig.parse(params, url);
		kese = new KorpEndpointSearchEngine();

		System.out.println(config.getBaseUrl());
		System.out.println(config.getDatabase());

		// System.out.println(holder.getServlet().getServletInfo());

		kese.doInit(config, new SRUQueryParserRegistry.Builder().register(new FCSQueryParser()),
				params);

		assertNotNull(kese.getCorporaInfo());
		assertNotNull(kese.getCorporaInfo().getTime());
		// assertNotNull(kese.getCorporaInfo().getCorpus("PAROLE"));
	}

	@Test
	public void getCapabilitiesFromDescription() throws SRUConfigException {
		assertEquals("http://clarin.eu/fcs/capability/basic-search",
				sed.getCapabilities().get(0).toString());
		assertEquals("http://clarin.eu/fcs/capability/advanced-search",
				sed.getCapabilities().get(1).toString());
	}

	@Test
	public void getDataViewsFromDescription() throws SRUConfigException {
		System.out.println(sed.getSupportedDataViews());
		assertEquals("hits", sed.getSupportedDataViews().get(0).getIdentifier());
		assertEquals("SEND_BY_DEFAULT",
				sed.getSupportedDataViews().get(0).getDeliveryPolicy().toString());
		assertEquals("application/x-clarin-fcs-adv+xml",
				sed.getSupportedDataViews().get(1).getMimeType());
		assertEquals("application/x-cmdi+xml", sed.getSupportedDataViews().get(2).getMimeType());
		assertEquals("NEED_TO_REQUEST",
				sed.getSupportedDataViews().get(2).getDeliveryPolicy().toString());
	}

	@Test
	public void getLayersFromDescription() throws SRUConfigException {
		System.out.println(sed.getSupportedLayers());
		assertEquals("http://clarin.dk/ns/fcs/layer/word",
				sed.getSupportedLayers().get(0).getResultId().toString());
		assertEquals("lemma", sed.getSupportedLayers().get(1).getType().toString());
	}

	@Test
	public void getResourcesFromDescription() throws SRUException {
		List<ResourceInfo> riList = sed.getResourceList("hdl:20.500.12115/LSPkorpora");
		System.out.println(riList.get(0).getTitle());
		assertEquals("hits", riList.get(0).getAvailableDataViews().get(0).getIdentifier());
		assertEquals("SEND_BY_DEFAULT",
				riList.get(0).getAvailableDataViews().get(0).getDeliveryPolicy().toString());
		assertEquals("application/x-clarin-fcs-hits+xml",
				riList.get(0).getAvailableDataViews().get(0).getMimeType());
		assertEquals("https://repository.clarin.dk/repository/xmlui/handle/20.500.12115/9",
				riList.get(0).getLandingPageURI());
		assertTrue(riList.get(0).hasAvailableLayers());
		assertEquals("word", riList.get(0).getAvailableLayers().get(0).getId());
		assertEquals("text", riList.get(0).getAvailableLayers().get(0).getType());
		assertNull(riList.get(0).getAvailableLayers().get(0).getQualifier());
		assertEquals("dan", riList.get(0).getLanguages().get(0));
		assertFalse(riList.get(0).hasSubResources());
	}

	@Test
	public void convertCQL() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		// params.put("query", "text = 'användning' AND text = 'begränsad'");
		final String query = "text = 'rådgivningen er illustreret'";
		final String res = "[word = 'rådgivningen'][word = 'er'][word = 'illustreret']";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromCQL(
				(new CQLQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSSimple() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning' & pos = 'NOUN']";
		final String res = "[word = 'rådgivning' & pos = 'N']";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSLemma() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[lemma = 'Claus' & pos = 'PROPN']";
		final String res = "[lemma contains 'Claus' & pos = 'PROPN']";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSNot() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning' & pos != 'NOUN']";
		final String res = "[word = 'rådgivning' & pos != 'N']";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSWildcard() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning' & pos = 'NOUN'] [] [pos = 'ADJ'] ";
		final String res = "[word = 'rådgivning' & pos = 'N'] [] [pos = 'ADJ'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSRegexCaseInsensitive() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning'/c & pos = 'NOUN'] ";
		final String res = "[word = 'rådgivning' %c & pos = 'N'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res.trim(), resActual.trim());
	}

	@Test
	public void convertFCSRegexIgnoreDiacritics() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning'/d & pos = 'NOUN'] ";
		final String res = "[word = 'rådgivning' %d & pos = 'N'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res.trim(), resActual.trim());
	}

	@Test
	@Ignore
	public void convertFCSRegexLiteral() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = '?'/l & pos = 'X'] ";
		final String res = "[word = '?' %l & pos = 'OTHER'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		// This fails right now since you get d too!
		assertEquals(res.trim(), resActual.trim());
	}

	@Test
	public void convertFCSOccurs() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning' & pos = 'NOUN'] []{1,3} [pos = 'ADJ'] ";
		final String res = "[word = 'rådgivning' & pos = 'N'] []{1,3} [pos = 'ADJ'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void convertFCSOccursExact() throws SRUException {
		Map<String, String> params = new HashMap<String, String>();
		final String query = "[word = 'rådgivning' & pos = 'NOUN'] []{3} [pos = 'ADJ'] ";
		final String res = "[word = 'rådgivning' & pos = 'N'] []{3} [pos = 'ADJ'] ";
		params.put("query", query);
		SRUDiagnosticList diagnostics = new Diagnostic();
		final String resActual = FCSToCQPConverter.makeCQPFromFCS(
				(new FCSQueryParser()).parseQuery(SRUVersion.VERSION_2_0, params, diagnostics));

		System.out.println(resActual);
		assertEquals(res, resActual);
	}

	@Test
	public void search1() throws SRUException, SRUConfigException, XMLStreamException {
		SRUDiagnosticList diagnostics = new Diagnostic();

		CorporaInfo openCorporaInfo = kese.getCorporaInfo();

		final String query = "[word = 'og']";
		final String cqpQuery = "[word = 'og']";

		Query queryRes = kese.makeQuery(cqpQuery, openCorporaInfo, 1, 25);
		KorpSRUSearchResultSet kssrs =
				new KorpSRUSearchResultSet(config, diagnostics, queryRes, query, openCorporaInfo);
		StringWriter sw = new StringWriter();
		XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(sw);

		try {
			System.out.println("getCurrentRecordCursor 0: " + kssrs.getCurrentRecordCursor());
			if (kssrs.nextRecord()) {
				kssrs.writeRecord(xmlStreamWriter);
				System.out.println("search1: " + sw.toString());
			}
			xmlStreamWriter.flush();
			xmlStreamWriter.close();
		} catch (Exception ex) {
			xmlStreamWriter.close();
		}

		System.out.println("getHits: " + queryRes.getHits());
		System.out.println("getTotalRecordCount: " + kssrs.getTotalRecordCount());
		System.out.println("getRecordCount: " + kssrs.getRecordCount());
		System.out.println("getCurrentRecordCursor 1: " + kssrs.getCurrentRecordCursor());
		assertNotNull(sw.toString());
	}

	@AfterClass
	public static void cleanupServletContainer() throws Exception {
		tester.stop();
	}

	public class Diagnostic implements SRUDiagnosticList {
		@Override
		public void addDiagnostic(String uri, String details, String message) {
		}
	}

	public static void main(String[] args) {
	}
}
