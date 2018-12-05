package gov.nysenate.sage.util;

public enum AsciiArt
{
    // ASCII Art generated at http://patorjk.com/software/taag/
    // SAGE FONT = USA FLAG

    SAGE("\n\n" +
    "          :::===  :::====  :::=====  :::=====    \n" +
    "          :::     :::  === :::       :::         \n" +
    "           =====  ======== === ===== ======      \n" +
    "              === ===  === ===   === ===         \n" +
    "          ======  ===  ===  =======  ========    \n" +
    "        =======================================  \n" +
    "          DEPLOYED ON DATE                     \n" +
    "        ======================================="
    );

    private String text;

    AsciiArt(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
