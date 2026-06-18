package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;

public record MemberInfo(String invertedName, String imageUrl, String url, String email) {
    public MemberInfo(Senator senator) {
        this(senator.getLastName() + ", " + senator.getFirstName(),
                senator.getImageUrl(), senator.getUrl(), senator.getEmail());
    }
}
