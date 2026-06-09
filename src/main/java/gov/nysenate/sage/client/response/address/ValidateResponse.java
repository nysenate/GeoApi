package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.List;

public class ValidateResponse extends SourcedResponse {
    protected AddressView address;
    protected boolean validated = false;
    protected List<String> messages = null;

    public ValidateResponse(AddressResult addressResult, boolean usePunct) {
        super(addressResult);
        if (addressResult != null) {
            this.validated = addressResult.isSuccess();
            if (addressResult.getAddress() != null) {
                this.address = new AddressView(addressResult.getAddress(), usePunct);
            }
            this.messages = addressResult.getMessages();
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public boolean isValidated() {
        return validated;
    }

    public List<String> getMessages() {
        return messages;
    }
}
