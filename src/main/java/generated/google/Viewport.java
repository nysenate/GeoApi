//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.29 at 12:58:15 PM EDT 
//


package generated.google;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}southwest"/>
 *         &lt;element ref="{}northeast"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "southwest",
    "northeast"
})
@XmlRootElement(name = "viewport")
public class Viewport {

    @XmlElement(required = true)
    protected Southwest southwest;
    @XmlElement(required = true)
    protected Northeast northeast;

    /**
     * Gets the value of the southwest property.
     * 
     * @return
     *     possible object is
     *     {@link Southwest }
     *     
     */
    public Southwest getSouthwest() {
        return southwest;
    }

    /**
     * Sets the value of the southwest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Southwest }
     *     
     */
    public void setSouthwest(Southwest value) {
        this.southwest = value;
    }

    /**
     * Gets the value of the northeast property.
     * 
     * @return
     *     possible object is
     *     {@link Northeast }
     *     
     */
    public Northeast getNortheast() {
        return northeast;
    }

    /**
     * Sets the value of the northeast property.
     * 
     * @param value
     *     allowed object is
     *     {@link Northeast }
     *     
     */
    public void setNortheast(Northeast value) {
        this.northeast = value;
    }

}
