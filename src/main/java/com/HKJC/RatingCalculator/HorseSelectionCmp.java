package com.HKJC.RatingCalculator;
import java.util.Comparator;

public class HorseSelectionCmp implements Comparator<HorseSelection> {
    public int compare(HorseSelection s1, HorseSelection s2)
    {
        if (s1.score == s2.score)
            return 0;
        else if (s1.score > s2.score)
            return -1;
        else
            return 1;
    }
}
