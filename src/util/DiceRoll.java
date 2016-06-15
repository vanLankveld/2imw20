package util;

import model.Edge;
import model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Guus on 13-06-16.
 */
public class DiceRoll {
    public static boolean booleanRoll() {
        return Math.random() > 0.5;
    }

    public static int diceRoll(int nrOfSides) {
        if (nrOfSides <= 1) {
            return 1;
        }
        Random rand = new Random();
        int randomNum = rand.nextInt(nrOfSides-1)+1;
        return randomNum;
    }

    public static Edge randomOutGoingEdge(Node node) {
        List<Edge> outgoing = new ArrayList<>(node.getOutgoingEdges());
        if (outgoing.isEmpty()) {
            return null;
        }
        return outgoing.get(diceRoll(outgoing.size())-1);
    }
}
