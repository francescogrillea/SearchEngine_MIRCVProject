package org.offline_phase;

import junit.framework.TestCase;
import org.common.Posting;

public class PostingTest extends TestCase {
    public void testPostingESEMPIO(){  //il seguente metodo va rimosso. #TODO rimuovere prima della consegna
        //esempio di test. premi sulla freccia verde a fianco a questo specifico test per runnare solo il test e non
        //tutta la classe di test
        Posting prova = new Posting(1,(byte) 5 );
        System.out.println("test di prova con un posting "+prova); //un test può fare system out

        assertEquals(1,prova.getDoc_id()); //ti dice test passato se questa condizione è verificata
        //assertEquals(2,prova.getDoc_id()); //restituisce errore altrimenti
    }
}
