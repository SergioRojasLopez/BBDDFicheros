package org.example.ejemploMarshYUnmarsh;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;

public class Principal {
    public static void main(String[] args) {
        try {

            //Marshalling org.example.ejemploMarshYUnmarsh.Coche
            JAXBContext context = JAXBContext.newInstance(org.example.ejemploMarshYUnmarsh.Coche.class);
            org.example.ejemploMarshYUnmarsh.Coche nuevoCoche = new org.example.ejemploMarshYUnmarsh.Coche("1234ABD","Seat","Leon");
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(nuevoCoche, new File("nuevoCoche.xml"));

            //Unmarshallig org.example.ejemploMarshYUnmarsh.Coche
            JAXBContext context2 = JAXBContext.newInstance(org.example.ejemploMarshYUnmarsh.Coche.class);
            Unmarshaller unmarshaller = context2.createUnmarshaller();
            org.example.ejemploMarshYUnmarsh.Coche nuevoCocheUnmarshalled = (org.example.ejemploMarshYUnmarsh.Coche) unmarshaller.unmarshal(new File("nuevoCoche.xml"));
            System.out.println("Tenemos un " + nuevoCocheUnmarshalled.getMarca() +
                    " " + nuevoCocheUnmarshalled.getModelo() +
                    " y tiene la matrícula: " + nuevoCocheUnmarshalled.getMatricula());

            //Marshalling BBDD coches
            JAXBContext context3 = JAXBContext.newInstance(BBDDCoches.class);
            org.example.ejemploMarshYUnmarsh.Coche nuevoCoche1 = new org.example.ejemploMarshYUnmarsh.Coche("1234ABD","Seat","Leon");
            org.example.ejemploMarshYUnmarsh.Coche nuevoCoche2 = new org.example.ejemploMarshYUnmarsh.Coche("4356BBC","Toyota","Corolla");
            ArrayList<Coche> coches = new ArrayList<>();
            coches.add(nuevoCoche1);
            coches.add(nuevoCoche2);
            BBDDCoches bbddCoches = new BBDDCoches(coches);
            Marshaller marshaller2 = context3.createMarshaller();
            marshaller2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller2.marshal(bbddCoches, new File("coches.xml"));

            //XML to JSON

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
