package org.online_phase.scoring;

import org.junit.Test;
import org.online_phase.ScoreBoard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreboardTest {
    @Test
    public void scoreBoardTest(){
        ScoreBoard sc = new ScoreBoard(5);
        for(int i=0;i<4;i++)
            sc.add(i,1);

        assertEquals(0f,sc.getThreshold(),0.001); //delta is the tolerance for float comparison
        sc.add(4,1);
        assertEquals(1f,sc.getThreshold(),0.001);
        sc.add(5,2);
        assertEquals(1f,sc.getThreshold(),0.001);

        for(int i=6;i<10;i++)
            sc.add(i,2);
        assertEquals(2f,sc.getThreshold(),0.001);

    }
}
