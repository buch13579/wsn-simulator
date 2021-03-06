package events;
import WSN.*;

import java.util.Random;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends events.Event {


    private Packet p;

    public StopTxEvent(StartTxEvent e, double time){
        super(e.getNode(), time, WSN.normColor);
        this.p = e.getPacket();
    }

    @Override
    public String toString(){
        return "[" + time + "][StopTxEvent] from node " +  this.n;
    }

    public void run(){
        super.run();
        this.n.setSize(WSN.normSize);
        WSN.trasmittingNodes.remove(n);
        Random r = new Random();

        if (n.collided){

            System.out.println("Tranmission unsuccessful");

            int oldCW = n.getCW();
            int newCW = Math.min(2*(oldCW+1) - 1, WSN.CWmax);

            n.setCW(newCW);


            n.setBOcounter(r.nextInt(n.getCW() + 1));

            // start new round NOW
            WSN.eventList.add(new StartListeningEvent(n, time));
        }else{

            System.out.println("Tranmission successful");
            n.setCW(WSN.CWmin);
            n.setBOcounter(r.nextInt(n.getCW() + 1));

            // start new round after SIFS + tACK
            WSN.eventList.add(new StartListeningEvent(n, time + WSN.tACK + WSN.SIFS));
        }

        // if the end of this transmission frees up the channel then notify all of the listening nodes
        // and make them start listening for DIFS seconds of silence
        if (WSN.trasmittingNodes.isEmpty()){
            WSN.status = WSN.CHANNEL_STATUS.FREE;

            for (Node listening :
                    WSN.listeningNodes) {
                WSN.eventList.add(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));
                listening.freeChannel = true;
            }
        }

        n.setStatus(WSN.NODE_STATUS.IDLING);


    }

    public Packet getPacket(){
        return this.p;
    }
}
