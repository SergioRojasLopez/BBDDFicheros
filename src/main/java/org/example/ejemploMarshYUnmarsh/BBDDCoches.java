package org.example.ejemploMarshYUnmarsh;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="coches")
public class BBDDCoches {
    private List<Coche> coches;

    public BBDDCoches() {
    }

    public BBDDCoches(List<Coche> coches) {
        this.coches = coches;
    }

    public List<Coche> getCoches() {
        return coches;
    }

    @XmlElement(name="coche")
    public void setCoches(List<Coche> coches) {
        this.coches = coches;
    }
}
