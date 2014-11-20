/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntax;

/**
 *
 * @author smaug
 */
public class HandcraftedScript implements Script {

    private File copy(String resource) throws IOException {
        File f = File.createTempFile("fca", null);
        f.deleteOnExit();
        try (InputStream input = HandcraftedScript.class.getClassLoader().getResourceAsStream(resource); FileOutputStream output = new FileOutputStream(f)) {
            IOUtils.copy(input, output);
        }
        return f;
    }

    @Override
    public List<File> getOntologies() {
        try {
            return Arrays.asList(copy("musicontology.n3"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<String> getAttributes() {
        /*
         mo:MusicArtist	3505
         mo:Record	5786
         mo:Lyrics	8823
         mo:Torrent	11572
         mo:ED2K	11572
         mo:Medium	23144
         mo:Signal	45634
         mo:Track	45634
         mo:Performance	45634
         mo:MusicalManifestation	51420
         mo:MusicalExpression	54457
         mo:Playlist	102804
         mo:MusicalItem	125948
         */
        return Arrays.asList("MusicalItem", "Playlist", "Track", "Signal", "Medium", "ED2K", "Torrent");
    }

    @Override
    public boolean getGenerateNegation() {
        return true;
    }

    @Override
    public boolean getGenerateDomain() {
        return false;
    }

    @Override
    public boolean getGenerateRange() {
        return false;
    }

    @Override
    public int getClassifier() {
        return 8;
    }

    @Override
    public boolean shouldRun() {
        return true;
    }

    @Override
    public boolean shouldLockLOD() {
        return true;
    }

    @Override
    public void submitLog(File logfile) {
        try {
            HttpPost post = new HttpPost("http://semantic.cs.put.poznan.pl/fca/submit.php");
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", logfile)
                    .build();
            post.setEntity(entity);
            try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(post)) {
                System.err.println(response);
                System.err.println(EntityUtils.toString(response.getEntity()));
            }
        } catch (Throwable ex) {
            Logger.getLogger(HandcraftedScript.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final static int[] CALCULATORS = new int[]{1};

    @Override
    public int[] getCalculators() {
        return CALCULATORS;
    }

}
