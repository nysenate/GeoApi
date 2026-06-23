package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;
import org.apache.commons.lang.StringUtils;

public record MemberInfo(String nameStart, String nameEnd, String imageUrl, String url, String email) {
    public MemberInfo {
        nameStart = StringUtils.trim(nameStart);
        nameEnd = StringUtils.trim(nameEnd);
        imageUrl = StringUtils.trim(imageUrl);
        url = StringUtils.trim(url);
        email = StringUtils.trim(email);
    }

    public MemberInfo(Senator senator) {
        this(senator.getFirstName(), senator.getLastName(),
                senator.getImageUrl(), senator.getUrl(), senator.getEmail());
    }
}
