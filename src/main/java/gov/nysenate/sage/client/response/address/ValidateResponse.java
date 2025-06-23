package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.result.AddressResult;

public class ValidateResponse extends BaseResponse {
    protected AddressView address;
    protected boolean validated = false;

    public ValidateResponse(AddressResult addressResult, boolean usePunct) {
        super(addressResult);
        if (addressResult != null) {
            this.validated = addressResult.isValidated();
            if (addressResult.getAddress() != null) {
                this.address = new AddressView(addressResult.getAddress(), usePunct);
            }
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public boolean isValidated() {
        return validated;
    }
}
