package bid.client.blockchain;

import java.util.List;

public class ProposalResponseException extends Exception {

    private List<String> errorList = null;


    public ProposalResponseException(List<String> errorList) {
        this.errorList = errorList;
    }

    public List<String> getErrorList() {
        return this.errorList;
    }
}
