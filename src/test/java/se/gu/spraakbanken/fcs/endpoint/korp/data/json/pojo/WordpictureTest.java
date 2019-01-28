package se.gu.spraakbanken.fcs.endpoint.korp.data.json.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gu.spraakbanken.fcs.endpoint.korp.data.json.pojo.wordpicture.Relation;
import se.gu.spraakbanken.fcs.endpoint.korp.data.json.pojo.wordpicture.Wordpicture;

public class WordpictureTest {
    private String jsonString =
            "{\"relations\":[{\"dep\":\"pappa..nn.1\",\"depextra\":\"\",\"deppos\":\"NN\",\"freq\":20,\"head\":\"ge..vb.1\",\"headpos\":\"VB\",\"mi\":22.20880511167489,\"rel\":\"SS\",\"source\":[\"ROMI:230203\"]}],\"time\":0.002602100372314453}";
    private static final Logger LOG = LogManager.getLogger(WordpictureTest.class);


    @Test
    public void wordpictureSerialize() {
        ObjectMapper mapper = new ObjectMapper();

        Wordpicture wp = new Wordpicture();
        Relation rel = new Relation();
        // {\"dep\":\"pappa..nn.1\",\"depextra\":\"\",\"deppos\":\"NN\",\"freq\":20,\"head\":\"ge..vb.1\",\"headpos\":\"VB\",\"mi\":22.20880511167489,\"rel\":\"SS\",\"source\":[\"ROMI:230203\"]}]
        rel.setDep("pappa..nn.1");
        rel.setDepextra("");
        rel.setDeppos("NN");
        rel.setFreq(new Integer("20"));
        rel.setHead("ge..vb.1");
        rel.setHeadpos("VB");
        rel.setMi(new Double("22.20880511167489"));
        rel.setRel("SS");
        List ss = new ArrayList<String>();
        ss.add("ROMI:230203");
        rel.setSource(ss);
        List relList = new ArrayList<Relation>();
        relList.add(rel);
        wp.setRelations(relList);
        wp.setTime(new Double("0.002602100372314453"));

        String s = null;
        try {
            s = mapper.writeValueAsString(wp);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOG.info(s);
        assertEquals(jsonString, s);
    }

    @Test
    public void wordpictureDeserialize() {
        ObjectMapper mapper = new ObjectMapper();

        Wordpicture wp2 = null;
        String roundTripString = "";

        try {
            wp2 = mapper.readerFor(Wordpicture.class).readValue(jsonString);
            roundTripString = mapper.writeValueAsString(wp2);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOG.info(roundTripString);
        assertEquals(jsonString, roundTripString);
        assertEquals(wp2.getTime(), new Double("0.002602100372314453"));

    }

    @Ignore("Word picture not currently supported.")
    @Test
    public void wordpictureDeserializeURL() {
        ObjectMapper mapper = new ObjectMapper();

        Wordpicture wp4 = null;
        String roundTripString = "";
        String wsString = "https://alf.hum.ku.dk/korp/cgi/korp.cgi?";
        String queryString =
                "command=relations&word=ge..vb.1&type=lemgram&corpus=LSPCONSTRUCTIONEB1";

        try {
            URL korp = new URL(wsString + queryString);

            wp4 = mapper.readerFor(Wordpicture.class).readValue(korp.openStream());
            roundTripString = mapper.writeValueAsString(wp4);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOG.info(roundTripString);
        // assertEquals(jsonString, roundTripString);
        assertEquals("ROMIROMI:230203", wp4.getRelations().get(0).getSource().get(0));
        assertNotNull(wp4.getTime());

    }

    public static void main(String[] args) {
    }
}
