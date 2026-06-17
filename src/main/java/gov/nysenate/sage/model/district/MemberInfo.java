package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;

public record MemberInfo(String name, String imageUrl, String url, String email) {
    public MemberInfo(Senator senator) {
        this(senator.getName(), senator.getUrl(), senator.getImageUrl(), senator.getEmail());
    }
}
