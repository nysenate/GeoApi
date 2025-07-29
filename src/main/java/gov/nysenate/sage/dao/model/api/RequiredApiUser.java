package gov.nysenate.sage.dao.model.api;

import org.apache.commons.text.WordUtils;

public enum RequiredApiUser {
    PUBLIC, DEFAULT, BLUEBIRD("Bluebird CRM"), ADMIN;

    private String desc = null;

    RequiredApiUser() {}

    RequiredApiUser(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        if (desc == null) {
            return "%s API key".formatted(WordUtils.capitalizeFully(name()));
        }
        return desc;
    }
}
