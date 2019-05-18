package bid.client.blockchain;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import javax.xml.bind.DatatypeConverter;
import java.io.*;

import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashSet;
import java.util.Set;


public class AppUser implements User {



    Participant participant;


    public AppUser(Participant participant) {
      this.participant = participant;
    }

    public String getName() {
        return this.participant.name;
    }

    public Set<String> getRoles() {
        return new HashSet<String>();
    }

    public String getAccount() {
        return "";
    }

    public String getAffiliation() {
        return "";
    }

    public Enrollment getEnrollment() {
        return new Enrollment() {

            @Override
            public PrivateKey getKey() {
                try {
                    return loadPrivateKey(getPrivateKey(participant.keyPath));
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getCert() {
                try {
                    return new String(Files.readAllBytes(Paths.get(participant.certPath, participant.certName)));
                } catch (Exception e) {
                    return null;
                }
            }

        };
    }

    public String getMspId() {
        return this.participant.mspId;
    }




    private static PrivateKey loadPrivateKey(Path fileName) throws IOException  {
        PrivateKey key = null;
        InputStream is = null;
        try {
            is = new FileInputStream(fileName.toAbsolutePath().toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            boolean inKey = false;
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!inKey) {
                    if (line.startsWith("-----BEGIN ") && line.endsWith(" PRIVATE KEY-----")) {
                        inKey = true;
                    }
                    continue;
                } else {
                    if (line.startsWith("-----END ") && line.endsWith(" PRIVATE KEY-----")) {
                        inKey = false;
                        break;
                    }
                    builder.append(line);
                }
            }
            //
            byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
            key = kf.generatePrivate(keySpec);
        } catch (Exception e) {
            System.out.println(e.getCause());
            e.printStackTrace();
        }
        finally {
            is.close();
        }
        return key;
    }


    private static Path getPrivateKey(String KeyPath) {

        try {
           Path path = Files.list(Paths.get(KeyPath)).findFirst().get();
           System.out.println(path);
           return path;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }



}
