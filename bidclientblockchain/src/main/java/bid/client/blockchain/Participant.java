package bid.client.blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public enum Participant {

    GOV("gov.example.com",
            "GovMSP",
            "../crypto-config/peerOrganizations/gov.example.com/users/User1@gov.example.com/msp/signcerts",
            "User1@gov.example.com-cert.pem",
            "../crypto-config/peerOrganizations/gov.example.com/users/User1@gov.example.com/msp/keystore",
            "grpc://localhost:7051",
            "../crypto-config/peerOrganizations/gov.example.com/peers/peer0.gov.example.com/tls/ca.crt"),
    REGULATOR("regulator.example.com",
            "RegulatorMSP",
            "../crypto-config/peerOrganizations/regulator.example.com/users/User1@regulator.example.com/msp/signcerts",
            "User1@regulator.example.com-cert.pem",
            "../crypto-config/peerOrganizations/regulator.example.com/users/User1@regulator.example.com/msp/keystore",
            "grpc://localhost:8051",
            "../crypto-config/peerOrganizations/regulator.example.com/peers/peer0.regulator.example.com/tls/ca.crt"),
    CONTRACTOR1("contractor1.example.com",
            "Contractor1MSP",
            "../crypto-config/peerOrganizations/contractor1.example.com/users/User1@contractor1.example.com/msp/signcerts",
            "User1@contractor1.example.com-cert.pem",
            "../crypto-config/peerOrganizations/contractor1.example.com/users/User1@contractor1.example.com/msp/keystore",
            "grpc://localhost:9051",
            "../crypto-config/peerOrganizations/contractor1.example.com/peers/peer0.contractor1.example.com/tls/ca.crt"),
    CONTRACTOR2("contractor2.example.com",
            "Contractor2MSP",
            "../crypto-config/peerOrganizations/contractor2.example.com/users/User1@contractor2.example.com/msp/signcerts",
            "User1@contractor2.example.com-cert.pem",
            "../crypto-config/peerOrganizations/contractor2.example.com/users/User1@contractor2.example.com/msp/keystore",
            "grpc://localhost:10051",
            "../crypto-config/peerOrganizations/contractor2.example.com/peers/peer0.contractor2.example.com/tls/ca.crt"),
    ORDERER("orderer.example.com",
            "OrdererMSP",
            "",
            "",
            "" ,
            "grpc://localhost:7050",
            "../crypto-config/ordererOrganizations/example.com" +
                        "/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem");

    public String name;
    public String mspId;
    public String certPath;
    public String certName;
    public String keyPath;
    public String URL;
    private String tlsCert;


    public byte[] getTlsCert() {
        try {
            return (Files.readAllBytes(Paths.get(tlsCert)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    Participant(String name, String mspId, String certPath, String certName,
                String keyPath, String URL, String tlsCert) {
        this.name = name;
        this.mspId = mspId;
        this.certPath = certPath;
        this.certName = certName;
        this.keyPath = keyPath;
        this.URL = URL;
        this.tlsCert = tlsCert;
    }
}
